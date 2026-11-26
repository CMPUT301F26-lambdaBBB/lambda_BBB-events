package com.example.lambda_bbb_events.view.ui.user_screens.notification

import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.message.ConversationType
import com.example.lambda_bbb_events.model.message.MessagingAccess
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.model.user.UserRole

/*
 * Builds the people shown on the notification page, each listed once no matter how many
 * campsites connect them to the current user.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * This whole file was written with the help of this AI tool.
 * Prompts:
 * - "Now on the notification page as well. Please also remove the profile duplication bug. Also
 *   please fix the way the admins work since it seems all admins share an account. One more thing
 *   is to fix the notification page so that the spacing looks right."
 * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
 *   chats per admin".
 */

/**
 * A person the current user can message or has messaged.
 *
 * @property contactId Device ID of the person.
 * @property type Type of conversation held with the person.
 * @property name Display name of the person.
 * @property imageId The person's profile picture ID, blank if they have none.
 * @property subtitle What connects the person to the current user, such as their campsites.
 * @property conversation The existing conversation with the person, or `null` if there is none yet.
 */
data class MessageContact(
    val contactId: String,
    val type: ConversationType,
    val name: String,
    val imageId: String,
    val subtitle: String,
    val conversation: Conversation? = null
)

/**
 * Gets the people the current user already has conversations with, most recent first.
 *
 * @param conversations The current user's conversations.
 * @param messagingAccess The current user's campsite messaging access records.
 * @param currentUserId Device ID of the current user.
 * @param profiles Every known user, by device ID.
 * @param campsiteNames Campsite names, by campsite ID.
 */
fun conversationContacts(
    conversations: List<Conversation>,
    messagingAccess: List<MessagingAccess>,
    currentUserId: String,
    profiles: Map<String, User>,
    campsiteNames: Map<String, String>
): List<MessageContact> {
    return conversations
        .distinctBy { it.conversationId }
        .sortedByDescending { it.updatedAt }
        .map { conversation ->
            val contactId = conversation.contactIdFor(currentUserId)
            val type = ConversationType.valueOf(conversation.type)
            val isCamperSide = conversation.userId == currentUserId

            buildContact(
                contactId = contactId,
                type = type,
                isCamperSide = isCamperSide,
                profiles = profiles,
                subtitle = contactSubtitle(contactId, type, isCamperSide, messagingAccess, campsiteNames),
                conversation = conversation
            )
        }
}

/**
 * Gets the people the current user can start a conversation with: each owner of a campsite
 * they are enrolled in, and every administrator once they have enrolled anywhere. People who
 * already have a conversation with the current user are left out.
 *
 * @param conversations The current user's conversations.
 * @param messagingAccess The current user's campsite messaging access records.
 * @param currentUserId Device ID of the current user.
 * @param profiles Every known user, by device ID.
 * @param campsiteNames Campsite names, by campsite ID.
 */
fun availableContacts(
    conversations: List<Conversation>,
    messagingAccess: List<MessagingAccess>,
    currentUserId: String,
    profiles: Map<String, User>,
    campsiteNames: Map<String, String>
): List<MessageContact> {
    val existingContactIds = conversations
        .filter { it.userId == currentUserId }
        .map { it.contactIdFor(currentUserId) }
        .toSet()

    val ownerIds = messagingAccess
        .filter { it.active }
        .map { it.ownerId }
        .filter { it.isNotBlank() && it != currentUserId }
        .distinct()

    val adminIds = if (messagingAccess.isEmpty()) {
        emptyList()
    } else {
        adminIdsByName(profiles).filter { it != currentUserId }
    }

    val owners = ownerIds.map { ConversationType.OWNER to it }
    val admins = adminIds.map { ConversationType.ADMIN to it }

    return (owners + admins)
        .filter { (_, contactId) -> contactId !in existingContactIds }
        .map { (type, contactId) ->
            buildContact(
                contactId = contactId,
                type = type,
                isCamperSide = true,
                profiles = profiles,
                subtitle = contactSubtitle(contactId, type, true, messagingAccess, campsiteNames)
            )
        }
}

/**
 * Gets the IDs of every administrator, ordered by name.
 */
private fun adminIdsByName(profiles: Map<String, User>): List<String> {
    return profiles.values
        .filter { it.role == UserRole.ADMIN }
        .sortedBy { it.name.lowercase() }
        .map { it.deviceId }
}

/**
 * Builds a contact, using the person's profile for their name and picture.
 *
 * @param isCamperSide Whether the current user is the camper in the conversation, so the
 * contact is the owner or administrator.
 */
private fun buildContact(
    contactId: String,
    type: ConversationType,
    isCamperSide: Boolean,
    profiles: Map<String, User>,
    subtitle: String,
    conversation: Conversation? = null
): MessageContact {
    val profile = profiles[contactId]
    val fallbackName = contactRoleLabel(type, isCamperSide)

    return MessageContact(
        contactId = contactId,
        type = type,
        name = profile?.name?.takeIf { it.isNotBlank() } ?: fallbackName,
        imageId = profile?.profilePictureImageId.orEmpty(),
        subtitle = subtitle,
        conversation = conversation
    )
}

/**
 * Describes how a contact is connected to the current user. A camper sees the names of an
 * owner's campsites they are enrolled in; otherwise the contact's role is shown.
 */
private fun contactSubtitle(
    contactId: String,
    type: ConversationType,
    isCamperSide: Boolean,
    messagingAccess: List<MessagingAccess>,
    campsiteNames: Map<String, String>
): String {
    val roleLabel = contactRoleLabel(type, isCamperSide)
    if (!isCamperSide || type != ConversationType.OWNER) {
        return roleLabel
    }

    val ownerCampsites = messagingAccess
        .filter { it.ownerId == contactId }
        .mapNotNull { campsiteNames[it.campsiteId] }
        .distinct()

    return ownerCampsites.joinToString(", ").ifBlank { roleLabel }
}

/**
 * Gets the role of the person on the other side of a conversation.
 *
 * @param type Type of conversation.
 * @param isCamperSide Whether the viewer is the camper, so the other person is the owner or
 * administrator.
 * @return "Admin", "Campsite Owner", or "Camper".
 */
fun contactRoleLabel(type: ConversationType, isCamperSide: Boolean): String {
    return when {
        !isCamperSide -> "Camper"
        type == ConversationType.ADMIN -> "Admin"
        else -> "Campsite Owner"
    }
}

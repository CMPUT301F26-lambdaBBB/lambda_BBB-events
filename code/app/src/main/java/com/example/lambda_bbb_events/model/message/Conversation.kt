package com.example.lambda_bbb_events.model

import com.example.lambda_bbb_events.model.message.ConversationType
import com.google.firebase.Timestamp

/*
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
 * - "Okay so there are no conversations yet so can I have admins be able to message users from
 *   the profile admin screen"
 */

/**
 * Represents a private conversation between a camper and one other person: a campsite
 * owner or a specific administrator.
 *
 * There is one conversation per pair of people, no matter how many of the owner's
 * campsites the camper is enrolled in, so each contact appears once. Each conversation
 * contains multiple messages stored in a Firestore subcollection.
 *
 * @property conversationId Unique identifier of the conversation, see [ownerConversationId]
 * and [adminConversationId].
 * @property campsiteId The campsite the camper was enrolled in when the conversation started.
 * Empty when an administrator started the conversation from the user's profile.
 * @property userId Unique identifier of the camper.
 * @property ownerId Unique identifier of the campsite owner. Empty for admin conversations.
 * @property adminId Unique identifier of the administrator. Empty for owner conversations.
 * @property type Type of conversation (OWNER or ADMIN).
 * @property lastMessage Content of the most recent message.
 * @property updatedAt Timestamp of the most recent update.
 */
class Conversation(
    val conversationId: String = "",
    val campsiteId: String = "",
    val userId: String = "",
    val ownerId: String = "",
    val adminId: String = "",
    val type: String = "",
    val lastMessage: String = "",
    var updatedAt: Timestamp = Timestamp.now()
) {

    /**
     * Gets the ID of the person [viewerId] is talking to in this conversation.
     *
     * @param viewerId Device ID of the person viewing the conversation.
     * @return The other participant's device ID.
     */
    fun contactIdFor(viewerId: String): String {
        if (viewerId != userId) {
            return userId
        }
        return if (type == ConversationType.ADMIN.name) adminId else ownerId
    }

    /**
     * Whether [personId] is one of the two people in this conversation.
     *
     * @param personId Device ID to check.
     */
    fun hasParticipant(personId: String): Boolean {
        return personId.isNotBlank() &&
                (personId == userId || personId == contactIdFor(userId))
    }

    /**
     * Whether this conversation uses the one-per-pair ID scheme. Conversations from before
     * it were kept per campsite and shared by all administrators, and are no longer shown.
     */
    fun hasCurrentId(): Boolean {
        val expectedId = when (type) {
            ConversationType.OWNER.name -> ownerConversationId(userId, ownerId)
            ConversationType.ADMIN.name -> adminConversationId(userId, adminId)
            else -> return false
        }
        return conversationId == expectedId
    }

    companion object {

        /**
         * Gets the ID of the conversation between a camper and a campsite owner.
         *
         * @param userId Device ID of the camper.
         * @param ownerId Device ID of the campsite owner.
         */
        fun ownerConversationId(userId: String, ownerId: String): String =
            "owner_${userId}_${ownerId}"

        /**
         * Gets the ID of the conversation between a camper and an administrator.
         *
         * @param userId Device ID of the camper.
         * @param adminId Device ID of the administrator.
         */
        fun adminConversationId(userId: String, adminId: String): String =
            "admin_${userId}_${adminId}"
    }
}

package com.example.lambda_bbb_events.view.ui.user_screens.notification

import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.message.ConversationType
import com.example.lambda_bbb_events.model.message.MessagingAccess
import com.example.lambda_bbb_events.model.user.Admin
import com.example.lambda_bbb_events.model.user.CampsiteOwner
import com.example.lambda_bbb_events.model.user.Entrant
import com.example.lambda_bbb_events.model.user.User
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageContactsUnitTest {
    private val camper = Entrant(deviceId = "ed", name = "Ed")
    private val owner = CampsiteOwner(deviceId = "olive", name = "Olive", profilePictureImageId = "olive-pic")
    private val adminA = Admin(deviceId = "ada", name = "Ada")
    private val adminB = Admin(deviceId = "bo", name = "Bo")

    private val profiles: Map<String, User> =
        listOf(camper, owner, adminA, adminB).associateBy { it.deviceId }

    private val campsiteNames = mapOf("lake" to "Lake Site", "hill" to "Hill Site", "bay" to "Bay Site")

    private val accessToTwoOfOlivesSites = listOf(
        MessagingAccess(campsiteId = "lake", ownerId = "olive", active = true),
        MessagingAccess(campsiteId = "hill", ownerId = "olive", active = true)
    )

    private fun ownerChat(updatedSeconds: Long = 1) = Conversation(
        conversationId = Conversation.ownerConversationId("ed", "olive"),
        campsiteId = "lake",
        userId = "ed",
        ownerId = "olive",
        type = ConversationType.OWNER.name,
        updatedAt = Timestamp(updatedSeconds, 0)
    )

    private fun adminChat(adminId: String, updatedSeconds: Long = 1) = Conversation(
        conversationId = Conversation.adminConversationId("ed", adminId),
        campsiteId = "lake",
        userId = "ed",
        adminId = adminId,
        type = ConversationType.ADMIN.name,
        updatedAt = Timestamp(updatedSeconds, 0)
    )

    private fun available(
        conversations: List<Conversation> = emptyList(),
        access: List<MessagingAccess> = accessToTwoOfOlivesSites,
        viewerId: String = "ed"
    ) = availableContacts(conversations, access, viewerId, profiles, campsiteNames)

    @Test
    fun ownerOfSeveralCampsitesIsListedOnceWithAllTheirCampsites() {
        val owners = available().filter { it.type == ConversationType.OWNER }

        assertEquals(1, owners.size)
        assertEquals("Olive", owners.single().name)
        assertEquals("olive-pic", owners.single().imageId)
        assertEquals("Lake Site, Hill Site", owners.single().subtitle)
    }

    @Test
    fun eachAdminIsListedOnceRegardlessOfCampsiteCount() {
        val admins = available().filter { it.type == ConversationType.ADMIN }

        assertEquals(listOf("ada", "bo"), admins.map { it.contactId })
    }

    @Test
    fun noAdminsAreAvailableBeforeEnrollingAnywhere() {
        assertTrue(available(access = emptyList()).isEmpty())
    }

    @Test
    fun peopleWithAConversationAreNotAvailableAgain() {
        val contacts = available(conversations = listOf(ownerChat(), adminChat("ada")))

        assertEquals(listOf("bo"), contacts.map { it.contactId })
    }

    @Test
    fun adminCannotStartAConversationWithThemself() {
        val contacts = available(viewerId = "ada")

        assertFalse(contacts.any { it.contactId == "ada" })
    }

    @Test
    fun inactiveAccessDoesNotOfferTheOwner() {
        val access = listOf(MessagingAccess(campsiteId = "bay", ownerId = "olive", active = false))

        assertFalse(available(access = access).any { it.type == ConversationType.OWNER })
    }

    @Test
    fun conversationsAreNamedAfterTheOtherPersonAndNewestFirst() {
        val conversations = listOf(ownerChat(updatedSeconds = 1), adminChat("ada", updatedSeconds = 2))

        val asCamper = conversationContacts(conversations, accessToTwoOfOlivesSites, "ed", profiles, campsiteNames)
        val asAdmin = conversationContacts(listOf(adminChat("ada")), emptyList(), "ada", profiles, campsiteNames)

        assertEquals(listOf("Ada", "Olive"), asCamper.map { it.name })
        assertEquals("Admin", asCamper.first().subtitle)
        assertEquals("Ed", asAdmin.single().name)
        assertEquals("Camper", asAdmin.single().subtitle)
    }

    @Test
    fun conversationBelongsOnlyToItsTwoPeople() {
        val chat = adminChat("ada")

        assertTrue(chat.hasParticipant("ed"))
        assertTrue(chat.hasParticipant("ada"))
        assertFalse(chat.hasParticipant("bo"))
        assertEquals("ada", chat.contactIdFor("ed"))
        assertEquals("ed", chat.contactIdFor("ada"))
    }

    @Test
    fun conversationsFromThePerCampsiteSchemeAreNotCurrent() {
        val legacy = Conversation(
            conversationId = "lake_ed_ADMIN",
            campsiteId = "lake",
            userId = "ed",
            type = ConversationType.ADMIN.name
        )

        assertFalse(legacy.hasCurrentId())
        assertTrue(adminChat("ada").hasCurrentId())
        assertTrue(ownerChat().hasCurrentId())
    }
}

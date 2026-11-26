package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.Message
import com.example.lambda_bbb_events.model.message.ConversationType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * This whole file was written with the help of this AI tool.
 * Prompts:
 * - "Fix the github action now"
 * - "I mean it passes because you removed the tests cases."
 */

/**
 * Tests listening for, sending, and deleting messages with [MessageRepository] against a mocked Firestore,
 * in an admin conversation between camper "ed" and administrator "ada".
 */
class MessageRepositoryUnitTest {
    private val db = mockk<FirebaseFirestore>()
    private val batch = mockk<WriteBatch>(relaxed = true)

    private val conversation = Conversation(
        conversationId = Conversation.adminConversationId("ed", "ada"),
        userId = "ed",
        adminId = "ada",
        type = ConversationType.ADMIN.name
    )

    private lateinit var conversationDoc: DocumentReference
    private lateinit var messages: MockCollection
    private lateinit var repository: MessageRepository

    @Before
    fun setup() {
        conversationDoc = MockCollection(db, "conversations").document(conversation.conversationId)
        messages = MockCollection(db, "conversations/${conversation.conversationId}/messages")
        storeConversation(conversation, lastMessageId = "m1")
        every { db.runBatch(any()) } answers {
            firstArg<WriteBatch.Function>().apply(batch)
            completedTask(null)
        }
        repository = MessageRepository(conversation.conversationId, db)
    }

    /** Makes reading the conversation return [stored], whose latest message is [lastMessageId]. */
    private fun storeConversation(stored: Conversation, lastMessageId: String) {
        val snapshot = documentOf(stored.conversationId, Conversation::class.java, stored)
        every { snapshot.getString("lastMessageId") } returns lastMessageId
        every { conversationDoc.get() } returns completedTask(snapshot)
    }

    /** Stores message [id], sent by [senderId], and returns its document reference. */
    private fun storeMessage(id: String, senderId: String): DocumentReference {
        val ref = messages.document(id)
        val message = Message(messageId = id, content = "Hello", senderId = senderId)
        every { ref.get() } returns completedTask(documentOf(id, Message::class.java, message))
        return ref
    }

    @Test
    fun sentMessageIsAddressedToTheOtherPersonAndUpdatesThePreview() = runTest {
        val newRef = messages.document("new")
        every { messages.ref.document() } returns newRef

        repository.sendMessage("  Is the site open?  ", senderId = "ed")

        verify {
            batch.set(newRef, match<Map<String, Any>> {
                it["senderId"] == "ed" && it["recipientId"] == "ada" && it["content"] == "Is the site open?"
            })
            batch.update(conversationDoc, match<Map<String, Any>> {
                it["lastMessage"] == "Is the site open?" && it["lastMessageId"] == "new"
            })
        }
    }

    @Test
    fun personOutsideTheConversationCannotSend() {
        assertThrows(IllegalStateException::class.java) {
            runBlocking { repository.sendMessage("Hi", senderId = "bo") }
        }
        verify(exactly = 0) { db.runBatch(any()) }
    }

    @Test
    fun conversationFromThePerCampsiteSchemeCannotBeMessaged() {
        storeConversation(
            Conversation(conversationId = "lake_ed_ADMIN", userId = "ed", type = ConversationType.ADMIN.name),
            lastMessageId = ""
        )

        assertThrows(IllegalStateException::class.java) {
            runBlocking { repository.sendMessage("Hi", senderId = "ed") }
        }
    }

    @Test
    fun deletingTheLatestMessageClearsItAndThePreview() = runTest {
        val ref = storeMessage("m1", senderId = "ed")

        repository.deleteMessage("m1", requesterId = "ed")

        verify {
            batch.update(ref, match<Map<String, Any>> { it["content"] == "" && it["deleted"] == true })
            batch.update(conversationDoc, "lastMessage", Message.DELETED_TEXT)
        }
    }

    @Test
    fun deletingAnOlderMessageKeepsThePreview() = runTest {
        storeMessage("m0", senderId = "ed")

        repository.deleteMessage("m0", requesterId = "ed")

        verify(exactly = 0) { batch.update(conversationDoc, "lastMessage", any()) }
    }

    @Test
    fun recipientCannotDeleteTheSendersMessage() {
        storeMessage("m1", senderId = "ed")

        assertThrows(IllegalStateException::class.java) {
            runBlocking { repository.deleteMessage("m1", requesterId = "ada") }
        }
        verify(exactly = 0) { db.runBatch(any()) }
    }

    @Test
    fun adminCanDeleteTheCampersMessage() = runTest {
        val ref = storeMessage("m1", senderId = "ed")

        repository.deleteMessage("m1", requesterId = "ada", isAdmin = true)

        verify { batch.update(ref, match<Map<String, Any>> { it["deleted"] == true }) }
    }

    @Test
    fun personOutsideTheConversationCannotDeleteEvenAsAdmin() {
        storeMessage("m1", senderId = "ed")

        assertThrows(IllegalStateException::class.java) {
            runBlocking { repository.deleteMessage("m1", requesterId = "bo", isAdmin = true) }
        }
    }

    /** Creates a stored message with [id] and [content], sent at [seconds]. */
    private fun messageDoc(id: String, content: String, seconds: Long) =
        documentOf(
            id,
            Message::class.java,
            Message(messageId = id, content = content, senderId = "ed", timestamp = Timestamp(seconds, 0))
        )

    @Test
    fun listenerCachesTheConversationsMessages() {
        repository.startListening()

        messages.emit(messageDoc("m1", "Hello, is this campsite available?", seconds = 10))

        val cached = repository.messages.single()
        assertEquals("m1", cached.messageId)
        assertEquals("Hello, is this campsite available?", cached.content)
        assertEquals("ed", cached.senderId)
    }

    @Test
    fun messagesAreNewestFirst() {
        repository.startListening()

        messages.emit(
            messageDoc("older", "Older message", seconds = 10),
            messageDoc("newer", "Newer message", seconds = 20)
        )

        assertEquals(listOf("newer", "older"), repository.messages.map { it.messageId })
    }

    @Test
    fun deletedMessageStaysInPlaceAfterTheListenerUpdates() {
        repository.startListening()

        messages.emit(
            documentOf("m1", Message::class.java, Message(messageId = "m1", senderId = "ed", deleted = true))
        )

        assertTrue(repository.messages.single().deleted)
    }

    @Test
    fun unknownMessageIsNotCached() {
        repository.startListening()

        messages.emit(messageDoc("m1", "Hi", seconds = 10))

        assertNull(repository.messages.find { it.messageId == "non-existent-id-999" })
    }

    @Test
    fun stoppingListeningRemovesTheListener() {
        repository.startListening()
        repository.stopListening()

        verify(exactly = 1) { messages.registration.remove() }
    }
}

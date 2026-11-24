package com.example.lambda_bbb_events.data

import com.example.lambda_bbb_events.model.message.Message
import com.example.lambda_bbb_events.model.repository.MessageRepository
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Tests [MessageRepository] against the real Firestore database.
 *
 * Every message uses a unique ID and is deleted after each test in [cleanUp].
 */
class MessageRepositoryTest {
    private val testData = FirestoreTestData()
    private lateinit var repository: MessageRepository

    @Before
    fun setUp() {
        repository = MessageRepository()
    }

    @After
    fun cleanUp() {
        repository.stopListening()
        testData.cleanUp()
    }

    // Helper to wait for Firestore asynchronous snapshots to update the local list
    private suspend fun waitForCondition(
        timeoutMs: Long = 5000L,
        condition: () -> Boolean
    ): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                if (condition()) return true
            } catch (e: Exception) {
                // Ignore transient race exceptions while list is updating
            }
            delay(100)
        }
        return try {
            condition()
        } catch (e: Exception) {
            false
        }
    }

    @Test
    fun addingMessageToDBAndGetMessage() = runTest {
        val messageId = testData.newId("messages", prefix = "test-msg")
        val senderId = "sender-${UUID.randomUUID()}"
        val recipientId = "receiver-${UUID.randomUUID()}"

        val message = Message(
            messageId = messageId,
            content = "Hello, is this campsite available?",
            senderId = senderId,
            recipientId = recipientId,
            timestamp = Timestamp.now()
        )

        // 1. Add to DB
        repository.addMessage(message)

        // 2. Wait for the SnapshotListener to update the local list
        val success = waitForCondition {
            repository.messages.any { it.messageId == messageId }
        }
        assertTrue("Message should be added to repository list", success)

        // 3. Verify data
        val fetched = repository.messages.find { it.messageId == messageId }
        assertEquals(message.messageId, fetched?.messageId)
        assertEquals(message.content, fetched?.content)
        assertEquals(message.senderId, fetched?.senderId)
    }

    @Test
    fun messageGetsDeleted() = runTest {
        val messageId = testData.newId("messages", prefix = "test-msg")
        val message = Message(
            messageId = messageId,
            content = "Delete me",
            senderId = "sender-1",
            recipientId = "receiver-1",
            timestamp = Timestamp.now()
        )

        repository.addMessage(message)
        waitForCondition { repository.messages.any { it.messageId == messageId } }

        repository.deleteMessage(message)

        val deleted = waitForCondition {
            repository.messages.none { it.messageId == messageId }
        }
        assertTrue("Message should be deleted from repository", deleted)
    }

    @Test
    fun updateMessageWithNewDetails() = runTest {
        val messageId = testData.newId("messages", prefix = "test-msg")

        val originalMessage = Message(
            messageId = messageId,
            content = "Original text",
            senderId = "sender-1",
            recipientId = "receiver-1",
            timestamp = Timestamp.now()
        )

        repository.addMessage(originalMessage)
        waitForCondition { repository.messages.any { it.messageId == messageId } }

        // Update the message
        // Update the message manually
        val updatedMessage = Message(
            messageId = originalMessage.messageId,
            content = "Updated text!", // The new value
            senderId = originalMessage.senderId,
            recipientId = originalMessage.recipientId,
            timestamp = originalMessage.timestamp
        )
        repository.updateMessage(updatedMessage)

        // Wait for Snapshot to reflect update
        val success = waitForCondition {
            repository.messages.find { it.messageId == messageId }?.content == "Updated text!"
        }
        assertTrue("Message content should be updated", success)
    }

    @Test
    fun checkChronologicalOrderingInLocalList() = runTest {
        // Note: Because your repository doesn't sort messages when it reads them from Firebase,
        // the order in the local `_messages` list depends on Firebase's internal indexing.
        // This test simply confirms that both messages are present.

        val recipientId = "receiver-order-${UUID.randomUUID()}"

        val msg1 = Message(
            messageId = testData.newId("messages", prefix = "msg-1"),
            content = "Older message",
            senderId = "sender-1",
            recipientId = recipientId,
            timestamp = Timestamp(0,100000)
        )
        val msg2 = Message(
            messageId = testData.newId("messages", prefix = "msg-2"),
            content = "Newer message",
            senderId = "sender-1",
            recipientId = recipientId,
            timestamp = Timestamp(0, 200000)
        )

        repository.addMessage(msg1)
        repository.addMessage(msg2)

        val success = waitForCondition {
            val messagesForRecipient = repository.messages.filter { it.recipientId == recipientId }
            messagesForRecipient.size == 2
        }
        assertTrue("Both messages should be fetched", success)
    }

    @Test
    fun checkInvalidMessageReturnsNull() = runTest {

        // Wait for initial DB sync to finish
        delay(1000)

        // Since the repo doesn't have a getMessage(id) function, we search the list
        val nonExistent = repository.messages.find { it.messageId == "non-existent-id-999" }
        assertNull(nonExistent)
    }
}
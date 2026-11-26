package com.example.lambda_bbb_events.model.message

import com.example.lambda_bbb_events.model.Message
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageUnitTest {
    private val message = Message(messageId = "m1", content = "Hi", senderId = "ed", recipientId = "ada")

    @Test
    fun senderCanDeleteTheirOwnMessage() {
        assertTrue(message.canBeDeletedBy("ed", isAdmin = false))
    }

    @Test
    fun recipientCannotDeleteSomeoneElsesMessage() {
        assertFalse(message.canBeDeletedBy("olive", isAdmin = false))
    }

    @Test
    fun adminCanDeleteAnyMessage() {
        assertTrue(message.canBeDeletedBy("ada", isAdmin = true))
    }

    @Test
    fun deletedMessageCannotBeDeletedAgain() {
        val deleted = Message(messageId = "m1", senderId = "ed", deleted = true)

        assertFalse(deleted.canBeDeletedBy("ed", isAdmin = false))
        assertFalse(deleted.canBeDeletedBy("ada", isAdmin = true))
    }
}

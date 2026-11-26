package com.example.lambda_bbb_events.model.message

import com.example.lambda_bbb_events.model.Message
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * This whole file was written with the help of this AI tool.
 */

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

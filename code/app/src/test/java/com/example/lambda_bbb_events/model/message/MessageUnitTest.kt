package com.example.lambda_bbb_events.model.message

import com.example.lambda_bbb_events.model.Message
import com.google.firebase.Timestamp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * This whole file was written with the help of this AI tool.
 * Prompts:
 * - "Also please clean up a database and offer a way for users to delete messages with a message
 *   deleted plus. Show also that messages are active."
 * - "So to clarify I want to allow users and admins to remove messages."
 */

/** Verifies the message model's values and behavior. */
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

    private fun sentAt(senderId: String, seconds: Long) =
        Message(messageId = "m$seconds", senderId = senderId, timestamp = Timestamp(seconds, 0))

    /**
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Can you combine the message timestamps because it's a bit weird to put it in multiple
     *   places for the same time."
     */
    @Test
    fun messagesFromTheSamePersonInTheSameMinuteAreGrouped() {
        assertTrue(sentAt("ed", 600).isGroupedWith(sentAt("ed", 659)))
    }

    /**
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Can you combine the message timestamps because it's a bit weird to put it in multiple
     *   places for the same time."
     */
    @Test
    fun messagesInDifferentMinutesAreNotGrouped() {
        assertFalse(sentAt("ed", 659).isGroupedWith(sentAt("ed", 660)))
    }

    /**
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Can you combine the message timestamps because it's a bit weird to put it in multiple
     *   places for the same time."
     */
    @Test
    fun messagesFromDifferentPeopleAreNotGrouped() {
        assertFalse(sentAt("ed", 600).isGroupedWith(sentAt("ada", 600)))
    }
}

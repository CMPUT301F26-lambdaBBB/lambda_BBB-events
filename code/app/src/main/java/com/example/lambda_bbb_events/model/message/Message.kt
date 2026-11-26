package com.example.lambda_bbb_events.model

import com.google.firebase.Timestamp

/**
 * Represents a message exchanged between users, campsite
 * owners, and administrators.
 *
 * @property messageId Unique identifier of the message.
 * @property content Text content of the message.
 * @property senderId Unique identifier of the sender.
 * @property recipientId Unique identifier of the recipient.
 * @property timestamp Timestamp indicating when the message
 *                     was sent.
 * @property deleted Whether the message was deleted. A deleted
 *                   message keeps its place in the conversation,
 *                   but its content is removed.
 */
class Message(
    val messageId: String = "",
    val content: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    var timestamp: Timestamp = Timestamp.now(),
    val deleted: Boolean = false
) {

    /**
     * Whether [personId] may delete this message: its sender can, and so can
     * administrators, as long as it has not already been deleted.
     *
     * @param personId Device ID of the person asking to delete the message.
     * @param isAdmin Whether that person is an administrator.
     */
    fun canBeDeletedBy(personId: String, isAdmin: Boolean): Boolean {
        return !deleted && personId.isNotBlank() && (senderId == personId || isAdmin)
    }

    companion object {

        /** Shown in place of a deleted message, in the chat and the conversation preview. */
        const val DELETED_TEXT = "Message deleted"
    }
}
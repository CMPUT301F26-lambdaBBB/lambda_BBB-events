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
 *                       Uses "ADMIN_SUPPORT" for messages
 *                       addressed to the shared admin inbox.
 * @property timestamp Timestamp indicating when the message
 *                     was sent.
 */
class Message(
    val messageId: String = "",
    val content: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    var timestamp: Timestamp = Timestamp.now()
)
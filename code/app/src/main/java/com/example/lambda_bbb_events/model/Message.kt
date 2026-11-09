package com.example.lambda_bbb_events.model

import com.google.firebase.Timestamp

/*
 * Defines messages exchanged between users.
 */

/**
 * Represents a message sent between users.
 *
 * @property messageId The unique ID of the message.
 * @property content The message's text content.
 * @property senderId The device ID of the sender.
 * @property recipientId The device ID of the recipient.
 * @property timestamp The timestamp associated with the message.
 */
class Message(
    val messageId: String = "",
    val content: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    var timestamp: Timestamp = Timestamp.now()
)
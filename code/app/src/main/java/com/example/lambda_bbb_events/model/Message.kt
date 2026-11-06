package com.example.lambda_bbb_events.model

import com.google.firebase.Timestamp

/**
 * Represents a message sent between users.
 *
 * @property messageId The unique ID of the message. For storage in firebase and admin's manage of messages
 * @property content The content of the message.
 * @property senderId The deviceID of the user who sent the message.
 * @property recipientId The deviceID of the user who should receive the message.
 * @property timestamp The time the message was sent. (not sure whether is required)
 */
class Message(
    val messageId: String = "",
    val content: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    var timestamp: Timestamp = Timestamp.now()
)
package com.example.lambda_bbb_events.model

import com.google.firebase.Timestamp

/**
 * Represents a conversation between a camper and a campsite
 * owner or authorized administrators.
 *
 * Each conversation contains multiple messages stored
 * in a Firestore subcollection.
 *
 * @property conversationId Unique identifier of the conversation.
 * @property campsiteId Unique identifier of the associated campsite.
 * @property userId Unique identifier of the camper.
 * @property ownerId Unique identifier of the campsite owner.
 *                   Empty for admin conversations.
 * @property type Type of conversation (OWNER or ADMIN).
 * @property lastMessage Content of the most recent message.
 * @property updatedAt Timestamp of the most recent update.
 */
class Conversation(
    val conversationId: String = "",
    val campsiteId: String = "",
    val userId: String = "",
    val ownerId: String = "",
    val type: String = "",
    val lastMessage: String = "",
    var updatedAt: Timestamp = Timestamp.now()
)
package com.example.lambda_bbb_events.model.message

/**
 * Represents a user's messaging access for a campsite.
 *
 * Messaging access is granted when a user successfully
 * enrolls in a campsite.
 *
 * @property campsiteId Unique identifier of the campsite.
 * @property ownerId Unique identifier of the campsite owner.
 * @property active Whether the user currently has permission
 * to send messages to the campsite owner.
 */
class MessagingAccess(
    val campsiteId: String = "",
    val ownerId: String = "",
    val active: Boolean = false
)
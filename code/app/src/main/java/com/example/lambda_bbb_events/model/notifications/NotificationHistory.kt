package com.example.lambda_bbb_events.model.notification

/**
 * Represents a notification saved in the user's history.
 */
data class NotificationHistory(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
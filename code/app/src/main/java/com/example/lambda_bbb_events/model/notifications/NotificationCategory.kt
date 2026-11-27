package com.example.lambda_bbb_events.model.notification

/**
 * Represents the categories of system notifications.
 */
enum class NotificationCategory(val title: String) {
    REJECTION("Rejection information"),
    LOTTERY("Lottery results"),
    CANCELLATION("Cancellations"),
    GENERAL("Notifications Sent")
}

/**
 * Determines how notifications are delivered.
 */
enum class NotificationMode(val title: String) {
    HIGH_PRIORITY("High priority"),
    IMMEDIATE("Immediate updates"),
    WEEKLY("Weekly updates"),
    MUTED("Muted")
}
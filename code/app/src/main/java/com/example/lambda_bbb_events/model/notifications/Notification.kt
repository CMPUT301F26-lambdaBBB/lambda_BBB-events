package com.example.lambda_bbb_events.model.notification

/*
 * Model classes for notifications.
 *
 * TODO Notification Actions
 */


///** TO BE WORKED ON LATER FOR FURTHER NOTIF FUNCTION
// * Buttons and functionality for notification.
// *
// * @property notifType text of action buttons of notification.
// * @property actionType Identifier for
// */
//data class NotificationFunction(
//    val notifType: String,
//    val actionType: String
//)

/**
 * A data class for notification structure .
 *
 * @property id Unique identification for the notification which is based on current time in milliseconds.
 * @property title Title of notification.
 * @property message Body text for notification.
 */
data class Notification(
    val id: Int = System.currentTimeMillis().toInt(),
    val title: String,
    val message: String
)
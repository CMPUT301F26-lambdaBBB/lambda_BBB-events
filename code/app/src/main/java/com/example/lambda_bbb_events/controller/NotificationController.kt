
package com.example.lambda_bbb_events.controller
/*
 * builds notifications and verifies if app and user preference both have notification enabled
 */

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lambda_bbb_events.MainActivity
import com.example.lambda_bbb_events.R
import com.example.lambda_bbb_events.model.notification.Notification
import com.example.lambda_bbb_events.model.repository.NotificationRepository

/**
 * Builds and displays notifications
 *
 * @property context Context used for system services
 * @property users Used to verify user notification preference
 */
class NotificationController(
    private val context: Context,
    private val users: UserController
) {
    private val repository = NotificationRepository(context)

    /**
     * Creates the notification channel and calls at app startup.
     */
    fun createNotificationChannel() {
        repository.createNotificationChannel()
    }

    /**
     * Shows a notification if the user and system both allow it.
     * @param notification The notification content.
     */
     fun showNotification(
        /* call notification with
        * controllers.notifications.showNotification(
        *    Notification(
        *       title = "Notification main title",
        *       message = "Notification message"
        *    )
        * )
        */
        notification: Notification,
    ) {
        // Reference:
        // Android Developers. (n.d.).
        // Create a notification. Android Developers.
        // https://developer.android.com/develop/ui/compose/notifications/create-notification

        // checks user notification preference in from user controller before sending
        if (users.currentUser?.notificationsEnabled != true) return
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, "APP_NOTIFICATIONS")
            .setSmallIcon(R.drawable.lb_small)
            .setContentTitle(notification.title) // only title and message to be set for now UPDATE LATER
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

         // future button functions will go here such as reply or lottery related function before this line
        repository.showNotification(notification.id, builder.build())
    }
}
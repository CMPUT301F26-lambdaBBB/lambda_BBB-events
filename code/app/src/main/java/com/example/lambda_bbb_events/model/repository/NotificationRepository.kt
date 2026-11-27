package com.example.lambda_bbb_events.model.repository

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/*
 * creates notification channel so controller can build upon it
 */

/**
 * Creates the notification channel and posts notifications.
 *
 * @property context Context used for system services.
 */
class NotificationRepository(
    private val context: Context
) {

    /**
     * Creates notification channel at startup.
     */
    fun createNotificationChannel() {
        // notification class is not in support library for api <26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "APP_NOTIFICATIONS",
                "Lambda Campsite Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification for lambda_BBB campsite"
            }

            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Posts a notification.
     *
     * @param id Notification id; reusing it replaces existing notification.
     * @param notification The built notification from controller.
     */
    // suppress warning in repository as checks happen in controller
    @SuppressLint("MissingPermission")
    fun showNotification(id: Int, notification: Notification) {
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
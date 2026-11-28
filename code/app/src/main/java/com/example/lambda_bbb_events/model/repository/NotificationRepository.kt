package com.example.lambda_bbb_events.model.repository

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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

        val defaultChannel = NotificationChannel(
            "APP_NOTIFICATIONS",
            "Lambda Campsite Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "General campsite notifications"
        }

        val highPriorityChannel = NotificationChannel(
            "HIGH_PRIORITY_NOTIFICATIONS",
            "High Priority Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Important campsite notifications"
        }

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        manager.createNotificationChannels(
            listOf(defaultChannel, highPriorityChannel)
        )
    }

    /**
     * Posts a notification.
     *
     * @param id Notification id; reusing it replaces existing notification.
     * @param notification The built notification from controller.
     */
    // suppress warning in repository as checks happen in controller
    /** Posts [notification] using [id] after the caller has checked notification permissions. */
    @SuppressLint("MissingPermission")
    fun showNotification(id: Int, notification: Notification) {
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}

package com.example.lambda_bbb_events.controller

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.lambda_bbb_events.MainActivity
import com.example.lambda_bbb_events.model.notification.Notification
import com.example.lambda_bbb_events.model.repository.NotificationRepository

class NotificationController(
    private val context: Context,
    private val users: UserController
) {
    private val repository = NotificationRepository(context)

    fun createNotificationChannel() {
        repository.createNotificationChannel()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun showNotification(
        // call show notification with controllers.notifications.showNotification(
        //    Notification(
        //        id = 1,
        //        title = "Notification main title",
        //        message = "Notification message"
        //    )
        //) for every notification need
        notification: Notification,
    ) {
        // checks user notification preference in from user controller before sending
        if (users.currentUser?.notificationsEnabled != true) return

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

        val builder = NotificationCompat.Builder(
            context,
            "LOTTERY_NOTIFICATIONS"
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        repository.showNotification(
            notification.id,
            builder.build()
        )
    }
}

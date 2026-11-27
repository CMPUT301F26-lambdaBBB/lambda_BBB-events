
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
import com.example.lambda_bbb_events.model.local.NotificationHistoryStore
import com.example.lambda_bbb_events.model.notification.NotificationHistory
import java.util.UUID
import com.example.lambda_bbb_events.model.local.NotificationCategoryStore
import com.example.lambda_bbb_events.model.notification.NotificationCategory
import com.example.lambda_bbb_events.model.notification.NotificationMode

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
    private val historyStore = NotificationHistoryStore(context)
    private val categoryStore = NotificationCategoryStore(context)

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
        notification: Notification,
        category: NotificationCategory = NotificationCategory.GENERAL
    ) {
        val user = users.currentUser ?: return

        historyStore.add(
            userId = user.deviceId,
            notification = NotificationHistory(
                id = UUID.randomUUID().toString(),
                title = notification.title,
                message = notification.message
            )
        )

        if (!user.notificationsEnabled) return

        val mode = categoryStore.getMode(
            user.deviceId,
            category
        )

        when (mode) {

            NotificationMode.MUTED -> return

            NotificationMode.WEEKLY -> {
                categoryStore.addPending(
                    user.deviceId,
                    category
                )
            }

            NotificationMode.IMMEDIATE,
            NotificationMode.HIGH_PRIORITY -> {
                postNotification(notification, mode)
            }
        }
    }

    private fun postNotification(
        notification: Notification,
        mode: NotificationMode
    ) {
        if (!NotificationManagerCompat.from(context)
                .areNotificationsEnabled()
        ) return

        val highPriority =
            mode == NotificationMode.HIGH_PRIORITY

        val channelId = if (highPriority) {
            "HIGH_PRIORITY_NOTIFICATIONS"
        } else {
            "APP_NOTIFICATIONS"
        }

        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(R.drawable.lb_small)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(
                if (highPriority) {
                    NotificationCompat.PRIORITY_HIGH
                } else {
                    NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        repository.showNotification(
            notification.id,
            builder.build()
        )
    }

    fun showWeeklySummaries() {

        val user = users.currentUser ?: return

        if (!user.notificationsEnabled) return

        if (!NotificationManagerCompat.from(context)
                .areNotificationsEnabled()
        ) return

        NotificationCategory.entries.forEach { category ->

            val count = categoryStore.getDueCount(
                user.deviceId,
                category
            )

            if (count > 0) {

                postNotification(
                    notification = Notification(
                        title = "Weekly ${category.title}",
                        message = "You have $count new updates."
                    ),
                    mode = NotificationMode.IMMEDIATE
                )

                categoryStore.clearPending(
                    user.deviceId,
                    category
                )
            }
        }
    }

    fun observeHistory(userId: String) =
        historyStore.observeHistory(userId)

    fun clearHistory() {

        val userId = users.currentUser?.deviceId ?: return

        historyStore.clear(userId)
    }

    fun getCategoryMode(
        userId: String,
        category: NotificationCategory
    ): NotificationMode {
        return categoryStore.getMode(userId, category)
    }

    fun setCategoryMode(
        category: NotificationCategory,
        mode: NotificationMode
    ) {
        val userId = users.currentUser?.deviceId ?: return

        categoryStore.setMode(
            userId,
            category,
            mode
        )
    }
}
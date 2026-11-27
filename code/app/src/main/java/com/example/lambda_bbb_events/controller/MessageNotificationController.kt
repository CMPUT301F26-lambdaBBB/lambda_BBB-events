package com.example.lambda_bbb_events.controller

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.lambda_bbb_events.MainActivity
import com.example.lambda_bbb_events.R
import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.Message
import com.example.lambda_bbb_events.model.repository.NotificationPreferenceRepository

/**
 * Manages Android notifications for incoming chat messages.
 *
 * Respects global notification settings and individual
 * contact notification preferences.
 *
 * @param userController Controller for the current user.
 */
class MessageNotificationController(
    private val userController: UserController
) {

    private val channelId = "chat_messages"

    /**
     * Determines whether an incoming message should
     * generate a notification.
     *
     * Only the message's recipient is notified, never its sender,
     * and not while the sender is muted.
     *
     * @param conversation Conversation containing the message.
     * @param message Incoming message.
     * @return True if notifications are permitted.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Changed with the help of this AI tool.
     * Prompts:
     * - "Now on the notification page as well. Please also remove the profile duplication bug.
     *   Also please fix the way the admins work since it seems all admins share an account. One
     *   more thing is to fix the notification page so that the spacing looks right."
     * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
     *   chats per admin".
     */
    suspend fun shouldNotify(
        conversation: Conversation,
        message: Message
    ): Boolean {

        val user = userController.currentUser ?: return false

        // Check global notification preference.
        if (!user.notificationsEnabled) return false

        // Ignore deleted messages.
        if (message.deleted) return false

        // Prevent notifications for messages sent by the user.
        if (message.senderId == user.deviceId) return false

        // Only notify the intended recipient.
        if (message.recipientId != user.deviceId) return false

        // Check individual contact notification preference.
        val repository =
            NotificationPreferenceRepository(user.deviceId)

        val preference = repository.get(message.senderId)

        return preference?.isMuted() != true
    }

    /**
     * Creates the Android notification channel.
     *
     * @param context Android application context.
     */
    private fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Chat Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.description =
                "Notifications for incoming chat messages"

            val manager = context.getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a notification for an incoming message
     * if the recipient's preferences allow it.
     *
     * @param context Android application context.
     * @param conversation Conversation containing the message.
     * @param message Incoming message.
     */
    suspend fun showNotification(
        context: Context,
        conversation: Conversation,
        message: Message
    ) {

        // Check user notification preferences.
        if (!shouldNotify(conversation, message)) return

        // Check Android notification permission.
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Check Android system notification settings.
        if (!NotificationManagerCompat.from(context)
                .areNotificationsEnabled()
        ) {
            return
        }

        createNotificationChannel(context)

        // Open the application when the notification is tapped.
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
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification.
        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(R.drawable.lb_small)
            .setContentTitle("New message")
            .setContentText("You have received a new message.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Display the notification using a unique message tag.
        NotificationManagerCompat.from(context).notify(
            "${conversation.conversationId}/${message.messageId}",
            0,
            notification
        )
    }
}
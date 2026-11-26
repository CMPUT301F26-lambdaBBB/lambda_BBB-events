package com.example.lambda_bbb_events.controller

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
     */
    suspend fun shouldNotify(
        conversation: Conversation,
        message: Message
    ): Boolean {

        val user = userController.currentUser ?: return false

        if (!user.notificationsEnabled) return false

        if (message.senderId == user.deviceId) return false

        if (message.recipientId != user.deviceId) return false

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

        if (!shouldNotify(conversation, message)) return

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("New message")
            .setContentText("You have received a new message.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            "${conversation.conversationId}/${message.messageId}",
            0,
            notification
        )
    }
}
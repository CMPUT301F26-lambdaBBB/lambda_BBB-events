package com.example.lambda_bbb_events.model.services

import android.util.Log
import com.example.lambda_bbb_events.model.notification.Notification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Receives incoming Firebase Cloud Messaging messages
 * and handles registration token updates.
 */
class FirebaseNotificationService : FirebaseMessagingService() {

    companion object {

        private val _incomingNotifications =
            MutableSharedFlow<Notification>(
                extraBufferCapacity = 16
            )

        val incomingNotifications =
            _incomingNotifications.asSharedFlow()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Campsite Notification"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: return

        val notification = Notification(
            title = title,
            message = body
        )

        _incomingNotifications.tryEmit(notification)

        Log.d("FCM", "Notification received")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM", "Token refreshed")
    }
}
package com.example.lambda_bbb_events

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Written using Claude.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {

        // get device ID
        val deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        // save token
        FcmTokenRegistrar.save(deviceId, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {

        // get message data
        val data = message.data

        val conversationId = data["conversationId"]
        val messageId = data["messageId"]

        // make sure both IDs exist
        if (conversationId == null) {
            return
        }

        if (messageId == null) {
            return
        }

        // Check if notification permission is required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permission != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        // Create a notification channel for Android 8.0 and newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.description = "Notifications for incoming chat messages"

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)

        notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_email)
        notificationBuilder.setContentTitle("New message")
        notificationBuilder.setContentText("You have received a new message.")
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        notificationBuilder.setAutoCancel(true)

        val notification = notificationBuilder.build()

        // Display the notification
        // Using the same tag and ID prevents duplicate notifications
        val notificationManager = NotificationManagerCompat.from(this)

        val notificationTag = "$conversationId/$messageId"

        notificationManager.notify(notificationTag, 0, notification)
    }

    private companion object {
        const val CHANNEL_ID = "chat_messages"
    }
}


// Saves this device's FCM token in Firestore. Written with Claude.
object FcmTokenRegistrar {

    fun register(deviceId: String) {

        // Get the Firebase Cloud Messaging token
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->

                Log.d("FCM", "token=$token")

                // Save the token in Firestore
                save(deviceId, token)
            }
    }

    fun save(deviceId: String, token: String) {

        // Get the user's document in Firestore
        val userDocument = Firebase.firestore
            .collection("users")
            .document(deviceId)

        // Create a map containing the token
        val userData = mapOf(
            "fcmTokens" to FieldValue.arrayUnion(token)
        )

        // Save the token without deleting existing user data
        userDocument.set(userData, SetOptions.merge())
            .addOnFailureListener { error ->

                Log.w("FCM", "Token save failed", error)
            }
    }
}
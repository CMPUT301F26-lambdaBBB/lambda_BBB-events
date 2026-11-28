package com.example.lambda_bbb_events.model.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.example.lambda_bbb_events.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

/**
 * Retrieves the Firebase Cloud Messaging registration token
 * for the current device.
 */
object FcmTokenRegistrar {
    /**
     * Fetches the current token and saves it for [deviceId].
     *
     * @param deviceId The ID of the current device.
     */
    fun register(deviceId: String) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> save(deviceId, token) }
            .addOnFailureListener { Log.e("FCM", "Failed to get FCM token", it) }
    }

    /**
     * Writes the token to fcmTokens/{deviceId}. Also called when the token refreshes.
     *
     * @param deviceId The ID of the current device.
     * @param token The FCM token.
     */
    fun save(deviceId: String, token: String) {
        Firebase.firestore.collection("fcmTokens").document(deviceId)
            .set(mapOf("token" to token, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { Log.d("FCM", "Token saved for $deviceId") }
            .addOnFailureListener { Log.e("FCM", "Failed to save token", it) }
    }
}
package com.example.lambda_bbb_events.model.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.example.lambda_bbb_events.BuildConfig
import com.google.firebase.FirebaseApp

/**
 * Retrieves the Firebase Cloud Messaging registration token
 * for the current device.
 */
object FcmTokenRegistrar {

    /**
     * Retrieves the FCM token for the specified device.
     *
     * @param deviceId Unique identifier of the device.
     */
    fun register(deviceId: String) {

        val options = FirebaseApp.getInstance().options

        Log.d("FCM", "Project ID: ${options.projectId}")
        Log.d("FCM", "Sender ID: ${options.gcmSenderId}")
        Log.d("FCM", "Application ID: ${options.applicationId}")

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->

                Log.d("FCM", "Device: $deviceId")
                Log.d("FCM", "Token retrieved successfully")

                if (BuildConfig.DEBUG) {
                    Log.d("FCM", "Token: $token")
                }
            }
            .addOnFailureListener { exception ->

                Log.e(
                    "FCM",
                    "Failed to retrieve FCM token",
                    exception
                )
            }
    }
}
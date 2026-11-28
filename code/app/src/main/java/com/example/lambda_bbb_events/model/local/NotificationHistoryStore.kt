package com.example.lambda_bbb_events.model.local

import android.content.Context
import com.example.lambda_bbb_events.model.notification.NotificationHistory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Stores notification history locally for each user.
 */
class NotificationHistoryStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        "notification_history",
        Context.MODE_PRIVATE
    )

    /**
     * Retrieves saved notifications for a user.
     */
    fun getHistory(userId: String): List<NotificationHistory> {

        val data = preferences.getString(userId, "[]")
            ?: "[]"

        val array = JSONArray(data)

        return List(array.length()) { index ->

            val item = array.getJSONObject(index)

            NotificationHistory(
                id = item.getString("id"),
                title = item.getString("title"),
                message = item.getString("message"),
                timestamp = item.getLong("timestamp")
            )
        }
    }

    /**
     * Saves a new notification.
     */
    fun add(
        userId: String,
        notification: NotificationHistory
    ) {

        val history = (
                listOf(notification) + getHistory(userId)
                ).take(100)

        val array = JSONArray()

        history.forEach { item ->

            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("message", item.message)
                    put("timestamp", item.timestamp)
                }
            )
        }

        preferences.edit()
            .putString(userId, array.toString())
            .apply()
    }

    /**
     * Observes changes to notification history.
     */
    fun observeHistory(userId: String) = callbackFlow {

        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                    _, key ->

                if (key == userId) {
                    trySend(getHistory(userId))
                }
            }

        preferences.registerOnSharedPreferenceChangeListener(
            listener
        )

        trySend(getHistory(userId))

        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(
                listener
            )
        }
    }

    /**
     * Clears the notification history for a user.
     */
    fun clear(userId: String) {

        preferences.edit()
            .remove(userId)
            .apply()
    }
}
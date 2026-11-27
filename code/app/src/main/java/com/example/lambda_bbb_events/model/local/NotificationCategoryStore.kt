package com.example.lambda_bbb_events.model.local

import android.content.Context
import com.example.lambda_bbb_events.model.notification.NotificationCategory
import com.example.lambda_bbb_events.model.notification.NotificationMode
import java.util.concurrent.TimeUnit

/**
 * Stores notification category preferences for each user.
 */
class NotificationCategoryStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        "notification_categories",
        Context.MODE_PRIVATE
    )

    private fun key(
        userId: String,
        category: NotificationCategory
    ): String {
        return "${userId}_${category.name}"
    }

    /**
     * Retrieves the notification mode for a category.
     */
    fun getMode(
        userId: String,
        category: NotificationCategory
    ): NotificationMode {

        val defaultMode =
            if (category == NotificationCategory.REJECTION) {
                NotificationMode.HIGH_PRIORITY
            } else {
                NotificationMode.IMMEDIATE
            }

        val savedMode = preferences.getString(
            key(userId, category),
            defaultMode.name
        )

        return NotificationMode.entries.firstOrNull {
            it.name == savedMode
        } ?: defaultMode
    }

    /**
     * Updates a notification category preference.
     */
    fun setMode(
        userId: String,
        category: NotificationCategory,
        mode: NotificationMode
    ) {
        if (getMode(userId, category) == mode) return

        val key = key(userId, category)

        preferences.edit()
            .putString(key, mode.name)
            .remove("${key}_count")
            .remove("${key}_startedAt")
            .apply()
    }

    /**
     * Records a notification for a weekly summary.
     */
    fun addPending(
        userId: String,
        category: NotificationCategory
    ) {
        val key = key(userId, category)

        val count = preferences.getInt(
            "${key}_count",
            0
        )

        val editor = preferences.edit()

        if (count == 0) {
            editor.putLong(
                "${key}_startedAt",
                System.currentTimeMillis()
            )
        }

        editor.putInt("${key}_count", count + 1)
        editor.apply()
    }

    /**
     * Returns the pending count if a weekly summary is due.
     */
    fun getDueCount(
        userId: String,
        category: NotificationCategory
    ): Int {

        if (getMode(userId, category) != NotificationMode.WEEKLY) {
            return 0
        }

        val key = key(userId, category)

        val startedAt = preferences.getLong(
            "${key}_startedAt",
            0L
        )

        val elapsed = System.currentTimeMillis() - startedAt

        if (
            startedAt == 0L ||
            elapsed < TimeUnit.DAYS.toMillis(7)
        ) {
            return 0
        }

        return preferences.getInt("${key}_count", 0)
    }

    /**
     * Clears pending notifications after displaying a summary.
     */
    fun clearPending(
        userId: String,
        category: NotificationCategory
    ) {
        val key = key(userId, category)

        preferences.edit()
            .remove("${key}_count")
            .remove("${key}_startedAt")
            .apply()
    }
}
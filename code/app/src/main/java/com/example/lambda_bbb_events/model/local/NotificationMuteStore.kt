package com.example.lambda_bbb_events.model.local

import android.content.Context
import androidx.core.content.edit

/*
 * Remembers which notifications are muted on this device.
 */

/**
 * Saves which people and categories are muted, so they stay muted after the app restarts.
 */
interface NotificationMuteStore {
    /**
     * Loads everything that was muted.
     *
     * @return The time each mute ends, in milliseconds since 1970, by the muted ID.
     */
    fun load(): Map<String, Long>

    /**
     * Saves that something is muted.
     *
     * @param id The person or category to mute.
     * @param until When the mute ends, in milliseconds since 1970.
     */
    fun save(id: String, until: Long)

    /**
     * Removes a saved mute.
     *
     * @param id The person or category that is no longer muted.
     */
    fun remove(id: String)
}

/** Keeps mutes in memory only. Used in tests, where nothing is saved. */
class InMemoryNotificationMuteStore : NotificationMuteStore {
    private val mutes = mutableMapOf<String, Long>()

    override fun load(): Map<String, Long> = mutes.toMap()

    override fun save(id: String, until: Long) {
        mutes[id] = until
    }

    override fun remove(id: String) {
        mutes.remove(id)
    }
}

/**
 * Saves mutes in the app's shared preferences.
 *
 * @param context Used to open the shared preferences.
 * @param name The preferences file to use. Tests use their own.
 */
class SharedPreferencesNotificationMuteStore(
    context: Context,
    name: String = "notification_mutes"
) : NotificationMuteStore {
    private val preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    override fun load(): Map<String, Long> =
        preferences.all.mapNotNull { (id, until) -> (until as? Long)?.let { id to it } }.toMap()

    override fun save(id: String, until: Long) {
        preferences.edit { putLong(id, until) }
    }

    override fun remove(id: String) {
        preferences.edit { remove(id) }
    }
}

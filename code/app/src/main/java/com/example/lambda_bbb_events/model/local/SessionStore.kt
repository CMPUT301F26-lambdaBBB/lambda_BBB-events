package com.example.lambda_bbb_events.model.local

import android.content.Context

/*
 * Remembers whether the user has logged out on this device.
 */

/**
 * Saves whether the user has logged out, so they stay logged out after the app restarts.
 *
 * Accounts belong to the device they were made on, so logging out only hides the account
 * until the user logs back in on the same device.
 */
interface SessionStore {
    /** Whether the user has logged out on this device. */
    var loggedOut: Boolean
}

/**
 * Keeps the logged out state in memory only. Used in tests, where nothing is saved.
 *
 * @param loggedOut Whether the user starts logged out.
 */
class InMemorySessionStore(override var loggedOut: Boolean = false) : SessionStore

/**
 * Saves the logged out state in the app's shared preferences.
 *
 * @param context Used to open the shared preferences.
 */
class SharedPreferencesSessionStore(context: Context) : SessionStore {
    private val preferences =
        context.getSharedPreferences("session", Context.MODE_PRIVATE)

    override var loggedOut: Boolean
        get() = preferences.getBoolean(KEY_LOGGED_OUT, false)
        set(value) {
            preferences.edit().putBoolean(KEY_LOGGED_OUT, value).apply()
        }

    private companion object {
        const val KEY_LOGGED_OUT = "loggedOut"
    }
}

package com.example.lambda_bbb_events.model

/**
 * Represents every user that uses the app.
 *
 * @property deviceId The unique ID of the user's device.
 * @property name The user's real name, may be visible to other users.
 * @property email The user's email address, not visible to others.
 * @property phone The user's phone number, not visible to others.
 * @property profilePicture Optional profile picture, visible to other users. Defaults to null (empty).
 * @property notificationsEnabled Whether the user wants to receive notifications.
 */
abstract class Users (
    private val deviceId: String,
    var name: String,
    private var email: String,
    private var phone: String,
    var profilePicture: Image? = null, // allow user to have default or no profile picture
    private var notificationsEnabled: Boolean = true
) {
    /**
     * Turns notifications on or off for the user.
     *
     * @param allow `true` to receive notifications, `false` to opt out.
     */
    fun setAllowNotifications(allow: Boolean) {
        notificationsEnabled = allow
    }
}


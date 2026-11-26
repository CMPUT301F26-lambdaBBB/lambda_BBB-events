package com.example.lambda_bbb_events.model.message

import com.google.firebase.Timestamp

/**
 * Represents a user's notification preferences for a contact.
 *
 * Supports permanent muting, temporary muting,
 * and enabling notifications.
 *
 * @property contactId Device ID of the contact or ADMIN_SUPPORT.
 * @property muted Whether notifications are muted.
 * @property mutedUntil Time when temporary muting expires.
 * Null represents indefinite muting when muted is true.
 */
class NotificationPreference(
    val contactId: String = "",
    val muted: Boolean = false,
    val mutedUntil: Timestamp? = null
) {

    /**
     * Checks whether notifications are currently muted.
     *
     * @return True if notifications are muted indefinitely
     * or the temporary mute has not expired.
     */
    fun isMuted(): Boolean {
        if (!muted) return false

        return mutedUntil?.let {
            it.compareTo(Timestamp.now()) > 0
        } ?: true
    }
}
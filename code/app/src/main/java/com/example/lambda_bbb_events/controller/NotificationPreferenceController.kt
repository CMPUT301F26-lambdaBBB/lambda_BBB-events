package com.example.lambda_bbb_events.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lambda_bbb_events.model.message.NotificationPreference
import com.example.lambda_bbb_events.model.repository.NotificationPreferenceRepository

/**
 * Controls notification preferences for the current user.
 *
 * Manages temporary muting, indefinite muting,
 * and enabling notifications from individual contacts.
 *
 * @param userController Controller responsible for user operations.
 */
class NotificationPreferenceController(
    private val userController: UserController
) {

    private var repository by
    mutableStateOf<NotificationPreferenceRepository?>(null)

    /**
     * Cached notification preferences for the current user.
     */
    val preferences: List<NotificationPreference>
        get() = repository?.preferences ?: emptyList()

    /**
     * Starts synchronizing notification preferences
     * for the currently signed-in user.
     */
    fun startListening() {
        stopListening()

        val userId = userController.user.deviceId

        repository = NotificationPreferenceRepository(userId)

        repository?.startListening()
    }

    /**
     * Stops synchronizing notification preferences
     * and clears the current repository.
     */
    fun stopListening() {
        repository?.stopListening()

        repository = null
    }

    /**
     * Retrieves the notification preference for a contact.
     *
     * @param contactId Unique identifier of the contact.
     * @return Notification preference for the contact.
     */
    fun getPreference(contactId: String): NotificationPreference {
        return repository?.getPreference(contactId)
            ?: NotificationPreference(contactId = contactId)
    }

    /**
     * Checks whether notifications from a contact are muted.
     *
     * @param contactId Unique identifier of the contact.
     * @return True if notifications are currently muted.
     */
    fun isMuted(contactId: String): Boolean {
        return getPreference(contactId).isMuted()
    }

    /**
     * Mutes notifications from a contact indefinitely.
     *
     * @param contactId Unique identifier of the contact.
     */
    suspend fun mute(contactId: String) {
        val repo = repository
            ?: error("Notification preferences are not initialized")

        repo.mute(contactId)
    }

    /**
     * Temporarily mutes notifications from a contact.
     *
     * @param contactId Unique identifier of the contact.
     * @param hours Number of hours to mute notifications.
     */
    suspend fun muteForHours(
        contactId: String,
        hours: Int
    ) {
        val repo = repository
            ?: error("Notification preferences are not initialized")

        repo.muteForHours(contactId, hours)
    }

    /**
     * Enables notifications from a contact.
     *
     * @param contactId Unique identifier of the contact.
     */
    suspend fun unmute(contactId: String) {
        val repo = repository
            ?: error("Notification preferences are not initialized")

        repo.unmute(contactId)
    }
}
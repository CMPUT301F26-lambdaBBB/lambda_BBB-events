package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.message.NotificationPreference
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/**
 * Manages notification preferences associated with a user.
 *
 * Supports temporary muting, indefinite muting,
 * and enabling notifications from specific contacts.
 *
 * @param userId Device ID of the current user.
 * @param db Firestore database instance.
 */
class NotificationPreferenceRepository(
    userId: String,
    db: FirebaseFirestore = Firebase.firestore
) : Repository<NotificationPreference>(
    "notificationPreferences/$userId/contacts", db
) {

    /**
     * Retrieves the unique identifier of a notification preference.
     *
     * @param item Notification preference object.
     * @return Unique identifier of the contact.
     */
    override fun idOf(item: NotificationPreference): String =
        item.contactId

    /**
     * Converts a Firestore document into a notification preference.
     *
     * @param document Firestore document to convert.
     * @return Notification preference or null.
     */
    override fun fromDocument(
        document: DocumentSnapshot
    ): NotificationPreference? {
        if (!document.exists()) return null

        return NotificationPreference(
            contactId = document.id,
            muted = document.getBoolean("muted") ?: false,
            mutedUntil = document.getTimestamp("mutedUntil")
        )
    }

    /**
     * Converts a notification preference into Firestore data.
     *
     * @param item Notification preference to store.
     * @return Map containing the stored preference fields.
     */
    override fun toDocument(item: NotificationPreference): Any =
        mapOf(
            "contactId" to item.contactId,
            "muted" to item.muted,
            "mutedUntil" to item.mutedUntil
        )

    /**
     * All cached notification preferences.
     */
    val preferences: List<NotificationPreference>
        get() = cached

    /**
     * Retrieves a cached notification preference.
     *
     * Contacts without a saved preference are unmuted by default.
     *
     * @param contactId Unique identifier of the contact.
     * @return Notification preference for the contact.
     */
    fun getPreference(contactId: String): NotificationPreference {
        return getCached(contactId)
            ?: NotificationPreference(contactId = contactId)
    }

    /**
     * Checks whether notifications from a contact are currently muted.
     *
     * @param contactId Unique identifier of the contact.
     * @return True if notifications are muted.
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
        require(contactId.isNotBlank())

        val preference = NotificationPreference(
            contactId = contactId,
            muted = true,
            mutedUntil = null
        )

        update(preference)
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
        require(contactId.isNotBlank())
        require(hours > 0) {
            "Mute duration must be positive"
        }

        val now = Timestamp.now()

        val mutedUntil = Timestamp(
            now.seconds + hours * 3600L,
            now.nanoseconds
        )

        val preference = NotificationPreference(
            contactId = contactId,
            muted = true,
            mutedUntil = mutedUntil
        )

        update(preference)
    }

    /**
     * Enables notifications from a contact.
     *
     * @param contactId Unique identifier of the contact.
     */
    suspend fun unmute(contactId: String) {
        require(contactId.isNotBlank())

        val preference = NotificationPreference(
            contactId = contactId,
            muted = false,
            mutedUntil = null
        )

        update(preference)
    }
}
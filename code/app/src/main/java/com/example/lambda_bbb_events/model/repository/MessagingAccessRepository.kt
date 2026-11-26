package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.message.MessagingAccess
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/**
 * Manages messaging access records associated with a user.
 *
 * Synchronizes campsite messaging access with Firestore
 * and provides information about available contacts.
 *
 * @param userId Device ID of the user.
 * @param db Firestore database instance.
 */
class MessagingAccessRepository(
    userId: String,
    db: FirebaseFirestore = Firebase.firestore
) : Repository<MessagingAccess>(
    "messagingAccess/$userId/campsites", db
) {

    /**
     * Retrieves the unique identifier of a messaging access record.
     *
     * @param item Messaging access record.
     * @return Unique identifier of the associated campsite.
     */
    override fun idOf(item: MessagingAccess): String =
        item.campsiteId

    /**
     * Converts a Firestore document into a messaging access record.
     *
     * @param document Firestore document to convert.
     * @return Messaging access record or null.
     */
    override fun fromDocument(
        document: DocumentSnapshot
    ): MessagingAccess? =
        document.toObject(MessagingAccess::class.java)

    /**
     * All cached messaging access records.
     */
    val messagingAccess: List<MessagingAccess>
        get() = cached

    /**
     * Messaging access records associated with active enrollments.
     */
    val activeMessagingAccess: List<MessagingAccess>
        get() = cached.filter { it.active }

    /**
     * Grants messaging access after successful campsite enrollment.
     *
     * Creates a new access record or reactivates an existing record.
     *
     * @param campsiteId Unique identifier of the campsite.
     * @param ownerId Device ID of the campsite owner.
     */
    suspend fun grantAccess(
        campsiteId: String,
        ownerId: String
    ) {
        require(campsiteId.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        require(ownerId.isNotBlank()) {
            "Owner ID cannot be empty"
        }

        val access = MessagingAccess(
            campsiteId = campsiteId,
            ownerId = ownerId,
            active = true
        )

        add(access)
    }

    /**
     * Revokes owner messaging access for a campsite.
     *
     * Preserves the access record so the user can retain
     * conversation history and contact administrator support.
     *
     * @param campsiteId Unique identifier of the campsite.
     */
    suspend fun revokeAccess(campsiteId: String) {
        val existingAccess = get(campsiteId) ?: return

        val updatedAccess = MessagingAccess(
            campsiteId = existingAccess.campsiteId,
            ownerId = existingAccess.ownerId,
            active = false
        )

        update(updatedAccess)
    }

    /**
     * Retrieves the unique IDs of available campsite owners.
     *
     * @return List of campsite owner IDs without duplicates.
     */
    fun getAvailableOwnerIds(): List<String> {
        return activeMessagingAccess
            .map { it.ownerId }
            .filter { it.isNotBlank() }
            .distinct()
    }

    /**
     * Checks whether the user can message a campsite owner
     * based on cached messaging access records.
     *
     * @param campsiteId Unique identifier of the campsite.
     * @return True if owner messaging access is active.
     */
    fun canMessageOwner(campsiteId: String): Boolean {
        return cached.any {
            it.campsiteId == campsiteId && it.active
        }
    }

    /**
     * Checks whether the user can contact administrator support.
     *
     * @return True if the cached records indicate that the user
     * has previously enrolled in a campsite.
     */
    fun canMessageAdmin(): Boolean {
        return cached.isNotEmpty()
    }
}
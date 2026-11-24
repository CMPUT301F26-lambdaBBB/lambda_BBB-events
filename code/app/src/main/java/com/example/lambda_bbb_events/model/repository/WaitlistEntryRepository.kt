package com.example.lambda_bbb_events.model.repository

import android.util.Log
import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/*
 * Manages waitlist entries in Firestore using the repository pattern.
 *
 */


/**
 * Handles the data in the Firestore database for the [WaitlistEntry] class.
 */
class WaitlistEntryRepository(
    db: FirebaseFirestore = Firebase.firestore
) : Repository<WaitlistEntry>("waitlistEntries", db) {

    init {
        startListening()
    }

    override fun idOf(item: WaitlistEntry) = item.entryId

    override fun fromDocument(document: DocumentSnapshot): WaitlistEntry? =
        document.toObject(WaitlistEntry::class.java)

    override fun onListenError(error: FirebaseFirestoreException) {
        Log.e("WaitlistEntryRepo", "listener error", error)
    }

    /** Cached waitlist entries synchronized with Firestore. */
    val waitlistEntries: List<WaitlistEntry>
        get() = cached

    /**
     * Adds a new waitlist entry to the Firestore database.
     *
     * @param entry The waitlist entry to add.
     */
    suspend fun addWaitlistEntry(entry: WaitlistEntry) = add(entry)

    /**
     * Deletes a waitlist entry from the Firestore database and removes it from its lottery.
     *
     * @param item The waitlist entry to be deleted.
     */
    override suspend fun delete(item: WaitlistEntry) {
        val entryRef = collectionRef.document(item.entryId)
        val batch = db.batch()
        batch.delete(entryRef)

        // an entry without a lottery has no lottery to remove it from
        if (item.lotteryId.isNotBlank()) {
            batch.update(
                db.collection("lotteries").document(item.lotteryId),
                "entryIds",
                FieldValue.arrayRemove(item.entryId)
            )
        }

        batch.commit().await()
        removeFromCache(item.entryId)
    }

    /**
     * Deletes the waitlist entry with the given ID, also removing it from its lottery
     * if the entry can be found.
     *
     * @param id The `entryId` of the waitlist entry to be deleted.
     */
    override suspend fun delete(id: String) {
        val entry = getCached(id) ?: get(id)
        if (entry != null) {
            delete(entry)
        } else {
            super.delete(id)
        }
    }

    /**
     * Deletes a waitlist entry from the Firestore database.
     *
     * @param entry The waitlist entry to be deleted.
     */
    suspend fun deleteWaitlistEntry(entry: WaitlistEntry) = delete(entry)


    /**
     * Saves a waitlist entry to Firestore, creating it if absent.
     *
     * @param updatedEntry The entry data to save.
     */
    suspend fun updateWaitlistEntry(updatedEntry: WaitlistEntry) = update(updatedEntry)

    /**
     * Gets a [WaitlistEntry] object from the database given its entryId.
     *
     * @param entryId The `entryId` to search for.
     * @return The [WaitlistEntry] object with the specified entryId if it exists. `null` otherwise.
     */
    suspend fun getWaitlistEntry(entryId: String): WaitlistEntry? = get(entryId)

    /**
     * Gets all the [WaitlistEntry] objects of a specified [Lottery].
     *
     * @param lotteryId The lottery ID to get entries from.
     * @return The list of entries in the lottery.
     */
    fun getEntries(lotteryId: String): List<WaitlistEntry> {
        val result = mutableListOf<WaitlistEntry>()

        for (entry in cached) {
            if (entry.lotteryId == lotteryId) {
                result.add(entry)
            }
        }

        return result
    }

    /**
     * Gets all [WaitlistEntry] of a specific entrant.
     *
     * @param entrantId The entrant device ID to get entries for.
     * @return The list of entries made by the entrant.
     */
    fun getEntriesForEntrant(entrantId: String): List<WaitlistEntry> {
        val result = mutableListOf<WaitlistEntry>()

        for (entry in cached) {
            if (entry.entrantId == entrantId) {
                result.add(entry)
            }
        }

        return result
    }

    /**
     * Changes the status of a waitlist entry.
     *
     * @param waitlistEntry The [WaitlistEntry] to update.
     * @param newStatus The new [WaitlistStatus] of the entry.
     */
    suspend fun updateEntryStatus(waitlistEntry: WaitlistEntry, newStatus: WaitlistStatus) {
        collectionRef.document(waitlistEntry.entryId).update("status", newStatus.name).await()
    }
}
package com.example.lambda_bbb_events.model.repository

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.model.WaitlistStatus
import com.example.lambda_bbb_events.model.roles.Entrant
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Handles the data in the Firestore database for the [WaitlistEntry] class.
 */
class WaitlistEntryRepository {
    private val db = Firebase.firestore
    private val entriesRef = db.collection("waitlistEntries")

    private val _waitlistEntries = mutableStateListOf<WaitlistEntry>()

    init {
        entriesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("WaitlistEntryRepo", "listener error", error)
                return@addSnapshotListener
            }

            _waitlistEntries.clear()

            snapshot?.documents?.forEach { document ->
                val entry = document.toObject(WaitlistEntry::class.java)
                if (entry != null) {
                    _waitlistEntries.add(entry)
                }
            }
        }
    }

    val waitlistEntries: List<WaitlistEntry>
        get() = _waitlistEntries

    /**
     * Adds a new waitlist entry to the Firestore database.
     *
     * @param entry The waitlist entry to add.
     */
    suspend fun addWaitlistEntry(entry: WaitlistEntry) {
        entriesRef.document(entry.entryId).set(entry).await()
    }

    /**
     * Deletes a waitlist entry from the Firestore database.
     *
     * @param entry The waitlist entry to be deleted.
     */
    suspend fun deleteWaitlistEntry(entry: WaitlistEntry) {
        entriesRef.document(entry.entryId).delete().await()
    }

    /**
     * Overwrites an existing waitlist entry in the Firestore database with updated data.
     *
     * @param updatedEntry The waitlist entry to be updated. Must have the same `waitlistentryId`
     * as the original document.
     */
    suspend fun updateWaitlistEntry(updatedEntry: WaitlistEntry) {
        entriesRef.document(updatedEntry.entryId).set(updatedEntry).await()
    }

    /**
     * Gets a [WaitlistEntry] object from the database given its entryId.
     *
     * @param entryId The `entryId` to search for.
     * @return The [WaitlistEntry] object with the specified entryId if it exists. `null` otherwise.
     */
    suspend fun getWaitlistEntry(entryId: String): WaitlistEntry? {
        if (entryId.isBlank()) {
            return null
        }
        return entriesRef.document(entryId).get().await().toObject(WaitlistEntry::class.java)
    }

    /**
     * Gets all the [WaitlistEntry] objects of a specified [Lottery].
     *
     * @param lottery The lottery to get entries from.
     * @return The list of entries in the lottery.
     */
    fun getEntries(lottery: Lottery): List<WaitlistEntry> {
        val result = mutableListOf<WaitlistEntry>()

        for (entry in _waitlistEntries) {
            if (entry.lotteryId == lottery.lotteryId) {
                result.add(entry)
            }
        }

        return result
    }

    /**
     * Gets all [WaitlistEntry] of a specific entrant.
     *
     * @param entrant The entrant to get entries for.
     * @return The list of entries made by the entrant.
     */
    fun getEntriesForEntrant(entrant: Entrant): List<WaitlistEntry> {
        val result = mutableListOf<WaitlistEntry>()

        for (entry in _waitlistEntries) {
            if (entry.entrantId == entrant.deviceId) {
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
        entriesRef.document(waitlistEntry.entryId).update("status", newStatus.name).await()
    }
}
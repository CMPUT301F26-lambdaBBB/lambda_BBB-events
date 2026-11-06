package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.model.WaitlistStatus
import com.example.lambda_bbb_events.model.roles.Entrant
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/*
 * Manages lottery and waitlist data in Firestore.
 * TODO: Remove waitlist entry records when entries are deleted.
 */


/**
 * Handles the data in the Firestore database for the [Lottery] class.
 */
class LotteryRepository {
    private val db = Firebase.firestore
    private val lotteriesRef = db.collection("lotteries")

    private val _lotteries = mutableStateListOf<Lottery>()

    init {
        lotteriesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _lotteries.clear()

            snapshot?.documents?.forEach { document ->
                val lottery = document.toObject(Lottery::class.java)
                if (lottery != null) {
                    _lotteries.add(lottery)
                }
            }
        }
    }

    /** Cached lotteries synchronized with Firestore. */
    val lotteries: List<Lottery>
        get() = _lotteries

    /**
     * Adds a new lottery to the Firestore database.
     *
     * @param lottery The lottery to add.
     */
    suspend fun addLottery(lottery: Lottery) {
        lotteriesRef.document(lottery.lotteryId).set(lottery).await()
    }

    /**
     * Deletes a lottery from the Firestore database.
     *
     * @param lottery The lottery to be deleted.
     */
    suspend fun deleteLottery(lottery: Lottery) {
        lotteriesRef.document(lottery.lotteryId).delete().await()
    }


    /**
     * Saves a lottery to Firestore, creating it if absent.
     *
     * @param updatedLottery The lottery data to save.
     */
    suspend fun updateLottery(updatedLottery: Lottery) {
        lotteriesRef.document(updatedLottery.lotteryId).set(updatedLottery).await()
    }

    /**
     * Gets a [Lottery] object from the database given its lotteryId.
     *
     * @param lotteryId The `lotteryId` to search for.
     * @return The [Lottery] object with the specified lotteryId if it exists. `null` otherwise.
     */
    suspend fun getLottery(lotteryId: String): Lottery? {
        // there's probably a faster way to do this but i dont understand all the stuff i read in google
        // someone pls rewrite this, this is temporary
        for (lottery in _lotteries) {
            if (lottery.lotteryId == lotteryId) {
                return lottery
            }
        }
        return null
    }

    /**
     * Adds a waitlist entry to a specified lottery if there is enough space left.
     *
     * @param lottery The [Lottery] to add the [waitlistEntry] to.
     * @param waitlistEntry The [WaitlistEntry] to add.
     * @return `true` if the entry was added successfully, `false` if the waitlist
     * is full or the entry is already on it.
     */
    suspend fun addWaitlistToLottery(lottery: Lottery, waitlistEntry: WaitlistEntry): Boolean {
        // check if the waitlist is already full
        if (lottery.entryIds.size >= lottery.waitlistCapacity) { // > should be impos, but include strict bound incase
            return false
        }

        // check if this entry is already on the waitlist
        if (waitlistEntry.entryId in lottery.entryIds) {
            return false
        }

        // valid conditions, union the old entryIds with the new entryId (add)
        lotteriesRef.document(lottery.lotteryId)
            .update("entryIds", FieldValue.arrayUnion(waitlistEntry.entryId)).await()

        return true
    }

    /**
     * Removes a waitlist entry from a specified lottery. If the waitlist entry is
     * not in the lottery, the function does nothing.
     *
     * @param lottery The [Lottery] to remove the waitlist from.
     * @param waitlistEntry The [WaitlistEntry] to remove.
     */
    suspend fun removeWaitlistFromLottery(lottery: Lottery, waitlistEntry: WaitlistEntry) {
        // check if this entry is on the waitlist
        if (waitlistEntry.entryId in lottery.entryIds) {
            lotteriesRef.document(lottery.lotteryId)
                .update("entryIds", FieldValue.arrayRemove(waitlistEntry.entryId)).await()
        }
    }

    /**
     * Gets all [Lottery] that is created by a specific campsite.
     *
     * @param campsiteId The `campsiteId` to search for.
     * @return The list of lotteries that include the campsite. An empty list is
     * returned if there are none.
     */
    fun getLotteriesForCampsite(campsiteId: String): List<Lottery> {
        val result = mutableListOf<Lottery>()

        for (lottery in _lotteries) {
            if (campsiteId in lottery.campsiteIds) {
                result.add(lottery)
            }
        }

        return result
    }

    /**
     * Registers an entrant for a lottery and adds their entry to the Firestore database.
     *
     * Checks if registration is open, the entrant has already joined, and the waitlist
     * has available space. Uses a Firestore transaction to prevent duplicate entries.
     *
     * @param lotteryId The `lotteryId` of the lottery to join.
     * @param entrantId The ID of the entrant joining the lottery.
     * @param numberOfGuests The number of guests included in the registration.
     * @param onResult Callback that returns `true` if registration was successful,
     * `false` otherwise, along with a message describing the result.
     */
    fun joinLottery(
        lotteryId: String,
        entrantId: String,
        numberOfGuests: Int,
        onResult: (Boolean, String) -> Unit
    ){
        if (lotteryId.isBlank() || entrantId.isBlank() || numberOfGuests < 1) {
            onResult(false, "Invalid registration information")
            return
        }

        val lotteryRef = lotteriesRef.document(lotteryId)
        val entryId = "${lotteryId}_${entrantId}"
        val entryRef = db.collection("waitlistEntries").document(entryId)

        db.runTransaction { transaction ->
            val lotterySnapshot = transaction.get(lotteryRef)
            val entrySnapshot = transaction.get(entryRef)
            val lottery = lotterySnapshot.toObject(Lottery::class.java) ?: throw IllegalStateException("Lottery not found")
            if (!lottery.isRegistrationOpen()) {
                throw IllegalStateException("Registration is closed")
            }

            if (entrySnapshot.exists() || entryId in lottery.entryIds) {
                throw IllegalStateException("Already joined this lottery")
            }

            if (lottery.isFull()) {
                throw IllegalStateException("Waitlist is full")
            }

            val waitlistEntry = WaitlistEntry(
                entryId = entryId,
                entrantId = entrantId,
                lotteryId = lotteryId,
                numberOfGuests = numberOfGuests,
                status = WaitlistStatus.IN_WAITLIST
            )

            transaction.set(entryRef, waitlistEntry)

            transaction.update(
                lotteryRef,
                "entryIds",
                FieldValue.arrayUnion(entryId)
            )
            true
        }.addOnSuccessListener {
            onResult(true, "Successfully joined lottery")
        }.addOnFailureListener { exception ->
            onResult(false, exception.message ?: "Registration failed")
        }
    }
}
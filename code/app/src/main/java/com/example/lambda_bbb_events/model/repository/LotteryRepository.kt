package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.model.WaitlistStatus
import com.example.lambda_bbb_events.model.roles.Entrant
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

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

    val lotteries: List<Lottery>
        get() = _lotteries

    /**
     * Adds a new lottery to the Firestore database.
     *
     * @param lottery The lottery to add.
     */
    fun addLottery(lottery: Lottery) {
        lotteriesRef.document(lottery.lotteryId).set(lottery)
    }

    /**
     * Deletes a lottery from the Firestore database.
     *
     * @param lottery The lottery to be deleted.
     */
    fun deleteLottery(lottery: Lottery) {
        lotteriesRef.document(lottery.lotteryId).delete()
    }

    /**
     * Overwrites an existing lottery in the Firestore database with updated data.
     *
     * @param updatedLottery The lottery to be updated. Must have the same `lotteryId`
     * as the original document.
     */
    fun updateLottery(updatedLottery: Lottery) {
        lotteriesRef.document(updatedLottery.lotteryId).set(updatedLottery)
    }

    /**
     * Gets a [Lottery] object from the database given its lotteryId.
     *
     * @param lotteryId The `lotteryId` to search for.
     * @return The [Lottery] object with the specified lotteryId if it exists. `null` otherwise.
     */
    fun getLottery(lotteryId: String): Lottery? {
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
    fun addWaitlistToLottery(lottery: Lottery, waitlistEntry: WaitlistEntry): Boolean {
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
            .update("entryIds", FieldValue.arrayUnion(waitlistEntry.entryId))

        return true
    }

    /**
     * Removes a waitlist entry from a specified lottery. If the waitlist entry is
     * not in the lottery, the function does nothing.
     *
     * @param lottery The [Lottery] to remove the waitlist from.
     * @param waitlistEntry The [WaitlistEntry] to remove.
     */
    fun removeWaitlistFromLottery(lottery: Lottery, waitlistEntry: WaitlistEntry) {
        // check if this entry is on the waitlist
        if (waitlistEntry.entryId in lottery.entryIds) {
            lotteriesRef.document(lottery.lotteryId)
                .update("entryIds", FieldValue.arrayRemove(waitlistEntry.entryId))
        }
    }
}
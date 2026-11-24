package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.LotteryStatus
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.model.message.Message
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Date

/*
 * Manages lottery and waitlist data in Firestore.
 * TODO: Remove waitlist entry records when entries are deleted.
 */


/**
 * Handles the data in the Firestore database for the [Lottery] class.
 */
class LotteryRepository(
    db: FirebaseFirestore = Firebase.firestore
) : Repository<Lottery>("lotteries", db) {

    init {
        startListening()
    }

    override fun idOf(item: Lottery) = item.lotteryId

    override fun fromDocument(document: DocumentSnapshot): Lottery? =
        document.toObject(Lottery::class.java)

    /** Cached lotteries synchronized with Firestore. */
    val lotteries: List<Lottery>
        get() = cached

    /**
     * Adds a new lottery to the Firestore database.
     *
     * @param lottery The lottery to add.
     */
    suspend fun addLottery(lottery: Lottery) = add(lottery)

    /**
     * Deletes a lottery from the Firestore database, along with every waitlist entry
     * for it and every entrant's reference to those entries.
     *
     * @param id The `lotteryId` of the lottery to be deleted.
     */
    override suspend fun delete(id: String) {
        deleteAndNotifyWinners(id, makeNotice = null)
    }

    /**
     * Deletes a lottery like [delete], and sends a message to each of its winners: the
     * entries that won ([WaitlistStatus.WON]) or accepted their win
     * ([WaitlistStatus.ACCEPTED]). The messages are written in the same batch as the
     * deletion, so either the lottery is deleted and every winner is told, or nothing changes.
     *
     * @param id The `lotteryId` of the lottery to be deleted.
     * @param makeNotice Makes the message for a winner's entry, or `null` to send none.
     * @return The number of winners sent a message.
     */
    suspend fun deleteAndNotifyWinners(
        id: String,
        makeNotice: ((WaitlistEntry) -> Message)?
    ): Int {
        val batch = db.batch()
        val notified = queueLotteryDeletion(batch, id, makeNotice)
        batch.commit().await()
        removeFromCache(id)
        return notified
    }

    private suspend fun queueLotteryDeletion(
        batch: WriteBatch,
        id: String,
        makeNotice: ((WaitlistEntry) -> Message)?
    ): Int {
        var notified = 0

        val entriesSnapshot = db.collection("waitlistEntries")
            .whereEqualTo("lotteryId", id)
            .get()
            .await()

        for (entryDocument in entriesSnapshot.documents) {
            val usersSnapshot = db.collection("users")
                .whereArrayContains("waitlistEntryIds", entryDocument.id)
                .get()
                .await()

            for (userDoc in usersSnapshot.documents) {
                batch.update(
                    userDoc.reference,
                    "waitlistEntryIds",
                    FieldValue.arrayRemove(entryDocument.id)
                )
            }

            if (makeNotice != null) {
                val entry = try {
                    entryDocument.toObject(WaitlistEntry::class.java)
                } catch (_: Exception) {
                    null
                }
                if (entry != null && entry.status in WINNING_STATUSES) {
                    val notice = makeNotice(entry)
                    batch.set(db.collection("messages").document(notice.messageId), notice)
                    notified++
                }
            }

            batch.delete(entryDocument.reference)
        }

        batch.delete(collectionRef.document(id))
        return notified
    }

    internal suspend fun queueCampsiteLotteryDeletions(
        batch: WriteBatch,
        campsiteId: String
    ) {
        val lotteries = collectionRef
            .whereArrayContains("campsiteIds", campsiteId)
            .get()
            .await()

        for (lottery in lotteries.documents) {
            queueLotteryDeletion(batch, lottery.id, makeNotice = null)
        }
    }

    /**
     * Reads every lottery a campsite owner created from Firestore, including ones not
     * in the cache yet.
     *
     * @param ownerId The device ID of the owner.
     * @return The owner's lotteries. Empty if [ownerId] is blank.
     */
    suspend fun getLotteriesByOwner(ownerId: String): List<Lottery> {
        if (ownerId.isBlank()) {
            return emptyList()
        }
        val snapshot = collectionRef.whereEqualTo("ownerId", ownerId).get().await()
        return snapshot.documents.mapNotNull { document ->
            try {
                fromDocument(document)
            } catch (e: Exception) {
                null // skip lotteries that are not valid
            }
        }
    }

    private companion object {
        /** Statuses of entries that won a lottery, so hold a booking. */
        val WINNING_STATUSES = setOf(WaitlistStatus.WON, WaitlistStatus.ACCEPTED)
    }

    /**
     * Deletes a lottery from the Firestore database, along with every waitlist entry
     * for it and every entrant's reference to those entries.
     *
     * @param lottery The lottery to be deleted.
     */
    suspend fun deleteLottery(lottery: Lottery) = delete(lottery)


    /**
     * Saves a lottery to Firestore, creating it if absent.
     *
     * @param updatedLottery The lottery data to save.
     */
    suspend fun updateLottery(updatedLottery: Lottery) = update(updatedLottery)

    /**
     * Gets a [Lottery] object from the database given its lotteryId.
     *
     * @param lotteryId The `lotteryId` to search for.
     * @return The [Lottery] object with the specified lotteryId if it exists. `null` otherwise.
     */
    suspend fun getLottery(lotteryId: String): Lottery? = get(lotteryId)

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
        if (lottery.isFull()) {
            return false
        }

        // check if this entry is already on the waitlist
        if (waitlistEntry.entryId in lottery.entryIds) {
            return false
        }

        // valid conditions, union the old entryIds with the new entryId (add)
        collectionRef.document(lottery.lotteryId)
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
            collectionRef.document(lottery.lotteryId)
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

        for (lottery in cached) {
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

        val lotteryRef = collectionRef.document(lotteryId)
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

    /**
     * Draws the winners of a lottery randomly from the waitlist. The status of the entries
     * that gets drawn become WON, the non-winners become LOST, and the lottery status becomes
     * DRAWN. The amount drawn is specified by the owner (in lottery object data).
     *
     * @param lotteryId The ID of the lottery to draw.
     * @return `null` if the winners were drawn successfully, an error message otherwise.
     */
    suspend fun drawWinners(lotteryId: String): String? {
        val lotteryRef = collectionRef.document(lotteryId)
        val entriesRef = db.collection("waitlistEntries")
        var errorMessage: String? = null

        try {
            db.runTransaction { transaction ->
                errorMessage = null

                val lottery = transaction.get(lotteryRef).toObject(Lottery::class.java)

                if (lottery == null) {
                    errorMessage = "This lottery no longer exists."
                } else if (lottery.status != LotteryStatus.OPEN) {
                    errorMessage = "The lottery is not open."
                } else if (Date().before(lottery.registrationEnd)) {
                    errorMessage = "Registration has not ended yet."
                } else {
                    val possibleWinners = mutableListOf<DocumentReference>()

                    for (entryId in lottery.entryIds) {
                        val entryRef = entriesRef.document(entryId)
                        val entry = transaction.get(entryRef)
                            .toObject(WaitlistEntry::class.java)

                        if (entry != null && entry.status == WaitlistStatus.IN_WAITLIST) {
                            possibleWinners.add(entryRef)
                        }
                    }

                    possibleWinners.shuffle() // random
                    val winnersCount = minOf(lottery.numberOfWinners, possibleWinners.size) // for cases when entries in waitlist < num of winners
                    
                    for (i in 0 until winnersCount) { // set winners status
                        transaction.update(possibleWinners[i], "status", WaitlistStatus.WON.name)
                    }
                    for (i in winnersCount until possibleWinners.size) { // set losers status
                        transaction.update(possibleWinners[i], "status", WaitlistStatus.LOST.name)
                    }

                    transaction.update(lotteryRef, "status", LotteryStatus.DRAWN.name)
                }

                null
            }.await()
        } catch (e: Exception) {
            return "Could not draw the winners. Please try again."
        }

        return errorMessage
    }
}
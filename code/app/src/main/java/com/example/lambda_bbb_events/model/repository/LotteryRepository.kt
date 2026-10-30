package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.Lottery
import com.google.firebase.Firebase
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
}
package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.Reservation
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Handles the data in the Firestore database for the [Reservation] class.
 */
class ReservationRepository {
    private val db = Firebase.firestore
    private val reservationsRef = db.collection("reservations")

    private val _reservations = mutableStateListOf<Reservation>()

    init {
        reservationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _reservations.clear()

            snapshot?.documents?.forEach { document ->
                val reservation = document.toObject(Reservation::class.java)
                if (reservation != null) {
                    _reservations.add(reservation)
                }
            }
        }
    }

    val reservations: List<Reservation>
        get() = _reservations

    /**
     * Adds a new reservation to the Firestore database.
     *
     * @param reservation The reservation to add.
     */
    fun addReservation(reservation: Reservation) {
        reservationsRef.document(reservation.reservationId).set(reservation)
    }

    /**
     * Deletes a reservation from the Firestore database.
     *
     * @param reservation The reservation to be deleted.
     */
    fun deleteReservation(reservation: Reservation) {
        reservationsRef.document(reservation.reservationId).delete()
    }

    /**
     * Overwrites an existing reservation in the Firestore database with updated data.
     *
     * @param updatedReservation The reservation to be updated. Must have the same `reservationId`
     * as the original document.
     */
    fun updateReservation(updatedReservation: Reservation) {
        reservationsRef.document(updatedReservation.reservationId).set(updatedReservation)
    }
}
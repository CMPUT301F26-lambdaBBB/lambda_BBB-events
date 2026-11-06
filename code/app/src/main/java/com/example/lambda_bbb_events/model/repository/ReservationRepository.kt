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

            val newReservations = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Reservation::class.java)
            } ?: emptyList()

            synchronized(_reservations) {
                _reservations.clear()
                _reservations.addAll(newReservations)
            }
        }
    }

    val reservations: List<Reservation>
        get() = synchronized(_reservations) { _reservations.toList() }

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

    /**
     * Gets a [Reservation] object from the database given its reservationId.
     *
     * @param reservationId The `reservationId` to search for.
     * @return The [Reservation] object with the specified reservationId if it exists. `null` otherwise.
     */
    fun getReservation(reservationId: String): Reservation? {
        val snapshotList = synchronized(_reservations) { _reservations.toList() }
        return snapshotList.find { it.reservationId == reservationId }
    }

    /**
     * Gets all the [Reservation] objects for a specific Campsite given its `campsiteId`.
     *
     * @param campsiteId The `campsiteId` to search for.
     * @return The list of [Reservation] associated with the `campsiteId`. An empty list
     * is returned if no reservation matches the `campsiteId`.
     */
    fun getReservationsForCampsite(campsiteId: String): List<Reservation>{
        val snapshotList = synchronized(_reservations) { _reservations.toList() }
        return snapshotList.filter { it.campsiteId == campsiteId }
    }

    /**
     * Gets all the [Reservation] objects for a specific Entrant given its `entrantId`.
     *
     * @param entrantId The `entrantId` to search for.
     * @return The list of [Reservation] associated with the `entrantId`. An empty list
     * is returned if no reservation matches the `entrantId`.
     */
    fun getReservationsForEntrant(entrantId: String): List<Reservation>{
        val snapshotList = synchronized(_reservations) { _reservations.toList() }
        return snapshotList.filter { it.entrantId == entrantId }
    }
}
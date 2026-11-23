package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.reservation.Reservation
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/*
 * Manages reservation data and cached records using Firestore.
 */

/**
 * Handles the data in the Firestore database for the [Reservation] class.
 */
class ReservationRepository(
    db: FirebaseFirestore = Firebase.firestore
) : Repository<Reservation>("reservations", db) {

    init {
        startListening()
    }

    override fun idOf(item: Reservation) = item.reservationId

    override fun fromDocument(document: DocumentSnapshot): Reservation? =
        document.toObject(Reservation::class.java)

    /** Cached reservations synchronized with Firestore. */
    val reservations: List<Reservation>
        get() = cached

    /**
     * Adds a new reservation to the Firestore database.
     *
     * @param reservation The reservation to add.
     */
    suspend fun addReservation(reservation: Reservation) = add(reservation)

    /**
     * Deletes a reservation from the Firestore database.
     *
     * @param reservation The reservation to be deleted.
     */
    suspend fun deleteReservation(reservation: Reservation) = delete(reservation)


    /**
     * Saves a reservation to Firestore, creating it if absent.
     *
     * @param updatedReservation The reservation data to save.
     */
    suspend fun updateReservation(updatedReservation: Reservation) = update(updatedReservation)

    /**
     * Gets a [Reservation] object from the cache given its reservationId.
     *
     * @param reservationId The `reservationId` to search for.
     * @return The [Reservation] object with the specified reservationId if it exists. `null` otherwise.
     */
    fun getReservation(reservationId: String): Reservation? = getCached(reservationId)

    /**
     * Gets all the [Reservation] objects for a specific Campsite given its `campsiteId`.
     *
     * @param campsiteId The `campsiteId` to search for.
     * @return The list of [Reservation] associated with the `campsiteId`. An empty list
     * is returned if no reservation matches the `campsiteId`.
     */
    fun getReservationsForCampsite(campsiteId: String): List<Reservation> =
        cached.filter { it.campsiteId == campsiteId }

    /**
     * Gets all the [Reservation] objects for a specific Entrant given its `entrantId`.
     *
     * @param entrantId The `entrantId` to search for.
     * @return The list of [Reservation] associated with the `entrantId`. An empty list
     * is returned if no reservation matches the `entrantId`.
     */
    fun getReservationsForEntrant(entrantId: String): List<Reservation> =
        cached.filter { it.entrantId == entrantId }
}

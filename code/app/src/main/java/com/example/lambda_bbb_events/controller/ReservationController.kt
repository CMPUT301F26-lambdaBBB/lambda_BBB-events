package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.Reservation
import com.example.lambda_bbb_events.model.repository.ReservationRepository

/**
 * Controller for [Reservation]-related operations.
 *
 * @property reservationRepository The repository for [Reservation].
 */
class ReservationController(
    val reservationRepository: ReservationRepository
) {
    /**
     * Gets all the reservations made by an entrant.
     *
     * @param entrantId The device ID of the entrant.
     * @return The list of reservations made by the entrant.
     */
    fun getReservationsForEntrant(entrantId: String): List<Reservation> {
        return reservationRepository.getReservationsForEntrant(entrantId)
    }
}
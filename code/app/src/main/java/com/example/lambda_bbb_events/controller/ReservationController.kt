package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.formatting.parseDateOrNull
import com.example.lambda_bbb_events.model.reservation.Reservation
import com.example.lambda_bbb_events.model.reservation.ReservationStatus
import com.example.lambda_bbb_events.model.repository.ReservationRepository
import java.time.LocalDate

/**
 * The groups for the owner reservations view.
 */
enum class ReservationFilter {
    ALL,        // all reservations
    UPCOMING,   // status: pending or confirmed and the stay has not ended
    PAST,       // status: not cancelled or rejected and the stay has ended
    CANCELLED   // status: cancelled by the entrant or rejected by the owner
}

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

    /**
     * Cancels a reservation by updating its status to CANCELLED.
     *
     * @param reservation The reservation to cancel.
     */
    suspend fun cancelReservation(reservation: Reservation) {
        val updated = reservation.copy(status = ReservationStatus.CANCELLED)
        reservationRepository.updateReservation(updated)
    }

    /**
     * Gets the reservations of a campsite that match the filter. They are sorted by
     * earliest first.
     *
     * @param campsiteId The `campsiteId` to search for.
     * @param filter The filter of reservations to return.
     * @return The list of matching reservations.
     */
    suspend fun getReservationsForCampsite(
        campsiteId: String,
        filter: ReservationFilter
    ): List<Reservation> {
        val result = mutableListOf<Reservation>()

        for (reservation in reservationRepository.getReservationsForCampsite(campsiteId)) {
            if (matchesFilter(reservation, filter)) {
                result.add(reservation)
            }
        }

        if (filter == ReservationFilter.UPCOMING) {
            return result.sortedBy { parseDateOrNull(it.startDate) ?: LocalDate.MAX }
        }

        return result.sortedByDescending { parseDateOrNull(it.startDate) ?: LocalDate.MIN }
    }

    /**
     * Checks if a reservation matches the filter.
     *
     * @param reservation The reservation to check.
     * @param filter The filter to check against.
     * @return `true` if the reservation matches the filter, `false` otherwise.
     */
    private fun matchesFilter(reservation: Reservation, filter: ReservationFilter): Boolean {
        val isCancelled = reservation.status == ReservationStatus.CANCELLED ||
                reservation.status == ReservationStatus.REJECTED

        val endDate = parseDateOrNull(reservation.endDate)
        val hasEnded = reservation.status == ReservationStatus.COMPLETED ||
                (endDate != null && endDate.isBefore(LocalDate.now()))

        return when (filter) {
            ReservationFilter.ALL -> true
            ReservationFilter.CANCELLED -> isCancelled
            ReservationFilter.PAST -> !isCancelled && hasEnded
            ReservationFilter.UPCOMING -> !isCancelled && !hasEnded
        }
    }
}
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
    /** The repository used to load and persist reservations. */
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
     * Cancels a reservation by updating its status to REJECTED.
     *
     * @param reservation The reservation to cancel.
     */
    suspend fun rejectReservation(reservation: Reservation) {
        val updated = reservation.copy(status = ReservationStatus.REJECTED)
        reservationRepository.updateReservation(updated)
    }

    /**
     * Confirms a reservation for an entrant who has won a lottery.
     *
     * Creates a confirmed reservation and updates the associated
     * waitlist entry status to ACCEPTED.
     *
     * @param entryId The unique identifier of the waitlist entry.
     * @param entrantId The device ID of the current entrant.
     * @param campsiteId The selected campsite identifier.
     * @param fromDate The reservation start date.
     * @param endDate The reservation end date.
     * @param numberOfGuests The number of guests.
     */
    suspend fun confirmLotteryReservation(
        entryId: String,
        entrantId: String,
        campsiteId: String,
        fromDate: LocalDate,
        endDate: LocalDate,
        numberOfGuests: Int
    ) {
        require(entryId.isNotBlank()) {
            "Invalid waitlist entry."
        }

        require(entrantId.isNotBlank()) {
            "Invalid entrant."
        }

        require(campsiteId.isNotBlank()) {
            "Invalid campsite."
        }

        require(!endDate.isBefore(fromDate)) {
            "End date cannot be before start date."
        }

        require(numberOfGuests > 0) {
            "Number of guests must be greater than zero."
        }

        reservationRepository.confirmLotteryReservation(
            entryId = entryId,
            entrantId = entrantId,
            campsiteId = campsiteId,
            fromDate = fromDate.toString(),
            endDate = endDate.toString(),
            numberOfGuests = numberOfGuests
        )
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
     * Finds the days a campsite is fully booked. A day is fully booked when the guests of all
     * active (pending or confirmed) reservations that cover it add up to the campsite's
     * max occupancy or more. A reservation covers every day from its start date to its end
     * date, including both.
     *
     * @param reservations The reservations of the campsite.
     * @param maxOccupancy The max occupancy of the campsite.
     * @return The set of fully booked days.
     *
     * Reference: Claude created this function.
     */
    fun getFullyBookedDates(reservations: List<Reservation>, maxOccupancy: Int): Set<LocalDate> {
        val guestsPerDay = mutableMapOf<LocalDate, Int>()

        for (reservation in reservations) {
            if (!reservation.isActive()) continue

            val start = parseDateOrNull(reservation.startDate)
            val end = parseDateOrNull(reservation.endDate)
            if (start == null || end == null || end.isBefore(start)) continue

            var day: LocalDate = start
            while (!day.isAfter(end)) {
                guestsPerDay[day] = (guestsPerDay[day] ?: 0) + reservation.guests
                day = day.plusDays(1)
            }
        }

        return guestsPerDay.filter { it.value >= maxOccupancy }.keys
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
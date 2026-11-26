package com.example.lambda_bbb_events.model.reservation

import com.google.firebase.firestore.Exclude
import java.time.LocalDate
import java.time.format.DateTimeParseException

/*
 * Defines campsite reservations and its details.
 */

/**
 * The current status of a campsite reservation.
 */
enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    COMPLETED
}

/**
 * Represents a reservation made by an entrant for a campsite.
 *
 * @property reservationId The unique ID of the reservation.
 * @property campsiteId The ID of the reserved campsite.
 * @property entrantId The device ID of the entrant.
 * @property ownerId The device ID of the campsite owner.
 * @property startDate The start date of the reservation.
 * @property endDate The end date of the reservation.
 * @property guests The number of guests in the reservation.
 * @property status The current status of the reservation.
 * @property ruleConfirmed Whether the entrant accepted the campsite rules.
 */
data class Reservation(
    val reservationId: String = "",
    val campsiteId: String = "",
    val entrantId: String = "",
    val ownerId: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val guests: Int = 1,
    val status: ReservationStatus = ReservationStatus.PENDING,
    val ruleConfirmed: Boolean = false
) {

    /**
     * Checks whether the reservation is pending or confirmed.
     *
     * @return True if the reservation is active.
     */
    @Exclude
    fun isActive(): Boolean {
        return status == ReservationStatus.PENDING ||
                status == ReservationStatus.CONFIRMED
    }

    /**
     * Checks whether this reservation permits owner messaging.
     *
     * The reservation must be active and its stay must not
     * have ended.
     *
     * @return True if the entrant is eligible to message the owner.
     */
    @Exclude
    fun isMessagingEligible(): Boolean {
        if (!isActive()) return false

        return try {
            !LocalDate.parse(endDate).isBefore(LocalDate.now())
        } catch (e: DateTimeParseException) {
            false
        }
    }
}
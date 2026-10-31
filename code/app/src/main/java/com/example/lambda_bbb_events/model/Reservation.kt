package com.example.lambda_bbb_events.model

import com.google.firebase.firestore.Exclude
import java.time.LocalDate

/**
 * The current status of a campsite reservation.
 */
enum class ReservationStatus {
    PENDING,     // created, waiting on owner or confirmation
    CONFIRMED,   // owner accepted the entrant
    REJECTED,    // owner rejected the entrant
    CANCELLED,   // entrant cancelled
    COMPLETED    // after confirmed, stay has ended
}

/**
 * Represents a reservation made by an entrant for a campsite.
 *
 * @property reservationId The unique ID of the reservation.
 * @property campsiteId The ID of the reserved campsite.
 * @property entrantId The device ID of the entrant who made the reservation.
 * @property startDate The start date of the reservation.
 * @property endDate The end date of the reservation.
 * @property guests The number of guests in the reservation.
 * @property status The current status of the reservation.
 * @property ruleConfirmed `true` if the entrant has accepted the campsite rules,
 * `false` otherwise.
 */
data class Reservation(
    val reservationId: String = "",
    val campsiteId: String = "",
    val entrantId: String = "", // entrant deviceId
    val ownerId: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val guests: Int = 1,
    val status: ReservationStatus = ReservationStatus.PENDING,
    val ruleConfirmed: Boolean = false, // entrant accepted campsite rules and policies
) {
    @Exclude
    fun isActive(): Boolean {
        return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED
    }
}
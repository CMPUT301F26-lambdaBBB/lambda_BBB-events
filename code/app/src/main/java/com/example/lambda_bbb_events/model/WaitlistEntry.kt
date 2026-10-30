package com.example.lambda_bbb_events.model

import java.sql.Date

enum class WaitlistStatus {
    IN_WAITLIST, // still in waitlist
    AVAILABLE,   // spot is available to book
    DECLINED,     // owner declined the entrant from waitlist
    CANCELLED,   // entrant cancelled
    COMPLETED    // entrant booked after available
}

/**
 * Represents an entrant's waitlist entry.
 *
 * @property entryId The unique ID of the entry.
 * @property joinedAt The date and time the entrant joined the waitlist.
 * @property numberOfGuests The total number of guests included in the reservation request.
 * @property status The status of the waitlist.
 */
data class WaitlistEntry (
    val entryId: String,
    val joinedAt: Date,
    val numberOfGuests: Int,
    var status: WaitlistStatus = WaitlistStatus.IN_WAITLIST
) {
    /**
     * Updates the waitlist entry to decline.
     */
    fun declineEntry() {
        status = WaitlistStatus.DECLINED
    }
}
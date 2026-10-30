package com.example.lambda_bbb_events.model

import java.util.Date

/**
 * The current status of a waitlist.
 */
enum class WaitlistStatus {
    IN_WAITLIST, // still in waitlist
    WON,         // won waitlist, can accept or decline the win
    LOST,        // lost waitlist
    ACCEPTED,    // entrant accepted the win
    DECLINED     // entrant declined win or didn't respond in time
}

/**
 * Represents an entrant's waitlist entry.
 *
 * @property entryId The unique ID of the entry.
 * @property entrantId The ID of the entrant who joined the waitlist.
 * @property lotteryId The ID of the lottery this entry is from.
 * @property joinedAt The date and time the entrant joined the waitlist.
 * @property numberOfGuests The total number of guests included in the reservation request.
 * @property status The status of the waitlist.
 * @throws IllegalArgumentException triggered if [numberOfGuests] is less than 1.
 */
data class WaitlistEntry (
    val entryId: String,
    val entrantId: String,
    val lotteryId: String,
    val joinedAt: Date,
    val numberOfGuests: Int,
    var status: WaitlistStatus = WaitlistStatus.IN_WAITLIST
) {
    init {
        require(numberOfGuests >= 1) { "numberOfGuests must be at least 1" }
    }
    /**
     * Updates the waitlist entry status to "declined".
     */
    fun declineEntry() {
        status = WaitlistStatus.DECLINED
    }
}
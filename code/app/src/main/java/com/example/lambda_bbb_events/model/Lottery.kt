package com.example.lambda_bbb_events.model

import java.util.Date

/**
 * The current status of a campsite lottery.
 */
enum class LotteryStatus {
    OPEN,      // accepting entries
    CLOSED,    // not accepting entries
    DRAWN,     // lottery winners have been picked
    CANCELLED  // owner cancelled the lottery
}

/**
 * Represents a lottery run by the campsite owner.
 *
 * @property lotteryId The unique ID of the lottery.
 * @property campsiteIds IDs of the campsites included in this lottery. Must not be empty.
 * @property ownerId The ID of the campsite owner who created the lottery.
 * @property registrationStart The date when entrants can start joining the waitlist.
 * @property registrationEnd The date when the waitlist stops accepting entrants.
 * @property bookingStart The date when winners can start booking.
 * @property bookingEnd The date when the booking period ends.
 * @property numberOfWinners The amount of entries that will be drawn as winners.
 * @property waitlistCapacity The maximum number of entries allowed on the waitlist.
 * @property entryIds IDs of the [WaitlistEntry] objects currently in this lottery.
 * @property status The current status of the lottery.
 * @throws IllegalArgumentException triggered when [campsiteIds] is empty,
 * [numberOfWinners] is less than 1, or [numberOfWinners] is over the [waitlistCapacity].
 */
data class Lottery (
    val lotteryId: String,
    val campsiteIds: List<String>,
    val ownerId: String,
    val registrationStart: Date,
    val registrationEnd: Date,
    val bookingStart: Date,
    val bookingEnd: Date,
    val numberOfWinners: Int,
    val waitlistCapacity: Int,
    val entryIds: List<String> = emptyList(),
    val status: LotteryStatus = LotteryStatus.OPEN
) {
    init {
        require(campsiteIds.isNotEmpty()) { "A lottery needs at least one campsite" }
        require(numberOfWinners >= 1) { "numberOfWinners must be at least 1" }
        require(numberOfWinners <= waitlistCapacity) {
            "numberOfWinners cannot be over the waitlistCapacity"
        }
    }

    /**
     * Checks if the waitlist is full.
     *
     * @return `true` if the waitlist is full, `false` otherwise.
     */
    fun isFull(): Boolean {
        return entryIds.size >= waitlistCapacity
    }

    /**
     * Checks if entrants can join the lottery at a specified date and time.
     *
     * @param date The specified time to check. It defaults to the current time
     * @return `true` if registration is open, `false` otherwise.
     */
    fun isRegistrationOpen(date: Date = Date()): Boolean {
        return status == LotteryStatus.OPEN && date.after(registrationStart) &&
                date.before(registrationEnd)
    }
}
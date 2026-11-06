package com.example.lambda_bbb_events.model

import com.google.firebase.firestore.Exclude
import java.util.Date

/*
 * Defines lottery information such as registration periods and waitlist capacity
 */

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
 */
data class Lottery (
    val lotteryId: String = "",
    val campsiteIds: List<String> = emptyList(),
    val ownerId: String = "",
    val registrationStart: Date = Date(),
    val registrationEnd: Date = Date(),
    val bookingStart: Date = Date(),
    val bookingEnd: Date = Date(),
    val numberOfWinners: Int = 1,
    val waitlistCapacity: Int = 1,
    val entryIds: List<String> = emptyList(),
    val status: LotteryStatus = LotteryStatus.OPEN
) {
    /**
     * Checks if the waitlist is full.
     *
     * @return `true` if the waitlist is full, `false` otherwise.
     */
    @Exclude
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
        return status == LotteryStatus.OPEN && date >= registrationStart &&
                date <= registrationEnd
    }
}
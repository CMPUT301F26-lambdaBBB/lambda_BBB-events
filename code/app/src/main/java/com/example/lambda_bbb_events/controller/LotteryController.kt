package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.LotteryStatus
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID


/*
 * Manages lottery operations and filtering through the repository pattern.
 */

/**
 * Define filters for retrieving lotteries by lottery status.
 */
enum class LotteryFilter {
    ALL,        // all lotteries
    ACTIVE,     // the registration and/or booking period still active
    PAST,       // the booking period has ended
    CANCELLED   // cancelled by the owner
}

/**
 * The phase of a lottery.
 *
 * @property label The text shown to the user.
 */
enum class LotteryPhase(val label: String) {
    UPCOMING("Upcoming"),                   // registration has not started
    REGISTRATION_OPEN("Registration open"), // entrants can join the waitlist
    AWAITING_DRAW("Awaiting draw"),         // registration ended, winners not drawn yet
    WINNERS_DRAWN("Winners drawn"),         // winners drawn, booking has not started
    BOOKING_OPEN("Booking open"),           // winners can book
    ENDED("Ended"),                         // booking period ended
    CANCELLED("Cancelled")                  // cancelled by the owner
}

/**
 * Handles lottery creation, retrieval, and registration.
 *
 * @property lotteryRepository Repository for lottery data.
 */
class LotteryController(
    val lotteryRepository: LotteryRepository
) {
    /**
     * Gets a [Lottery] given its lotteryId.
     *
     * @param lotteryId The `lotteryId` to search for.
     * @return The [Lottery] with the specified lotteryId if it exists. `null` otherwise.
     */
    suspend fun getLottery(lotteryId: String): Lottery? {
        return lotteryRepository.getLottery(lotteryId)
    }

    /**
     * Creates a lottery for a campsite. Includes input validation.
     *
     * @param ownerId The device ID of the owner of the lottery.
     * @param campsiteId The ID of the campsite of the lottery.
     * @param registrationStart The start date of the registration period.
     * @param registrationEnd The end date of the registration period.
     * @param bookingStart The start date of the booking period.
     * @param bookingEnd The end date of the booking period.
     * @param numberOfWinnersString The number of winners of the lottery (as a string).
     * @param waitlistCapacityString The maximum number of entries (as a string).
     * @return `null` if the lottery was created successfully. An error message otherwise.
     */
    suspend fun createLottery(
        ownerId: String,
        campsiteId: String,
        registrationStart: LocalDate?,
        registrationEnd: LocalDate?,
        bookingStart: LocalDate?,
        bookingEnd: LocalDate?,
        numberOfWinnersString: String,
        waitlistCapacityString: String
    ): String? {
        val today = LocalDate.now()

        // inpuut check the dates
        if (registrationStart == null || registrationEnd == null ||
            bookingStart == null || bookingEnd == null) {
            return "Please pick all four dates."
        }

        if (registrationEnd.isBefore(today)) {
            return "The registration period must not end before today."
        }

        if (registrationEnd.isBefore(registrationStart)) {
            return "The registration period must end after it starts."
        }

        if (!bookingStart.isAfter(registrationEnd)) {
            return "The booking period must start after the registration period ends."
        }

        if (bookingEnd.isBefore(bookingStart)) {
            return "The booking period must end after it starts."
        }

        // input check the no of winners
        val numberOfWinners = numberOfWinnersString.trim().toIntOrNull()
            ?: return "The number of winners is not a valid number."
        if (numberOfWinners < 1) {
            return "The number of winners must be at least 1."
        }

        // input check the capacity
        val waitlistCapacity = waitlistCapacityString.trim().toIntOrNull()
            ?: return "The waitlist capacity is not a valid number."
        if (waitlistCapacity < numberOfWinners) {
            return "The waitlist capacity must be at least the number of winners."
        }

        val newRegistrationStart = toStartOfDay(registrationStart)
        val newRegistrationEnd = toEndOfDay(registrationEnd)

        // a campsite shouldn't have multiple conflicting lottery periods / overlap
        val existingLotteries = lotteryRepository.getLotteriesForCampsite(campsiteId)
        for (existing in existingLotteries) {
            val overlaps = !existing.registrationStart.after(newRegistrationEnd) &&
                    !newRegistrationStart.after(existing.registrationEnd)

            if (existing.status != LotteryStatus.CANCELLED && overlaps) {
                return "This campsite already has a lottery with an overlapping registration period."
            }
        }

        val newBookingStart = toStartOfDay(bookingStart)
        val newBookingEnd = toEndOfDay(bookingEnd)

        lotteryRepository.addLottery(
            Lottery(
                lotteryId = UUID.randomUUID().toString(),
                campsiteIds = listOf(campsiteId),
                ownerId = ownerId,
                registrationStart = newRegistrationStart,
                registrationEnd = newRegistrationEnd,
                bookingStart = newBookingStart,
                bookingEnd = newBookingEnd,
                numberOfWinners = numberOfWinners,
                waitlistCapacity = waitlistCapacity
            )
        )

        return null
    }

    /**
     * Converts a [LocalDate] date to the first timestamp of that day (so 00:00:00).
     *
     * @param date The date to convert.
     * @return The [Date] at 00:00:00 of the given date.
     */
    private fun toStartOfDay(date: LocalDate): Date {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    /**
     * Converts a [LocalDate] date to the last timestamp of that day (so 23:59:59).
     *
     * @param date The date to convert.
     * @return The [Date] at 23:59:59 of the given date.
     */
    private fun toEndOfDay(date: LocalDate): Date {
        return Date.from(date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant())
    }

    /**
     * Gets the lotteries of a campsite that match a filter.
     *
     * @param campsiteId The `campsiteId` to search for.
     * @param filter Which lotteries to return. The default is only the active ones.
     * @return The list of matching lotteries.
     */
    fun getLotteriesForCampsite(
        campsiteId: String,
        filter: LotteryFilter = LotteryFilter.ACTIVE
    ): List<Lottery> {
        val result = mutableListOf<Lottery>()

        for (lottery in lotteryRepository.getLotteriesForCampsite(campsiteId)) {
            if (matchesFilter(lottery, filter)) {
                result.add(lottery)
            }
        }

        return result
    }

    /**
     * Gets where a lottery is in its timeline, using its current status and
     * registration and booking dates.
     *
     * @param lottery The lottery to check.
     * @return The [LotteryPhase] of the lottery right now.
     */
    fun getPhase(lottery: Lottery): LotteryPhase {
        val today = Date()

        if (lottery.status == LotteryStatus.CANCELLED) {
            return LotteryPhase.CANCELLED
        }

        if (today.before(lottery.registrationStart)) {
            return LotteryPhase.UPCOMING
        }

        if (!today.after(lottery.registrationEnd)) {
            return LotteryPhase.REGISTRATION_OPEN
        }

        if (today.before(lottery.bookingStart)) {
            if (lottery.status == LotteryStatus.DRAWN) {
                return LotteryPhase.WINNERS_DRAWN
            }
            return LotteryPhase.AWAITING_DRAW
        }

        if (!today.after(lottery.bookingEnd)) {
            return LotteryPhase.BOOKING_OPEN
        }

        return LotteryPhase.ENDED
    }

    /**
     * Checks if a lottery matches a filter.
     *
     * @param lottery The lottery to check.
     * @param filter The filter to check against.
     * @return `true` if the lottery matches the filter, `false` otherwise.
     */
    private fun matchesFilter(lottery: Lottery, filter: LotteryFilter): Boolean {
        val phase = getPhase(lottery)

        if (filter == LotteryFilter.ALL) {
            return true
        }

        if (filter == LotteryFilter.CANCELLED) {
            return phase == LotteryPhase.CANCELLED
        }

        if (filter == LotteryFilter.PAST) {
            return phase == LotteryPhase.ENDED
        }

        return phase != LotteryPhase.ENDED && phase != LotteryPhase.CANCELLED
    }


    /**
     * Retrieves the first lottery accepting registrations
     * for the specified campsite.
     *
     * @param campsiteId The ID of the campsite.
     * @return An open [Lottery], or null if none is available.
     */
    fun getOpenLotteryForCampsite(campsiteId: String): Lottery? {
        return lotteryRepository
            .getLotteriesForCampsite(campsiteId)
            .firstOrNull { it.isRegistrationOpen() }
    }


    /**
     * Submits a user's entry to a campsite lottery.
     *
     * @param lotteryId The ID of the lottery to join.
     * @param entrantId The ID of the participating user.
     * @param numberOfGuests The number of guests in the entry.
     * @param onResult Callback returning the success status and a message.
     */
    fun joinLotteryEntry(
        lotteryId: String,
        entrantId: String,
        numberOfGuests: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        lotteryRepository.joinLottery(
            lotteryId = lotteryId,
            entrantId = entrantId,
            numberOfGuests = numberOfGuests,
            onResult = onResult
        )
    }
}
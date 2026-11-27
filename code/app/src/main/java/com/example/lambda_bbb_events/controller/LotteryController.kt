package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.LotteryStatus
import com.example.lambda_bbb_events.model.Message
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
    AWAITING_DRAW("Awaiting draw"),         // registration ended, booking has not started
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
     * Creates a lottery for one or more campsites. Includes input validation.
     *
     * @param ownerIds The device IDs of the owners of the lottery.
     * @param campsiteIds The IDs of the campsites of the lottery.
     * @param registrationStart The start date of the registration period.
     * @param registrationEnd The end date of the registration period.
     * @param bookingStart The start date of the booking period.
     * @param bookingEnd The end date of the booking period.
     * @param numberOfWinnersString The number of winners of the lottery (as a string).
     * @param waitlistCapacityString The maximum number of entries (as a string).
     * @return `null` if the lottery was created successfully. An error message otherwise.
     */
    suspend fun createLottery(
        ownerIds: List<String>,
        campsiteIds: List<String>,
        registrationStart: LocalDate?,
        registrationEnd: LocalDate?,
        bookingStart: LocalDate?,
        bookingEnd: LocalDate?,
        numberOfWinnersString: String,
        waitlistCapacityString: String
    ): String? {
        val today = LocalDate.now()

        if (campsiteIds.isEmpty()) {
            return "Please pick at least one campsite."
        }

        if (ownerIds.isEmpty()) {
            return "A lottery needs at least one owner."
        }

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

        // input check the num of winners
        val numberOfWinners = numberOfWinnersString.trim().toIntOrNull()
            ?: return "The number of winners is not a valid number."
        if (numberOfWinners < 1) {
            return "The number of winners must be at least 1."
        }

        // input check the capacity
        val capacityText = waitlistCapacityString.trim()
        val waitlistCapacity: Int? =
            if (capacityText.isEmpty()) {
                null // empty means no limit
            }
            else {
                capacityText.toIntOrNull()
                    ?: return "The waitlist capacity is not a valid number."
            }
        if (waitlistCapacity != null && waitlistCapacity < numberOfWinners) {
            return "The waitlist capacity must be at least the number of winners."
        }

        val newRegistrationStart = toStartOfDay(registrationStart)
        val newRegistrationEnd = toEndOfDay(registrationEnd)

        // a campsite shouldn't have multiple conflicting lottery periods / overlap
        for (campsiteId in campsiteIds) {
            val existingLotteries = lotteryRepository.getLotteriesForCampsite(campsiteId)
            for (existing in existingLotteries) {
                val overlaps = !existing.registrationStart.after(newRegistrationEnd) &&
                        !newRegistrationStart.after(existing.registrationEnd)

                if (existing.status != LotteryStatus.CANCELLED && overlaps) {
                    return if (campsiteIds.size == 1) {
                        "This campsite already has a lottery with an overlapping registration period."
                    } else {
                        "One of the selected campsites already has a lottery with an overlapping registration period."
                    }
                }
            }
        }

        val newBookingStart = toStartOfDay(bookingStart)
        val newBookingEnd = toEndOfDay(bookingEnd)

        lotteryRepository.addLottery(
            Lottery(
                lotteryId = UUID.randomUUID().toString(),
                campsiteIds = campsiteIds,
                ownerIds = ownerIds,
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
     * Updates a lottery for a campsite/campsites. Includes input validation.
     *
     * @param lotteryId The ID of the lottery to update.
     * @param ownerId The device ID of the owner of the lottery.
     * @param registrationStart The start date of the registration period.
     * @param registrationEnd The end date of the registration period.
     * @param bookingStart The start date of the booking period.
     * @param bookingEnd The end date of the booking period.
     * @param numberOfWinnersString The number of winners of the lottery (as a string).
     * @param waitlistCapacityString The maximum number of entries (as a string).
     * @return `null` if the lottery was created successfully. An error message otherwise.
     */
    suspend fun updateLottery(
        lotteryId: String,
        registrationStart: LocalDate?,
        registrationEnd: LocalDate?,
        bookingStart: LocalDate?,
        bookingEnd: LocalDate?,
        numberOfWinnersString: String,
        waitlistCapacityString: String
    ): String? {
        val existingLottery = lotteryRepository.getLottery(lotteryId)
            ?: return "Lottery not found."

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

        // input check the num of winners
        val numberOfWinners = numberOfWinnersString.trim().toIntOrNull()
            ?: return "The number of winners is not a valid number."
        if (numberOfWinners < 1) {
            return "The number of winners must be at least 1."
        }

        // input check the capacity
        val capacityText = waitlistCapacityString.trim()
        val waitlistCapacity: Int? =
            if (capacityText.isEmpty()) {
                null // empty means no limit
            }
            else {
                capacityText.toIntOrNull()
                    ?: return "The waitlist capacity is not a valid number."
            }
        if (waitlistCapacity != null && waitlistCapacity < numberOfWinners) {
            return "The waitlist capacity must be at least the number of winners."
        }
        if (waitlistCapacity != null && waitlistCapacity < existingLottery.entryIds.size) {
            return "The waitlist capacity must not be smaller than the number of entrants already on it."
        }

        val newRegistrationStart = toStartOfDay(registrationStart)
        val newRegistrationEnd = toEndOfDay(registrationEnd)

        // a campsite shouldn't have multiple conflicting lottery periods / overlap
        for (campsiteId in existingLottery.campsiteIds) {
            val existingLotteries = lotteryRepository.getLotteriesForCampsite(campsiteId)
            for (existing in existingLotteries) {
                if (existing.lotteryId == lotteryId) {
                    continue
                }
                val overlaps = !existing.registrationStart.after(newRegistrationEnd) &&
                        !newRegistrationStart.after(existing.registrationEnd)

                if (existing.status != LotteryStatus.CANCELLED && overlaps) {
                    return if (existingLottery.campsiteIds.size == 1) {
                        "This campsite already has a lottery with an overlapping registration period."
                    } else {
                        "A campsite in this lottery already has a lottery with an overlapping registration period."
                    }
                }
            }
        }

        val newBookingStart = toStartOfDay(bookingStart)
        val newBookingEnd = toEndOfDay(bookingEnd)

        lotteryRepository.updateLottery(
            Lottery(
                lotteryId = lotteryId,
                campsiteIds = existingLottery.campsiteIds,
                ownerIds = existingLottery.ownerIds,
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
     * Deletes a lottery.
     *
     * @param lottery The lottery to delete.
     */
    suspend fun deleteLottery(lottery: Lottery) {
        lotteryRepository.deleteLottery(lottery)
    }

    /**
     * Deletes every active lottery that includes an owner who is deleting their account, and sends
     * each winner a message that their booking is no longer valid. A lottery is active
     * until its booking period ends or it is cancelled, as in [LotteryFilter.ACTIVE].
     * Lotteries that have ended or were cancelled are left as they are.
     *
     * @param ownerId The device ID of the owner.
     * @param campsiteName Gets a campsite's name from its ID, for the message, or `null` if unknown.
     * @return The number of winners sent a message.
     */
    suspend fun cancelLotteriesOfDeletedOwner(
        ownerId: String,
        campsiteName: (String) -> String?
    ): Int {
        var notified = 0
        for (lottery in lotteryRepository.getLotteriesByOwner(ownerId)) {
            if (!matchesFilter(lottery, LotteryFilter.ACTIVE)) {
                continue
            }

            val names = lottery.campsiteIds.mapNotNull(campsiteName)
            val place = if (names.isEmpty()) "this campsite" else names.joinToString(", ")
            notified += lotteryRepository.deleteAndNotifyWinners(lottery.lotteryId) { entry ->
                Message(
                    messageId = UUID.randomUUID().toString(),
                    content = "Your booking at $place is no longer valid. The campsite " +
                            "owner deleted their account, so the lottery was cancelled.",
                    senderId = ownerId,
                    recipientId = entry.entrantId
                )
            }
        }
        return notified
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
     * Gets where a lottery is in its timeline from its registration and booking dates.
     * Winners are drawn by a Firebase Function, so only cancelling changes the phase
     * through the lottery's status.
     *
     * @param lottery The lottery to check.
     * @return The [LotteryPhase] of the lottery right now.
     */
    fun getPhase(lottery: Lottery): LotteryPhase {
        val today = Date()

        if (lottery.status == LotteryStatus.CANCELLED) {
            return LotteryPhase.CANCELLED
        }

        if (today.before(lottery.registrationStart)) { // today < reg start
            return LotteryPhase.UPCOMING
        }

        if (!today.after(lottery.registrationEnd)) { // today <= reg end
            return LotteryPhase.REGISTRATION_OPEN
        }

        if (today.before(lottery.bookingStart)) { // today < book start
            return LotteryPhase.AWAITING_DRAW
        }

        if (!today.after(lottery.bookingEnd)) { // today <= book end
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
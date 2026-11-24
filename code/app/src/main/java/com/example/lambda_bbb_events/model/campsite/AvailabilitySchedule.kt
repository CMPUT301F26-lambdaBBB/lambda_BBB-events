package com.example.lambda_bbb_events.model.campsite

import java.time.LocalDate

data class AvailabilitySchedule (
    var dates: MutableMap<LocalDate, Int> = mutableMapOf<LocalDate, Int>(),
) {
    var currentBookings: MutableMap<String, ClosedRange<LocalDate>> = mutableMapOf()
    val minDate: LocalDate?
        get() = dates.keys.minOrNull()

    val maxDate: LocalDate?
        get() = dates.keys.maxOrNull()

    fun addAvailableDates(startDate: LocalDate, endDate: LocalDate? = null) {
        val newEndDate = endDate ?: startDate
        var newDate = startDate
        while (newDate <= newEndDate) {
            if (dates.contains(newDate)) {
                newDate = newDate.plusDays(1)
                continue
            }
            dates[newDate] = 0
            newDate = newDate.plusDays(1)
        }
    }

    /**
     * Books the days from startDate to endDate for one person, leaving every other day open.
     * Fails if any of those days is not offered or is already booked, or if the person
     * already holds a booking here.
     */
    fun addNewBooking(startDate: LocalDate, endDate: LocalDate? = null, ownerID: String): Boolean {
        val actualEnd = endDate ?: startDate
        if (ownerID in currentBookings || !isAvailable(startDate, actualEnd)) {
            return false
        }
        currentBookings[ownerID] = startDate..actualEnd
        daysBetween(startDate, actualEnd).forEach { dates[it] = 1 }
        return true
    }

    /**
     * Frees only the days held by this person's booking.
     */
    fun cancelBooking(ownerID: String): Boolean {
        val booking = currentBookings.remove(ownerID) ?: return false
        daysBetween(booking.start, booking.endInclusive)
            .filter { it in dates }
            .forEach { dates[it] = 0 }
        return true
    }

    fun removeAvailableDate(startDate: LocalDate, endDate: LocalDate? = null) {
        val newEndDate = endDate ?: startDate
        var newDate = startDate
        while (newDate <= newEndDate) {
            if (!dates.containsKey(newDate)) {
                newDate = newDate.plusDays(1)
                continue
            }
            dates.remove(newDate)
            newDate = newDate.plusDays(1)
        }
    }

    /**
     * True when every day from start to end is offered and not booked. Days outside the range
     * are ignored, so any open stretch can be booked around existing bookings.
     */
    fun isAvailable(
        start: LocalDate,
        end: LocalDate
    ): Boolean {
        if (end.isBefore(start)) {
            return false
        }
        return daysBetween(start, end).all { dates[it] == 0 }
    }

    private fun daysBetween(start: LocalDate, end: LocalDate): Sequence<LocalDate> {
        return generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }
    }
}

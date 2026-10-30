package com.example.lambda_bbb_events.model

import java.time.LocalDate

/**
 * Represents the availability schedule for a set of dates.
 *
 * @property availableDates Dates that are set to be available for booking by the owner.
 * @property bookedDates Dates that have already been booked.
 */
data class AvailabilitySchedule (
    private var availableDates: List<String> = emptyList(),
    private var bookedDates: List<String> = emptyList()
) {
    /**
     * Gets the set of available dates of type [LocalDate]. Dates are converted
     * from ISO date strings (compatible with firestore) to [LocalDate].
     *
     * @return A set of available dates.
     */
    fun getAvailableLocalDates(): Set<LocalDate> {
        val result = mutableSetOf<LocalDate>()

        for (date in availableDates) {
            result.add(LocalDate.parse(date))
        }

        return result
    }

    /**
     * Gets the set of booked dates of type [LocalDate]. Dates are converted
     * from ISO date strings (compatible with firestore) to [LocalDate].
     *
     * @return A set of booked dates.
     */
    fun getBookedLocalDates(): Set<LocalDate> {
        val result = mutableSetOf<LocalDate>()

        for (date in bookedDates) {
            result.add(LocalDate.parse(date))
        }

        return result
    }

    /**
     * Checks whether the list of dates are available and not booked.
     *
     * @param dates The dates to be checked.
     * @return `true` if every specified date is available and not booked,
     * `false` otherwise.
     */
    fun isAvailable(dates: Collection<LocalDate>): Boolean {
        val available = getAvailableLocalDates()
        val booked = getBookedLocalDates()

        for (date in dates) {
            if (!available.contains(date) || booked.contains(date)) {
                return false
            }
        }

        return true
    }

    /**
     * Returns a list of dates that are not booked of the available
     * dates (free), in order.
     *
     * @return sorted list of dates which are available and not booked, in sorted order.
     */
    fun freeDates(): List<LocalDate> {
        val available = getAvailableLocalDates()
        val booked = getBookedLocalDates()

        val result = mutableListOf<LocalDate>()

        for (date in available) {
            if (!booked.contains(date)) {
                result.add(date)
            }
        }

        return result.sorted()
    }

    /**
     * Marks the dates in the list as booked.
     *
     * @param dates The dates to mark as booked.
     */
    fun markAsBooked(dates: Collection<LocalDate>) {
        val available = getAvailableLocalDates()
        val booked = getBookedLocalDates().toMutableSet()

        for (date in dates) {
            if (available.contains(date)) {
                booked.add(date)
            }
        }

        val result = mutableListOf<String>()

        for (date in booked) {
            result.add(date.toString())
        }

        bookedDates = result
    }

    /**
     * Cancels bookings for the specified dates.
     *
     * @param dates The dates to remove from booked dates.
     */
    fun cancelBookings(dates: Collection<LocalDate>) {
        val booked = getBookedLocalDates()
        val result = mutableListOf<String>()

        for (date in booked) {
            if (!dates.contains(date)) {
                result.add(date.toString())
            }
        }

        bookedDates = result
    }

    /**
     * Adds dates to the schedule's available dates.
     *
     * @param dates The dates to add.
     */
    fun addAvailableDates(dates: Collection<LocalDate>) {
        val available = getAvailableLocalDates().toMutableSet()

        for (date in dates) {
            available.add(date)
        }

        val result = mutableListOf<String>()

        for (date in available) {
            result.add(date.toString())
        }

        availableDates = result
    }

    /**
     * Removes dates from the schedule's available dates.
     *
     * @param dates The dates to remove.
     */
    fun removeAvailableDates(dates: Collection<LocalDate>) {
        val available = getAvailableLocalDates()
        val result = mutableListOf<String>()

        for (date in available) {
            if (!dates.contains(date)) {
                result.add(date.toString())
            }
        }

        availableDates = result
    }
}
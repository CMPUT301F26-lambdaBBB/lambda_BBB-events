package com.example.lambda_bbb_events.model

import java.time.LocalDate

/**
 * Represents the availability schedule for a set of dates.
 *
 * @property availableDates Dates that are available for booking.
 * @property bookedDates Dates that have already been booked.
 */
data class AvailabilitySchedule (
    var availableDates: Set<LocalDate> = emptySet(),
    var bookedDates: Set<LocalDate> = emptySet()
) {
    /**
     * Checks whether the list of dates are available and not booked.
     *
     * @param dates The dates to be checked.
     * @return `true` if every specified date is available and not booked,
     * `false` otherwise.
     */
    fun isAvailable(dates: Collection<LocalDate>): Boolean {
        for (date in dates) {
            val isInAvailableDates = date in availableDates
            val isAlreadyBooked = date in bookedDates

            if (!isInAvailableDates || isAlreadyBooked) {
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
        val freeDates = mutableListOf<LocalDate>()

        for (date in availableDates) {
            if (date !in bookedDates) {
                freeDates.add(date)
            }
        }

        return freeDates.sorted()
    }

    /**
     * Marks the dates in the list as booked.
     *
     * @param dates The dates to mark as booked.
     */
    fun markAsBooked(dates: Collection<LocalDate>) {
        val updatedBookedDates = bookedDates.toMutableSet()

        for (date in dates) {
            if (date in availableDates) {
                updatedBookedDates.add(date)
            }
        }

        bookedDates = updatedBookedDates
    }

    /**
     * Cancels bookings for the specified dates.
     *
     * @param dates The dates to remove from booked dates.
     */
    fun cancelBookings(dates: Collection<LocalDate>) {
        val updatedBookedDates = bookedDates.toMutableSet()

        for (date in dates) {
            updatedBookedDates.remove(date)
        }

        bookedDates = updatedBookedDates
    }

    /**
     * Adds dates to the schedule's available dates.
     *
     * @param dates The dates to add.
     */
    fun addAvailableDates(dates: Collection<LocalDate>) {
        val updatedAvailableDates = availableDates.toMutableSet()

        updatedAvailableDates.addAll(dates)

        availableDates = updatedAvailableDates
    }

    /**
     * Removes dates from the schedule's available dates.
     *
     * @param dates The dates to remove.
     */
    fun removeAvailableDates(dates: Collection<LocalDate>) {
        val updatedAvailableDates = availableDates.toMutableSet()

        updatedAvailableDates.removeAll(dates.toSet())

        availableDates = updatedAvailableDates
    }
}
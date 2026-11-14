package com.example.lambda_bbb_events.model

import android.util.Range
import java.time.LocalDate

data class AvailabilitySchedule (
    var dates: MutableMap<LocalDate, Int> = mutableMapOf<LocalDate, Int>(),
) {
    var currentBookings: MutableMap<String, Range<LocalDate>> = mutableMapOf()
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

    fun canBook(
        startDate: LocalDate,
        endDate: LocalDate,
        maxGuests: Int
    ): Boolean {
        var current = startDate
        while (!current.isAfter(endDate)) {
            if (current !in dates && dates[current] !!<= maxGuests) {
                return false
            }
            current = current.plusDays(1)
        }
        return true
    }

    fun addNewBooking(startDate: LocalDate, endDate: LocalDate? = null, ownerID: String, maxGuests: Int): Boolean {
        val actualEnd = endDate ?: startDate
        if (!canBook(startDate, actualEnd, maxGuests)) {
            return false
        }
        currentBookings[ownerID] = Range(startDate, endDate)
        var current = startDate
        while (!current.isAfter(actualEnd)) {
            dates[current] = dates[current]!! + 1
            current = current.plusDays(1)
        }
        return true
    }

    fun cancelBooking(startDate: LocalDate, endDate: LocalDate? = null) {
        val newEndDate = endDate ?: startDate
        var newDate = startDate
        while (newDate <= newEndDate) {
            val currentBookings = dates[newDate]
            if (currentBookings != null && currentBookings > 0) {
                dates[newDate] = currentBookings - 1
            }
            newDate = newDate.plusDays(1)
        }
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

    fun isAvailable(
        start: LocalDate,
        end: LocalDate
    ): Boolean {
        var current: LocalDate = start
        while (!current.isAfter(end)) {
            if (current !in dates || dates[current]!! > 0) {
                return false
            }
            current = current.plusDays(1)
        }
        return true
    }
}
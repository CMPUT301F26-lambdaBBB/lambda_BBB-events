package com.example.lambda_bbb_events.model

import java.time.LocalDate

data class AvailabilitySchedule (
    var dates: MutableMap<LocalDate, Int> = mutableMapOf<LocalDate, Int>(),
) {
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

    fun addNewBooking(startDate: LocalDate, endDate: LocalDate? = null) {
        val newEndDate = endDate ?: startDate
        var newDate = startDate
        while (newDate <= newEndDate) {
            dates[newDate] = (dates[newDate] ?: 0) + 1
            newDate = newDate.plusDays(1)
        }
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
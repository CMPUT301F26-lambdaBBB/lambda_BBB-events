package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.AvailabilitySchedule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import java.time.LocalDate


class AvailabilityScheduleUnitTest {
    @Test
    fun checkingLocalDatesWithEmptySchedule() {
        val availabilitySchedule = AvailabilitySchedule()

        assertTrue(availabilitySchedule.getAvailableLocalDates().isEmpty())
    }

    @Test
    fun checkingLocalDatesWithSchedule() {
        val availabilitySchedule = AvailabilitySchedule(
            availableDates = listOf("2026-01-03", "2026-01-04")
        )
        assertEquals(setOf<LocalDate>(
            LocalDate.parse("2026-01-03"),
            LocalDate.parse("2026-01-04")),
            availabilitySchedule.getAvailableLocalDates()
        )
    }

    @Test
    fun checkingLocalDatesWithDuplicatedSchedule() {
        val availabilitySchedule = AvailabilitySchedule(
            availableDates = listOf("2026-01-03", "2026-01-04", "2026-01-03")
        )
        assertEquals(setOf<LocalDate>(
            LocalDate.parse("2026-01-03"),
            LocalDate.parse("2026-01-04")),
            availabilitySchedule.getAvailableLocalDates()
        )
    }

    @Test
    fun checkingBookedLocalDatesWithEmptySchedule() {
        val availabilitySchedule = AvailabilitySchedule()

        assertTrue(availabilitySchedule.getBookedLocalDates().isEmpty())
    }

    @Test
    fun checkingBookedDatesWithSchedule() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02")
        )

        assertEquals(setOf<LocalDate>(
            LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-02")),
            availabilitySchedule.getBookedLocalDates()
        )
    }

    @Test
    fun checkingBookedDatesWithDuplicatedSchedule() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02", "2026-01-01")
        )
        assertEquals(setOf<LocalDate>(
            LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-02")),
            availabilitySchedule.getBookedLocalDates()
        )
    }

    @Test
    fun checkingIsAvailableFalse() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02"),
            availableDates = listOf("2026-01-03", "2026-01-04")
        )

        assertFalse(availabilitySchedule.isAvailable(listOf(LocalDate.parse("2026-01-02"),
            LocalDate.parse("2026-01-03"))))
    }

    @Test
    fun checkingIsAvailableTrue() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02"),
            availableDates = listOf("2026-01-03", "2026-01-04")
        )

        assertTrue(availabilitySchedule.isAvailable(listOf(LocalDate.parse("2026-01-03"),
            LocalDate.parse("2026-01-04"))))
    }

    @Test
    fun checkingIsAvailableOutsideAvailableDates() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02"),
            availableDates = listOf("2026-01-03", "2026-01-04")
        )

        assertFalse(availabilitySchedule.isAvailable(listOf(LocalDate.parse("2026-02-02"))))
    }

    @Test
    fun noDatesThatAreFree() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02"),
        )

        assertTrue(availabilitySchedule.freeDates().isEmpty())
    }

    @Test
    fun datesFreeInSortedList() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02"),
            availableDates = listOf("2026-01-04", "2026-01-03", "2026-01-05")
        )

        assertEquals(
            listOf(LocalDate.parse("2026-01-03"),
                LocalDate.parse("2026-01-04"),
                LocalDate.parse("2026-01-05")),
            availabilitySchedule.freeDates()
        )
    }

    @Test
    fun standardUseOfBookMarked() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02"),
            availableDates = listOf("2026-01-04", "2026-01-03", "2026-01-05")
        )

        availabilitySchedule.markAsBooked(listOf(LocalDate.parse("2026-01-04")))
        assertEquals(setOf<LocalDate>(LocalDate.parse("2026-01-01"),
                                        LocalDate.parse("2026-01-02"),
                                        LocalDate.parse("2026-01-04")),
                        availabilitySchedule.getBookedLocalDates()
        )
    }

    @Test
    fun bookingDateNotAvailable() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02"),
            availableDates = listOf("2026-01-03", "2026-01-05")
        )

        availabilitySchedule.markAsBooked(listOf(LocalDate.parse("2026-01-04")))
        assertEquals(setOf<LocalDate>(LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-02")),
            availabilitySchedule.getBookedLocalDates()
        )
    }

    @Test
    fun cancellingBookingWithInvalidDates() {
        val availabilitySchedule = AvailabilitySchedule(
            bookedDates = listOf("2026-01-01", "2026-01-02"),
            availableDates = listOf("2026-01-03", "2026-01-05")
        )

        availabilitySchedule.markAsBooked(listOf(LocalDate.parse("2026-01-01")))
        assertEquals(setOf<LocalDate>(LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-02")),
            availabilitySchedule.getBookedLocalDates()
        )
    }

    @Test
    fun addAvailableDates() {
        val availabilitySchedule = AvailabilitySchedule()
        availabilitySchedule.addAvailableDates(listOf(LocalDate.parse("2026-01-01")))
        assertTrue(availabilitySchedule.availableDates.contains("2026-01-01"))
    }

    @Test
    fun removeAvailableDates() {
        val availabilitySchedule = AvailabilitySchedule(listOf("2026-01-01"))
        availabilitySchedule.removeAvailableDates(listOf(LocalDate.parse("2026-01-01")))
        assertTrue(availabilitySchedule.availableDates.isEmpty())
    }
}
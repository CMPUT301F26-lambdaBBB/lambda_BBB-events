package com.example.lambda_bbb_events.model.campsite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Verifies the availability schedule's date and booking rules. */
class AvailabilityScheduleUnitTest {
    private fun day(n: Int): LocalDate = LocalDate.of(2026, 1, n)

    // Days 1 to 7 offered, nothing booked
    private fun weekSchedule(): AvailabilitySchedule {
        val schedule = AvailabilitySchedule()
        schedule.addAvailableDates(day(1), day(7))
        return schedule
    }

    @Test
    fun fullWeekIsAvailable() {
        assertTrue(weekSchedule().isAvailable(day(1), day(7)))
    }

    @Test
    fun rangeOutsideOfferedDatesIsUnavailable() {
        assertFalse(weekSchedule().isAvailable(day(6), day(8)))
    }

    @Test
    fun endBeforeStartIsUnavailable() {
        assertFalse(weekSchedule().isAvailable(day(5), day(3)))
    }

    @Test
    fun bookingTwoDaysLeavesOtherFiveOpen() {
        val schedule = weekSchedule()

        assertTrue(schedule.addNewBooking(day(1), day(2), ownerID = "a"))

        assertFalse(schedule.isAvailable(day(1), day(2)))
        assertTrue(schedule.isAvailable(day(3), day(7)))
        assertTrue(schedule.addNewBooking(day(3), day(7), ownerID = "b"))
    }

    @Test
    fun bookingInTheMiddleLeavesBothSidesOpen() {
        val schedule = weekSchedule()

        assertTrue(schedule.addNewBooking(day(4), day(6), ownerID = "a"))

        assertTrue(schedule.isAvailable(day(1), day(3)))
        assertTrue(schedule.isAvailable(day(7), day(7)))
        assertTrue(schedule.addNewBooking(day(1), day(3), ownerID = "b"))
        assertTrue(schedule.addNewBooking(day(7), ownerID = "c"))
    }

    @Test
    fun rangeOverlappingBookingIsUnavailable() {
        val schedule = weekSchedule()
        schedule.addNewBooking(day(4), day(6), ownerID = "a")

        assertFalse(schedule.isAvailable(day(3), day(4)))
        assertFalse(schedule.isAvailable(day(6), day(7)))
        assertFalse(schedule.isAvailable(day(1), day(7)))
        assertFalse(schedule.addNewBooking(day(2), day(5), ownerID = "b"))
    }

    @Test
    fun failedBookingChangesNothing() {
        val schedule = weekSchedule()
        schedule.addNewBooking(day(4), day(6), ownerID = "a")

        schedule.addNewBooking(day(2), day(5), ownerID = "b")

        assertTrue(schedule.isAvailable(day(1), day(3)))
        assertFalse("b" in schedule.currentBookings)
    }

    @Test
    fun singleDayBooking() {
        val schedule = weekSchedule()

        assertTrue(schedule.addNewBooking(day(3), ownerID = "a"))

        assertEquals(day(3)..day(3), schedule.currentBookings["a"])
        assertTrue(schedule.isAvailable(day(1), day(2)))
        assertTrue(schedule.isAvailable(day(4), day(7)))
    }

    @Test
    fun cancellingFreesOnlyThatBooking() {
        val schedule = weekSchedule()
        schedule.addNewBooking(day(1), day(3), ownerID = "a")
        schedule.addNewBooking(day(4), day(6), ownerID = "b")

        assertTrue(schedule.cancelBooking("b"))

        assertTrue(schedule.isAvailable(day(4), day(7)))
        assertFalse(schedule.isAvailable(day(1), day(3)))
    }

    @Test
    fun cancellingUnknownBookingFails() {
        assertFalse(weekSchedule().cancelBooking("nobody"))
    }

    @Test
    fun emptyScheduleHasNoDateRange() {
        val schedule = AvailabilitySchedule()

        assertNull(schedule.minDate)
        assertNull(schedule.maxDate)
        assertFalse(schedule.isAvailable(day(1), day(1)))
    }

    @Test
    fun addingDatesSetsRangeAndKeepsExistingBookings() {
        val schedule = AvailabilitySchedule()
        schedule.addAvailableDates(day(3), day(5))
        schedule.addNewBooking(day(4), ownerID = "ed")

        schedule.addAvailableDates(day(1), day(6))

        assertEquals(day(1), schedule.minDate)
        assertEquals(day(6), schedule.maxDate)
        assertEquals(1, schedule.dates[day(4)])
        assertFalse(schedule.isAvailable(day(3), day(5)))
    }

    @Test
    fun addingSingleDate() {
        val schedule = AvailabilitySchedule()

        schedule.addAvailableDates(day(2))

        assertEquals(setOf(day(2)), schedule.dates.keys)
    }

    @Test
    fun removingDatesMakesThemUnavailable() {
        val schedule = AvailabilitySchedule()
        schedule.addAvailableDates(day(1), day(7))

        schedule.removeAvailableDate(day(3), day(4))
        schedule.removeAvailableDate(day(7))
        schedule.removeAvailableDate(day(20)) // not offered, ignored

        assertEquals(setOf(day(1), day(2), day(5), day(6)), schedule.dates.keys)
        assertFalse(schedule.isAvailable(day(2), day(5)))
        assertTrue(schedule.isAvailable(day(5), day(6)))
    }

    @Test
    fun personCannotHoldTwoBookingsAtOnce() {
        val schedule = AvailabilitySchedule()
        schedule.addAvailableDates(day(1), day(7))

        assertTrue(schedule.addNewBooking(day(1), ownerID = "ed"))
        assertFalse(schedule.addNewBooking(day(5), ownerID = "ed"))

        assertTrue(schedule.cancelBooking("ed"))
        assertTrue(schedule.addNewBooking(day(5), ownerID = "ed"))
    }

    @Test
    fun cancellingBookingOnRemovedDatesDoesNotReofferThem() {
        val schedule = AvailabilitySchedule()
        schedule.addAvailableDates(day(1), day(3))
        schedule.addNewBooking(day(1), day(3), ownerID = "ed")
        schedule.removeAvailableDate(day(3))

        schedule.cancelBooking("ed")

        assertEquals(setOf(day(1), day(2)), schedule.dates.keys)
        assertTrue(schedule.isAvailable(day(1), day(2)))
    }
}

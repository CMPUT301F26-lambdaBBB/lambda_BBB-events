package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.AvailabilitySchedule
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.LotteryStatus
import com.example.lambda_bbb_events.model.Reservation
import com.example.lambda_bbb_events.model.ReservationStatus
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.UserRole
import com.example.lambda_bbb_events.model.roles.Admin
import com.example.lambda_bbb_events.model.roles.CampsiteOwner
import com.example.lambda_bbb_events.model.roles.Entrant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Date

/**
 * Tests model behaviour not covered by the per-model test classes.
 */
class ModelUnitTest {

    // Campsite

    @Test
    fun campsitesAreEqualByIdOwnerAndName() {
        val campsite = Campsite(campsiteId = "a", ownerId = "olive", campsiteName = "Lakeside", rating = 1f)
        val sameButRerated = campsite.copy(rating = 5f, description = "changed")

        assertEquals(campsite, sameButRerated)
        assertEquals(campsite.hashCode(), sameButRerated.hashCode())
        assertNotEquals(campsite, campsite.copy(campsiteName = "Forest"))
        assertNotEquals(campsite, campsite.copy(ownerId = "max"))
        assertNotEquals(campsite, campsite.copy(campsiteId = "b"))
    }

    // User

    @Test
    fun updateProfileChangesOnlyGivenFields() {
        val user = User(deviceId = "d", name = "Ed", email = "ed@example.com", phone = "1", profilePictureImageId = "img")

        user.updateProfile(name = "Edward")

        assertEquals("Edward", user.name)
        assertEquals("ed@example.com", user.email)
        assertEquals("1", user.phone)
        assertEquals("img", user.profilePictureImageId)

        user.updateProfile(email = "new@example.com", phone = "2", imageId = "img-2")
        assertEquals("new@example.com", user.email)
        assertEquals("2", user.phone)
        assertEquals("img-2", user.profilePictureImageId)
    }

    @Test
    fun updateProfileRejectsBlankFieldsWithoutChangingAnything() {
        val user = User(name = "Ed", email = "ed@example.com", phone = "1")

        assertThrows(IllegalArgumentException::class.java) { user.updateProfile(name = " ") }
        assertThrows(IllegalArgumentException::class.java) { user.updateProfile(email = "") }
        assertThrows(IllegalArgumentException::class.java) { user.updateProfile(name = "New", phone = "") }

        assertEquals("Ed", user.name)
        assertEquals("1", user.phone)
    }

    @Test
    fun setAllowNotificationsTogglesSetting() {
        val user = User()
        assertTrue(user.notificationsEnabled)

        user.setAllowNotifications(false)
        assertFalse(user.notificationsEnabled)
    }

    @Test
    fun rolesHaveMatchingRoleField() {
        assertEquals(UserRole.ENTRANT, Entrant().role)
        assertEquals(UserRole.OWNER, CampsiteOwner().role)
        assertEquals(UserRole.ADMIN, Admin().role)
    }

    // Reservation

    @Test
    fun onlyPendingAndConfirmedReservationsAreActive() {
        val active = ReservationStatus.entries.filter { Reservation(status = it).isActive() }

        assertEquals(listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED), active)
    }

    // Lottery

    @Test
    fun registrationIsOpenIncludingBothEnds() {
        val lottery = Lottery(registrationStart = Date(1_000), registrationEnd = Date(2_000))

        assertFalse(lottery.isRegistrationOpen(Date(999)))
        assertTrue(lottery.isRegistrationOpen(Date(1_000)))
        assertTrue(lottery.isRegistrationOpen(Date(2_000)))
        assertFalse(lottery.isRegistrationOpen(Date(2_001)))
    }

    @Test
    fun registrationIsOnlyOpenForOpenLotteries() {
        for (status in LotteryStatus.entries) {
            val lottery = Lottery(registrationStart = Date(1_000), registrationEnd = Date(2_000), status = status)
            assertEquals(status == LotteryStatus.OPEN, lottery.isRegistrationOpen(Date(1_500)))
        }
    }

    @Test
    fun lotteryIsFullAtCapacity() {
        assertFalse(Lottery(waitlistCapacity = 2, entryIds = listOf("a")).isFull())
        assertTrue(Lottery(waitlistCapacity = 2, entryIds = listOf("a", "b")).isFull())
        assertTrue(Lottery(waitlistCapacity = 2, entryIds = listOf("a", "b", "c")).isFull())
    }

    // AvailabilitySchedule

    private fun day(n: Int): LocalDate = LocalDate.of(2026, 1, n)

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

package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.LotteryStatus
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.Message
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LotteryControllerUnitTest {
    private val repository = mockk<LotteryRepository>()
    private lateinit var controller: LotteryController
    private val today: LocalDate = LocalDate.now()

    /** The [Date] at noon [offsetDays] days from today. */
    private fun day(offsetDays: Long): Date =
        Date.from(today.plusDays(offsetDays).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant())

    private fun lottery(
        id: String = "lottery-1",
        registration: LongRange = -1L..1L,
        booking: LongRange = 3L..5L,
        status: LotteryStatus = LotteryStatus.OPEN,
        entryIds: List<String> = emptyList()
    ) = Lottery(
        lotteryId = id,
        campsiteIds = listOf("site-1"),
        ownerIds = listOf("owner-1"),
        registrationStart = day(registration.first),
        registrationEnd = day(registration.last),
        bookingStart = day(booking.first),
        bookingEnd = day(booking.last),
        status = status,
        entryIds = entryIds,
        waitlistCapacity = 10
    )

    /** Creates a lottery with valid defaults, overriding whichever input is under test. */
    private suspend fun create(
        registrationStart: LocalDate? = today,
        registrationEnd: LocalDate? = today.plusDays(2),
        bookingStart: LocalDate? = today.plusDays(3),
        bookingEnd: LocalDate? = today.plusDays(5),
        winners: String = "2",
        capacity: String = "10"
    ) = controller.createLottery(
        ownerIds = listOf("owner-1"),
//        ownerId = "owner-1",
        campsiteIds = listOf("site-1"),
        registrationStart = registrationStart,
        registrationEnd = registrationEnd,
        bookingStart = bookingStart,
        bookingEnd = bookingEnd,
        numberOfWinnersString = winners,
        waitlistCapacityString = capacity
    )

    private suspend fun update(
        lotteryId: String = "lottery-1",
        registrationEnd: LocalDate? = today.plusDays(2),
        capacity: String = "10"
    ) = controller.updateLottery(
        lotteryId = lotteryId,
        registrationStart = today,
        registrationEnd = registrationEnd,
        bookingStart = today.plusDays(3),
        bookingEnd = today.plusDays(5),
        numberOfWinnersString = "2",
        waitlistCapacityString = capacity
    )

    @Before
    fun setup() {
        every { repository.getLotteriesForCampsite(any()) } returns emptyList()
        coEvery { repository.addLottery(any()) } just runs
        coEvery { repository.updateLottery(any()) } just runs
        controller = LotteryController(repository)
    }

    // createLottery

    @Test
    fun createLotteryWithValidInputSavesIt() = runTest {
        val saved = slot<Lottery>()
        coEvery { repository.addLottery(capture(saved)) } just runs

        assertNull(create(winners = " 2 ", capacity = " 10 "))

        assertEquals(listOf("site-1"), saved.captured.campsiteIds)
        assertEquals(listOf("owner-1"), saved.captured.ownerIds)
        assertEquals(2, saved.captured.numberOfWinners)
        assertEquals(10, saved.captured.waitlistCapacity)
        assertTrue(saved.captured.lotteryId.isNotBlank())
    }

    @Test
    fun createLotteryCoversWholeDays() = runTest {
        val saved = slot<Lottery>()
        coEvery { repository.addLottery(capture(saved)) } just runs

        create()

        val zone = ZoneId.systemDefault()
        assertEquals(today.atStartOfDay(zone).toInstant(), saved.captured.registrationStart.toInstant())
        assertEquals(
            today.plusDays(2).atTime(23, 59, 59).atZone(zone).toInstant(),
            saved.captured.registrationEnd.toInstant()
        )
    }

    @Test
    fun createLotteryRequiresAllDates() = runTest {
        assertEquals("Please pick all four dates.", create(registrationStart = null))
        assertEquals("Please pick all four dates.", create(bookingEnd = null))
        coVerify(exactly = 0) { repository.addLottery(any()) }
    }

    @Test
    fun createLotteryRejectsRegistrationEndingInThePast() = runTest {
        assertEquals(
            "The registration period must not end before today.",
            create(registrationStart = today.minusDays(5), registrationEnd = today.minusDays(1))
        )
    }

    @Test
    fun createLotteryRejectsRegistrationEndingBeforeItStarts() = runTest {
        assertEquals(
            "The registration period must end after it starts.",
            create(registrationStart = today.plusDays(2), registrationEnd = today.plusDays(1))
        )
    }

    @Test
    fun createLotteryRejectsBookingStartingBeforeRegistrationEnds() = runTest {
        val message = "The booking period must start after the registration period ends."
        assertEquals(message, create(bookingStart = today.plusDays(2)))
        assertEquals(message, create(bookingStart = today.plusDays(1)))
    }

    @Test
    fun createLotteryRejectsBookingEndingBeforeItStarts() = runTest {
        assertEquals(
            "The booking period must end after it starts.",
            create(bookingStart = today.plusDays(5), bookingEnd = today.plusDays(4))
        )
    }

    @Test
    fun createLotteryValidatesNumberOfWinners() = runTest {
        assertEquals("The number of winners is not a valid number.", create(winners = "two"))
        assertEquals("The number of winners must be at least 1.", create(winners = "0"))
    }

    @Test
    fun createLotteryValidatesWaitlistCapacity() = runTest {
        assertEquals("The waitlist capacity is not a valid number.", create(capacity = "lots"))
        assertEquals(
            "The waitlist capacity must be at least the number of winners.",
            create(winners = "5", capacity = "4")
        )
        assertNull(create(winners = "5", capacity = "5"))
    }

    @Test
    fun createLotteryWithEmptyCapacityHasNoLimit() = runTest {
        val saved = slot<Lottery>()
        coEvery { repository.addLottery(capture(saved)) } just runs

        assertNull(create(winners = "50", capacity = "   "))

        assertNull(saved.captured.waitlistCapacity)
        assertFalse(saved.captured.isFull())
    }

    @Test
    fun createLotteryRejectsOverlappingRegistration() = runTest {
        every { repository.getLotteriesForCampsite("site-1") } returns listOf(lottery(registration = 1L..4L))

        assertEquals(
            "This campsite already has a lottery with an overlapping registration period.",
            create()
        )
        coVerify(exactly = 0) { repository.addLottery(any()) }
    }

    @Test
    fun createLotteryIgnoresCancelledAndNonOverlappingLotteries() = runTest {
        every { repository.getLotteriesForCampsite("site-1") } returns listOf(
            lottery(id = "cancelled", registration = 0L..2L, status = LotteryStatus.CANCELLED),
            lottery(id = "later", registration = 10L..12L)
        )

        assertNull(create())
    }

    // updateLottery

    @Test
    fun updateMissingLotteryFails() = runTest {
        coEvery { repository.getLottery("missing") } returns null

        assertEquals("Lottery not found.", update(lotteryId = "missing"))
    }

    @Test
    fun updateCannotShrinkCapacityBelowCurrentEntries() = runTest {
        coEvery { repository.getLottery("lottery-1") } returns lottery(entryIds = listOf("a", "b", "c"))

        assertEquals(
            "The waitlist capacity must not be smaller than the number of entrants already on it.",
            update(capacity = "2")
        )
    }

    @Test
    fun updateToNoLimitIsAllowedWithExistingEntries() = runTest {
        coEvery { repository.getLottery("lottery-1") } returns lottery(entryIds = listOf("a", "b", "c"))
        val saved = slot<Lottery>()
        coEvery { repository.updateLottery(capture(saved)) } just runs

        assertNull(update(capacity = ""))
        assertNull(saved.captured.waitlistCapacity)
    }

    @Test
    fun updateIgnoresOverlapWithItself() = runTest {
        val existing = lottery(registration = 0L..2L)
        coEvery { repository.getLottery("lottery-1") } returns existing
        every { repository.getLotteriesForCampsite("site-1") } returns listOf(existing)
        val saved = slot<Lottery>()
        coEvery { repository.updateLottery(capture(saved)) } just runs

        assertNull(update())
        assertEquals("lottery-1", saved.captured.lotteryId)
    }

    @Test
    fun updateRejectsOverlapWithAnotherLottery() = runTest {
        coEvery { repository.getLottery("lottery-1") } returns lottery()
        every { repository.getLotteriesForCampsite("site-1") } returns
            listOf(lottery(id = "other", registration = 1L..3L))

        assertEquals(
            "This campsite already has a lottery with an overlapping registration period.",
            update()
        )
        coVerify(exactly = 0) { repository.updateLottery(any()) }
    }

    @Test
    fun updateValidatesInputLikeCreate() = runTest {
        coEvery { repository.getLottery("lottery-1") } returns lottery()

        assertEquals("Please pick all four dates.", update(registrationEnd = null))
        assertEquals("The waitlist capacity is not a valid number.", update(capacity = "x"))
    }

    // phases and filters

    @Test
    fun phaseFollowsTheLotteryTimeline() {
        assertEquals(LotteryPhase.CANCELLED, controller.getPhase(lottery(status = LotteryStatus.CANCELLED)))
        assertEquals(LotteryPhase.UPCOMING, controller.getPhase(lottery(registration = 1L..2L)))
        assertEquals(LotteryPhase.REGISTRATION_OPEN, controller.getPhase(lottery(registration = -1L..1L)))
        assertEquals(LotteryPhase.AWAITING_DRAW, controller.getPhase(lottery(registration = -3L..-1L, booking = 1L..3L)))
        assertEquals(LotteryPhase.BOOKING_OPEN, controller.getPhase(lottery(registration = -5L..-3L, booking = -1L..1L)))
        assertEquals(LotteryPhase.ENDED, controller.getPhase(lottery(registration = -9L..-7L, booking = -5L..-1L)))
    }

    @Test
    fun lotteriesAreFilteredByPhase() {
        val open = lottery(id = "open")
        val ended = lottery(id = "ended", registration = -9L..-7L, booking = -5L..-1L)
        val cancelled = lottery(id = "cancelled", status = LotteryStatus.CANCELLED)
        every { repository.getLotteriesForCampsite("site-1") } returns listOf(open, ended, cancelled)

        fun ids(filter: LotteryFilter) = controller.getLotteriesForCampsite("site-1", filter).map { it.lotteryId }

        assertEquals(listOf("open", "ended", "cancelled"), ids(LotteryFilter.ALL))
        assertEquals(listOf("open"), ids(LotteryFilter.ACTIVE))
        assertEquals(listOf("ended"), ids(LotteryFilter.PAST))
        assertEquals(listOf("cancelled"), ids(LotteryFilter.CANCELLED))
        assertEquals(listOf("open"), controller.getLotteriesForCampsite("site-1").map { it.lotteryId })
    }

    @Test
    fun openLotteryForCampsiteIsFirstAcceptingRegistrations() {
        val upcoming = lottery(id = "upcoming", registration = 2L..3L)
        val open = lottery(id = "open")
        every { repository.getLotteriesForCampsite("site-1") } returns listOf(upcoming, open)
        every { repository.getLotteriesForCampsite("site-2") } returns listOf(upcoming)

        assertEquals("open", controller.getOpenLotteryForCampsite("site-1")?.lotteryId)
        assertNull(controller.getOpenLotteryForCampsite("site-2"))
    }

    // delegation

    @Test
    fun simpleOperationsDelegateToRepository() = runTest {
        val existing = lottery()
        coEvery { repository.getLottery("lottery-1") } returns existing
        coEvery { repository.deleteLottery(existing) } just runs
        every { repository.joinLottery(any(), any(), any(), any()) } just runs
        val onResult: (Boolean, String) -> Unit = { _, _ -> }

        assertSame(existing, controller.getLottery("lottery-1"))
        controller.deleteLottery(existing)
        controller.joinLotteryEntry("lottery-1", "ed", 3, onResult)

        coVerify(exactly = 1) { repository.deleteLottery(existing) }
        verify(exactly = 1) { repository.joinLottery("lottery-1", "ed", 3, onResult) }
    }

    @Test
    fun cancelLotteriesOfDeletedOwnerDeletesOnlyActiveOnesAndTellsWinners() = runTest {
        val running = lottery(id = "running")
        val ended = lottery(id = "ended", registration = -10L..-8L, booking = -6L..-4L, status = LotteryStatus.DRAWN)
        val cancelled = lottery(id = "cancelled", status = LotteryStatus.CANCELLED)
        coEvery { repository.getLotteriesByOwner("owner-1") } returns listOf(running, ended, cancelled)
        val makeNotice = slot<(WaitlistEntry) -> Message>()
        coEvery { repository.deleteAndNotifyWinners("running", capture(makeNotice)) } returns 3

        val notified = controller.cancelLotteriesOfDeletedOwner("owner-1") { id ->
            if (id == "site-1") "Lakeside" else null
        }

        assertEquals(3, notified)
        coVerify(exactly = 1) { repository.deleteAndNotifyWinners(any(), any()) }

        val notice = makeNotice.captured(WaitlistEntry(entrantId = "entrant-1", lotteryId = "running"))
        assertEquals("entrant-1", notice.recipientId)
        assertEquals("owner-1", notice.senderId)
        assertTrue(notice.messageId.isNotBlank())
        assertTrue(notice.content.contains("Lakeside"))
        assertTrue(notice.content.contains("no longer valid"))
    }
}

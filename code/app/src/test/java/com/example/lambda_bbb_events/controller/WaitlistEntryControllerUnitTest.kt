package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import com.example.lambda_bbb_events.model.repository.ReservationRepository
import com.example.lambda_bbb_events.model.repository.WaitlistEntryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Date

class WaitlistEntryControllerUnitTest {
    private val repository = mockk<WaitlistEntryRepository>()
    private lateinit var controller: WaitlistEntryController

    private val oldest = WaitlistEntry(entryId = "oldest", entrantId = "ed", lotteryId = "lottery-1", joinedAt = Date(1_000))
    private val middle = WaitlistEntry(entryId = "middle", entrantId = "ed", lotteryId = "lottery-1", joinedAt = Date(2_000))
    private val newest = WaitlistEntry(entryId = "newest", entrantId = "ed", lotteryId = "lottery-1", joinedAt = Date(3_000))

    @Before
    fun setup() {
        every { repository.getEntries("lottery-1") } returns listOf(middle, oldest, newest)
        every { repository.getEntriesForEntrant("ed") } returns listOf(oldest, newest, middle)
        every { repository.waitlistEntries } returns listOf(oldest, middle, newest)
        controller = WaitlistEntryController(
            repository,
            mockk<LotteryRepository>(relaxed = true),
            mockk<ReservationRepository>(relaxed = true)
        )
    }

    @Test
    fun lotteryEntriesAreNewestFirst() {
        assertEquals(
            listOf("newest", "middle", "oldest"),
            controller.getEntriesForLottery("lottery-1").map { it.entryId }
        )
    }

    @Test
    fun entrantEntriesAreNewestFirst() {
        assertEquals(
            listOf("newest", "middle", "oldest"),
            controller.getEntriesForEntrant("ed").map { it.entryId }
        )
    }

    @Test
    fun entryIsFoundById() {
        assertEquals(middle, controller.getEntryById("middle"))
        assertNull(controller.getEntryById("missing"))
    }

    @Test
    fun leavingWaitlistDeletesEntry() = runTest {
        coEvery { repository.deleteWaitlistEntry(middle) } just runs

        controller.leaveWaitlist(middle)

        coVerify(exactly = 1) { repository.deleteWaitlistEntry(middle) }
    }
}

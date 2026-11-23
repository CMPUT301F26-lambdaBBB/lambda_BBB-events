package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.controller.ReservationController
import com.example.lambda_bbb_events.controller.ReservationFilter
import com.example.lambda_bbb_events.model.Reservation
import com.example.lambda_bbb_events.model.ReservationStatus
import com.example.lambda_bbb_events.model.repository.ReservationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ReservationControllerUnitTest {
    private val repository = mockk<ReservationRepository>()
    private lateinit var controller: ReservationController
    private val today: LocalDate = LocalDate.now()

    private fun reservation(
        id: String,
        startOffset: Long,
        endOffset: Long,
        status: ReservationStatus = ReservationStatus.CONFIRMED
    ) = Reservation(
        reservationId = id,
        campsiteId = "site-1",
        entrantId = "ed",
        startDate = today.plusDays(startOffset).toString(),
        endDate = today.plusDays(endOffset).toString(),
        status = status
    )

    private val nextWeek = reservation("next-week", 7, 9)
    private val tomorrow = reservation("tomorrow", 1, 2, ReservationStatus.PENDING)
    private val endingToday = reservation("ending-today", -2, 0)
    private val lastMonth = reservation("last-month", -30, -28)
    private val lastWeek = reservation("last-week", -7, -5)
    private val completedEarly = reservation("completed", -1, 3, ReservationStatus.COMPLETED)
    private val cancelled = reservation("cancelled", 4, 6, ReservationStatus.CANCELLED)
    private val rejected = reservation("rejected", -10, -8, ReservationStatus.REJECTED)

    @Before
    fun setup() {
        every { repository.getReservationsForCampsite("site-1") } returns listOf(
            nextWeek, tomorrow, endingToday, lastMonth, lastWeek, completedEarly, cancelled, rejected
        )
        controller = ReservationController(repository)
    }

    private suspend fun ids(filter: ReservationFilter) =
        controller.getReservationsForCampsite("site-1", filter).map { it.reservationId }

    @Test
    fun upcomingIncludesStaysThatHaveNotEndedSoonestFirst() = runTest {
        assertEquals(listOf("ending-today", "tomorrow", "next-week"), ids(ReservationFilter.UPCOMING))
    }

    @Test
    fun pastIncludesEndedAndCompletedStaysNewestFirst() = runTest {
        assertEquals(listOf("completed", "last-week", "last-month"), ids(ReservationFilter.PAST))
    }

    @Test
    fun cancelledIncludesCancelledAndRejectedNewestFirst() = runTest {
        assertEquals(listOf("cancelled", "rejected"), ids(ReservationFilter.CANCELLED))
    }

    @Test
    fun allIncludesEveryReservationNewestFirst() = runTest {
        assertEquals(
            listOf("next-week", "cancelled", "tomorrow", "completed", "ending-today", "last-week", "rejected", "last-month"),
            ids(ReservationFilter.ALL)
        )
    }

    @Test
    fun campsiteWithoutReservationsReturnsEmpty() = runTest {
        every { repository.getReservationsForCampsite("site-2") } returns emptyList()

        assertEquals(
            emptyList<Reservation>(),
            controller.getReservationsForCampsite("site-2", ReservationFilter.ALL)
        )
    }

    @Test
    fun entrantReservationsComeFromRepository() {
        every { repository.getReservationsForEntrant("ed") } returns listOf(tomorrow)

        assertEquals(listOf(tomorrow), controller.getReservationsForEntrant("ed"))
    }
}

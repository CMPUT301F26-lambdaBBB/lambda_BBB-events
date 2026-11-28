package com.example.lambda_bbb_events.model.repository

import android.util.Log
import com.example.lambda_bbb_events.model.reservation.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

/** Verifies reservation repository behavior with mocked persistence. */
class ReservationRepositoryUnitTest {
    private val db = mockk<FirebaseFirestore>()
    private val reservations = MockCollection(db, "reservations")
    private val repository = ReservationRepository(db)

    /**
     * Messaging access cannot be written to the mocked database, which is logged as an error.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Fix the github action now"
     */
    @Before
    fun silenceLogging() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun restoreLogging() {
        unmockkStatic(Log::class)
    }

    private val r1 = Reservation(reservationId = "r1", campsiteId = "site-1", entrantId = "ed")
    private val r2 = Reservation(reservationId = "r2", campsiteId = "site-2", entrantId = "ed")
    private val r3 = Reservation(reservationId = "r3", campsiteId = "site-1", entrantId = "flo")

    private fun emitAll() = reservations.emit(
        documentOf("r1", Reservation::class.java, r1),
        documentOf("r2", Reservation::class.java, r2),
        documentOf("r3", Reservation::class.java, r3)
    )

    @Test
    fun reservationsAreFilteredByCampsiteAndEntrant() {
        emitAll()

        assertEquals(listOf(r1, r3), repository.getReservationsForCampsite("site-1"))
        assertEquals(listOf(r1, r2), repository.getReservationsForEntrant("ed"))
        assertTrue(repository.getReservationsForEntrant("nobody").isEmpty())
    }

    @Test
    fun getReservationReadsCache() {
        emitAll()

        assertEquals(r2, repository.getReservation("r2"))
        assertNull(repository.getReservation("r4"))
    }

    @Test
    fun crudWritesReservationDocuments() = runTest {
        repository.addReservation(r1)
        repository.updateReservation(r1.copy(guests = 3))
        repository.deleteReservation(r1)

        verify(exactly = 1) { reservations.document("r1").set(r1) }
        verify(exactly = 1) { reservations.document("r1").set(r1.copy(guests = 3)) }
        verify(exactly = 1) { reservations.document("r1").delete() }
        assertTrue(repository.reservations.isEmpty())
    }
}

package com.example.lambda_bbb_events.model.reservation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies the reservation model's values and behavior. */
class ReservationUnitTest {
    @Test
    fun testingReservationWhenActive() {
        val trueEntries = listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        for (i in trueEntries) {
            val res = Reservation(
                status = i
            )
            assertTrue(res.isActive())
        }
    }

    @Test
    fun testingReservationWhenNotActive() {
        val falseEntries = listOf(ReservationStatus.REJECTED,
                                  ReservationStatus.CANCELLED,
                                  ReservationStatus.COMPLETED
        )
        for (i in falseEntries) {
            val res = Reservation(
                status = i
            )
            assertFalse(res.isActive())
        }
    }

    @Test
    fun onlyPendingAndConfirmedReservationsAreActive() {
        val active = ReservationStatus.entries.filter { Reservation(status = it).isActive() }

        assertEquals(listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED), active)
    }
}

package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.Reservation
import com.example.lambda_bbb_events.model.ReservationStatus
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ReservationTest {
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
}
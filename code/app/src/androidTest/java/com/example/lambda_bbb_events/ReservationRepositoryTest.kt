package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.Reservation
import com.example.lambda_bbb_events.model.ReservationStatus
import com.example.lambda_bbb_events.model.repository.ReservationRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID

class ReservationRepositoryTest {

    private suspend fun waitForCondition(
        timeoutMs: Long = 5000L,
        condition: () -> Boolean
    ): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                if (condition()) return true
            } catch (e: Exception) {
                // Ignore transient race exceptions while Firestore snapshot is updating
            }
            delay(100)
        }
        return try {
            condition()
        } catch (e: Exception) {
            false
        }
    }

    @Test
    fun addingReservationToDBAndGetReservation() = runTest {
        val repository = ReservationRepository()
        val reservationId = "test-res-${UUID.randomUUID()}"
        val reservation = Reservation(
            reservationId = reservationId,
            campsiteId = "campsite-101",
            entrantId = "entrant-202",
            ownerId = "owner-303",
            startDate = "2026-06-01",
            endDate = "2026-06-05",
            guests = 3,
            status = ReservationStatus.PENDING,
            ruleConfirmed = true
        )

        repository.addReservation(reservation)

        val success = waitForCondition {
            repository.getReservation(reservationId) != null
        }
        assertTrue("Reservation should be added to repository", success)

        val fetched = repository.getReservation(reservationId)
        assertEquals(reservation, fetched)

        repository.deleteReservation(reservation)
        waitForCondition { repository.getReservation(reservationId) == null }
    }

    @Test
    fun reservationGetsDeleted() = runTest {
        val repository = ReservationRepository()
        val reservationId = "test-res-${UUID.randomUUID()}"
        val reservation = Reservation(
            reservationId = reservationId,
            campsiteId = "campsite-101",
            entrantId = "entrant-202"
        )

        repository.addReservation(reservation)
        waitForCondition { repository.getReservation(reservationId) != null }

        repository.deleteReservation(reservation)

        val deleted = waitForCondition {
            repository.getReservation(reservationId) == null
        }
        assertTrue("Reservation should be deleted from repository", deleted)
        assertNull(repository.getReservation(reservationId))
    }

    @Test
    fun updateReservationWithNewDetails() = runTest {
        val repository = ReservationRepository()
        val reservationId = "test-res-${UUID.randomUUID()}"
        val reservation = Reservation(
            reservationId = reservationId,
            campsiteId = "campsite-101",
            entrantId = "entrant-202",
            status = ReservationStatus.PENDING
        )

        repository.addReservation(reservation)
        waitForCondition { repository.getReservation(reservationId) != null }

        val updatedReservation = reservation.copy(
            status = ReservationStatus.CONFIRMED,
            guests = 4
        )

        repository.updateReservation(updatedReservation)

        val updated = waitForCondition {
            repository.getReservation(reservationId)?.status == ReservationStatus.CONFIRMED
        }
        assertTrue("Reservation status should be updated to CONFIRMED", updated)
        assertEquals(updatedReservation, repository.getReservation(reservationId))

        repository.deleteReservation(updatedReservation)
        waitForCondition { repository.getReservation(reservationId) == null }
    }

    @Test
    fun checkReservationsReceivedByCampsite() = runTest {
        val repository = ReservationRepository()
        val campsiteId = "campsite-${UUID.randomUUID()}"
        val res1 = Reservation(
            reservationId = "res-site-1-${UUID.randomUUID()}",
            campsiteId = campsiteId,
            entrantId = "entrant-1"
        )
        val res2 = Reservation(
            reservationId = "res-site-2-${UUID.randomUUID()}",
            campsiteId = campsiteId,
            entrantId = "entrant-2"
        )

        repository.addReservation(res1)
        repository.addReservation(res2)

        val success = waitForCondition {
            val list = repository.getReservationsForCampsite(campsiteId)
            list.contains(res1) && list.contains(res2)
        }
        assertTrue("Reservations for campsite should be returned", success)

        val results = repository.getReservationsForCampsite(campsiteId)
        assertTrue(results.contains(res1))
        assertTrue(results.contains(res2))

        repository.deleteReservation(res1)
        repository.deleteReservation(res2)
        waitForCondition {
            repository.getReservation(res1.reservationId) == null &&
                    repository.getReservation(res2.reservationId) == null
        }
    }

    @Test
    fun checkReservationsReceivedByEntrant() = runTest {
        val repository = ReservationRepository()
        val entrantId = "entrant-${UUID.randomUUID()}"
        val res1 = Reservation(
            reservationId = "res-ent-1-${UUID.randomUUID()}",
            campsiteId = "campsite-1",
            entrantId = entrantId
        )
        val res2 = Reservation(
            reservationId = "res-ent-2-${UUID.randomUUID()}",
            campsiteId = "campsite-2",
            entrantId = entrantId
        )

        repository.addReservation(res1)
        repository.addReservation(res2)

        val success = waitForCondition {
            val list = repository.getReservationsForEntrant(entrantId)
            list.contains(res1) && list.contains(res2)
        }
        assertTrue("Reservations for entrant should be returned", success)

        val results = repository.getReservationsForEntrant(entrantId)
        assertTrue(results.contains(res1))
        assertTrue(results.contains(res2))

        repository.deleteReservation(res1)
        repository.deleteReservation(res2)
        waitForCondition {
            repository.getReservation(res1.reservationId) == null &&
                    repository.getReservation(res2.reservationId) == null
        }
    }

    @Test
    fun checkInvalidReservationReturnsNullOrEmpty() = runTest {
        val repository = ReservationRepository()

        assertNull(repository.getReservation("non-existent-res-id-999"))
        assertTrue(repository.getReservationsForCampsite("non-existent-campsite-999").isEmpty())
        assertTrue(repository.getReservationsForEntrant("non-existent-entrant-999").isEmpty())
    }
}

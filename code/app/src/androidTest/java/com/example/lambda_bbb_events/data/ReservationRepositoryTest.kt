package com.example.lambda_bbb_events.data

import com.example.lambda_bbb_events.model.reservation.Reservation
import com.example.lambda_bbb_events.model.reservation.ReservationStatus
import com.example.lambda_bbb_events.model.repository.ReservationRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Tests [ReservationRepository] against the real Firestore database.
 *
 * Every reservation uses a unique ID and is deleted after each test in [cleanUp].
 */
class ReservationRepositoryTest {
    private val testData = FirestoreTestData()
    private lateinit var repository: ReservationRepository

    @Before
    fun setUp() {
        repository = ReservationRepository()
    }

    @After
    fun cleanUp() {
        repository.stopListening()
        testData.cleanUp()
    }

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
        val reservationId = testData.newId("reservations", prefix = "test-res")
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
    }

    @Test
    fun reservationGetsDeleted() = runTest {
        val reservationId = testData.newId("reservations", prefix = "test-res")
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
        val reservationId = testData.newId("reservations", prefix = "test-res")
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
    }

    @Test
    fun checkReservationsReceivedByCampsite() = runTest {
        val campsiteId = "campsite-${UUID.randomUUID()}"
        val res1 = Reservation(
            reservationId = testData.newId("reservations", prefix = "res-site-1"),
            campsiteId = campsiteId,
            entrantId = "entrant-1"
        )
        val res2 = Reservation(
            reservationId = testData.newId("reservations", prefix = "res-site-2"),
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
    }

    @Test
    fun checkReservationsReceivedByEntrant() = runTest {
        val entrantId = "entrant-${UUID.randomUUID()}"
        val res1 = Reservation(
            reservationId = testData.newId("reservations", prefix = "res-ent-1"),
            campsiteId = "campsite-1",
            entrantId = entrantId
        )
        val res2 = Reservation(
            reservationId = testData.newId("reservations", prefix = "res-ent-2"),
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
    }

    @Test
    fun checkInvalidReservationReturnsNullOrEmpty() = runTest {

        assertNull(repository.getReservation("non-existent-res-id-999"))
        assertTrue(repository.getReservationsForCampsite("non-existent-campsite-999").isEmpty())
        assertTrue(repository.getReservationsForEntrant("non-existent-entrant-999").isEmpty())
    }
}

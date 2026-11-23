package com.example.lambda_bbb_events.data

import com.example.lambda_bbb_events.model.campsite.AvailabilitySchedule
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

/**
 * Tests [CampsiteRepository] against the real Firestore database.
 *
 * Every campsite uses a unique ID and is deleted after each test in [cleanUp].
 */
class CampsiteRepositoryTests {
    private val testData = FirestoreTestData()
    private lateinit var repository: CampsiteRepository

    @Before
    fun setUp() {
        repository = CampsiteRepository()
    }

    @After
    fun cleanUp() {
        repository.stopListening()
        testData.cleanUp()
    }

    private fun newCampsite(ownerId: String = "owner-${UUID.randomUUID()}") = Campsite(
        campsiteId = testData.newId("campsites", prefix = "test-campsite"),
        ownerId = ownerId,
        campsiteName = "Test Campsite",
        address = "1 Test Rd",
        maxOccupancy = 4,
        tags = listOf("lake")
    )

    @Test
    fun addedCampsiteCanBeReadBack() = runTest {
        val campsite = newCampsite()

        repository.addCampsite(campsite)
        val stored = repository.get(campsite.campsiteId)

        assertEquals(campsite, stored)
        assertEquals("1 Test Rd", stored?.address)
        assertEquals(4, stored?.maxOccupancy)
        assertEquals(listOf("lake"), stored?.tags)
    }

    @Test
    fun availabilityDatesSurviveTheRoundTrip() = runTest {
        val availability = AvailabilitySchedule()
        availability.addAvailableDates(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3))
        availability.addNewBooking(LocalDate.of(2026, 7, 2), ownerID = "ed")
        val campsite = newCampsite().copy(availability = availability)

        repository.addCampsite(campsite)

        assertEquals(availability.dates, repository.get(campsite.campsiteId)?.availability?.dates)
    }

    @Test
    fun deletedCampsiteIsGone() = runTest {
        val campsite = newCampsite()
        repository.addCampsite(campsite)

        repository.deleteCampsite(campsite.campsiteId)

        assertNull(repository.get(campsite.campsiteId))
        assertNull(repository.getCached(campsite.campsiteId))
    }

    @Test
    fun updateReplacesCampsiteDetails() = runTest {
        val campsite = newCampsite()
        repository.addCampsite(campsite)

        repository.updateCampsite(campsite.campsiteId, campsite.copy(description = "Updated", maxOccupancy = 6))
        val stored = repository.get(campsite.campsiteId)

        assertEquals("Updated", stored?.description)
        assertEquals(6, stored?.maxOccupancy)
    }

    @Test
    fun getAllCampsitesIncludesOnlyExistingCampsites() = runTest {
        val ownerId = "owner-${UUID.randomUUID()}"
        val first = newCampsite(ownerId)
        val second = newCampsite(ownerId)
        repository.addCampsite(first)
        repository.addCampsite(second)
        repository.deleteCampsite(second.campsiteId)

        val all = repository.getAllCampsites()

        assertTrue(first.campsiteId in all)
        assertFalse(second.campsiteId in all)
        assertEquals(listOf(first.campsiteId), all.values.filter { it.ownerId == ownerId }.map { it.campsiteId })
    }

    @Test
    fun missingCampsiteIsNull() = runTest {
        assertNull(repository.get("this-campsite-does-not-exist-${UUID.randomUUID()}"))
    }
}

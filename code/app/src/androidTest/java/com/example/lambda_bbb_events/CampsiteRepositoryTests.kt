package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.AvailabilitySchedule
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

/**
 * Tests [CampsiteRepository] against the real Firestore database.
 *
 * Every campsite uses a random ID and is deleted afterwards.
 */
class CampsiteRepositoryTests {
    private val repository = CampsiteRepository()
    private val created = mutableListOf<String>()

    private fun newCampsite(ownerId: String = "owner-${UUID.randomUUID()}") = Campsite(
        campsiteId = "test-${UUID.randomUUID()}",
        ownerId = ownerId,
        campsiteName = "Test Campsite",
        address = "1 Test Rd",
        maxOccupancy = 4,
        tags = listOf("lake")
    ).also { created.add(it.campsiteId) }

    @After
    fun cleanUp() = runTest {
        created.forEach { repository.deleteCampsite(it) }
    }

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
        val availability = AvailabilitySchedule().apply {
            addAvailableDates(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3))
            addNewBooking(LocalDate.of(2026, 7, 2), ownerID = "ed")
        }
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
    fun observeAllCampsitesIncludesAddedCampsite() = runTest {
        val campsite = newCampsite()
        repository.addCampsite(campsite)

        val observed = repository.observeAllCampsites().first { campsite.campsiteId in it }

        assertEquals(campsite, observed[campsite.campsiteId])
    }

    @Test
    fun missingCampsiteIsNull() = runTest {
        assertNull(repository.get("this-campsite-does-not-exist-${UUID.randomUUID()}"))
    }
}

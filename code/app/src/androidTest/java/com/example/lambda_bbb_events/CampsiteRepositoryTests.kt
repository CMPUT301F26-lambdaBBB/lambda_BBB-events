package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID

class CampsiteRepositoryTests {
    @Test
    fun addingCampsiteToDBAndGetCampsites() = runTest {
        val campsite = Campsite(
            campsiteId = UUID.randomUUID().toString(),
            ownerId = "Test Location"
        )
        val campsiteRepository = CampsiteRepository()
        campsiteRepository.addCampsite(campsite)
        assertEquals(campsite, campsiteRepository.get(campsite.campsiteId))
        campsiteRepository.deleteCampsite(campsite)
        campsiteRepository.close()
    }

    @Test
    fun campsiteGetsDeleted() = runTest {
        val campsite = Campsite(
            campsiteId = UUID.randomUUID().toString(),
            ownerId = "Test Location"
        )
        val campsiteRepository = CampsiteRepository()
        campsiteRepository.addCampsite(campsite)
        campsiteRepository.deleteCampsite(campsite)
        assertEquals(null, campsiteRepository.get(campsite.campsiteId))
        campsiteRepository.close()
    }

    @Test
    fun updateCampsiteWithNewDetails() = runTest {
        val campsite = Campsite(
            campsiteId = UUID.randomUUID().toString(),
            ownerId = "Test Location"
        )

        val updatedCampsite = campsite.copy(
            ownerId = "Updated Test Location"
        )

        val repository = CampsiteRepository()

        repository.addCampsite(campsite)
        repository.updateCampsite(updatedCampsite)

        assertEquals(
            updatedCampsite,
            repository.get(campsite.campsiteId)
        )

        repository.deleteCampsite(updatedCampsite)
        repository.close()
    }

    @Test
    fun checkCampsitesReceivedAreValidByOwner() = runTest {
        val ownerId = "owner-${UUID.randomUUID()}"

        val ownerCampsites = listOf(
            Campsite("Yuta Man Site 1", ownerId),
            Campsite("Test Man Site 2", ownerId),
            Campsite("Test Man Site 3", ownerId)
        )

        val repository = CampsiteRepository()

        ownerCampsites.forEach {
            repository.addCampsite(it)
        }

        val result = repository.getCampsitesByOwner(ownerId)

        ownerCampsites.forEach {
            assertTrue(result.contains(it))
        }

        ownerCampsites.forEach {
            repository.deleteCampsite(it)
        }
        repository.close()

    }

    @Test
    fun checkCampsitesReceivedAreInvalidByOwner() = runTest {
        val ownerCampsites = listOf(
            Campsite("Yuta Man Site 1", "Yokai Toyota"),
            Campsite("Test Man Site 2", "Yokai Toyota"),
            Campsite("Test Man Site 3", "Yokai Toyota")
        )
        val campsiteRepository = CampsiteRepository()

        for (campsite in ownerCampsites) {
            campsiteRepository.addCampsite(campsite)
        }
        assertTrue(campsiteRepository.getCampsitesByOwner("Yutah").isEmpty())

        for (campsite in ownerCampsites) {
            campsiteRepository.deleteCampsite(campsite)
        }
        campsiteRepository.close()
    }

    @Test
    fun filterCampsitesCheck() = runTest {
        val ownerCampsites = listOf(
            Campsite("Yuta Man Site 1", "Yokai Toyota"),
            Campsite("Test Man Site 2", "Yokai Toyota"),
            Campsite("Test Man Site 3", "Yokai Toyota")
        )
        val campsiteRepository = CampsiteRepository()

        for (campsite in ownerCampsites) {
            campsiteRepository.addCampsite(campsite)
        }

        campsiteRepository.filterCampsites(
            startDate = null,
            endDate = null,
            location = "",
            minGuests = 0,
            maxGuests = 100,
            minRating = null
        )

        val filteredIds = campsiteRepository.filteredCampsites
            .map { it.campsiteId }

        ownerCampsites.forEach {
            assertTrue(it.campsiteId in filteredIds)
        }

        for (campsite in ownerCampsites) {
            campsiteRepository.deleteCampsite(campsite)
        }
    }

    @Test
    fun clearingFilter() = runTest {
        val repository = CampsiteRepository()

        repository.filterCampsites(
            startDate = null,
            endDate = null,
            location = "",
            minGuests = 0,
            maxGuests = 100,
            minRating = null
        )

        repository.clearExploreFilters()

        assertTrue(repository.filteredCampsites.isEmpty())
        assertFalse(repository.isExploreFiltered)
        assertFalse(repository.isFiltering)
        assertNull(repository.filterError)

        repository.close()
    }
}
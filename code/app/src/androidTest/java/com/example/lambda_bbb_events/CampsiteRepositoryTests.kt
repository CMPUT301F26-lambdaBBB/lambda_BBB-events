package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.controller.CampsiteController
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CampsiteRepositoryTests {
    @Test
    fun addingCampsiteToDBAndGetCampsites() = runTest {
        val campsite = Campsite("Test Campsite",
            "Test Location")
        val campsiteRepository = CampsiteRepository()
        campsiteRepository.addCampsite(campsite)
        assertEquals(campsite, campsiteRepository.get(campsite.campsiteId))
        campsiteRepository.deleteCampsite(campsite)
    }

    @Test
    fun campsiteGetsDeleted() = runTest {
        val campsite = Campsite("Test Campsite",
            "Test Location")
        val campsiteRepository = CampsiteRepository()
        campsiteRepository.addCampsite(campsite)
        campsiteRepository.deleteCampsite(campsite)
        assertEquals(null, campsiteRepository.get(campsite.campsiteId))
    }

    @Test
    fun updateCampsiteWithNewDetails() = runTest {
        val campsite = Campsite("Test Campsite",
            "Test Location")
        val newCampsite = Campsite("Updated Test Campsite",
            "Updated Test Location")
        val campsiteRepository = CampsiteRepository()
        campsiteRepository.addCampsite(campsite)
        campsiteRepository.updateCampsite(newCampsite)
        assertEquals(newCampsite, campsiteRepository.get(campsite.campsiteId))
        campsiteRepository.deleteCampsite(newCampsite)
    }

    @Test
    fun checkCampsitesReceivedAreValidByOwner() = runTest {
        val ownerCampsites = listOf(
            Campsite("Yuta Man Site 1", "Yokai Toyota"),
            Campsite("Test Man Site 2", "Yokai Toyota"),
            Campsite("Test Man Site 3", "Yokai Toyota")
        )
        val campsiteRepository = CampsiteRepository()

        for (campsite in ownerCampsites) {
            campsiteRepository.addCampsite(campsite)
        }
        assertEquals(ownerCampsites, CampsiteRepository().getCampsitesByOwner("Yokai Toyota"))

        for (campsite in ownerCampsites) {
            val campsiteRepository = CampsiteRepository()
            campsiteRepository.deleteCampsite(campsite)
        }
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
        assertTrue(CampsiteRepository().getCampsitesByOwner("Yutah").isEmpty())

        for (campsite in ownerCampsites) {
            val campsiteRepository = CampsiteRepository()
            campsiteRepository.deleteCampsite(campsite)
        }
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
            val campsiteRepository = CampsiteRepository()
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
    }
}
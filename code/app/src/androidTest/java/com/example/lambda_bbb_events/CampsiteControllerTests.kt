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

class CampsiteControllerTests {
    @Test
    fun addingCampsiteAndGettingTheCampsite() = runTest {
        val campsite = Campsite(campsiteId = "testCampsite", campsiteName = "Test Camp")
        val controller = CampsiteController(CampsiteRepository())

        controller.addCampsite(campsite)

        assertEquals(campsite, controller.getCampsite("testCampsite"))
    }

    @Test
    fun gettingACampsiteThatDoesNotExistReturnsNull() = runTest {
        val controller = CampsiteController(CampsiteRepository())

        assertNull(controller.getCampsite("thisCampsiteDoesntExist!!"))
    }

    @Test
    fun gettingCampsitesByOwnerOnlyReturnsThatOwnersCampsites() = runTest {
        val ownersCampsite = Campsite(campsiteId = "testCampsite", ownerId = "testOwner")
        val otherCampsite = Campsite(campsiteId = "testCampsite2", ownerId = "otherOwner")
        val controller = CampsiteController(CampsiteRepository())

        controller.addCampsite(ownersCampsite)
        controller.addCampsite(otherCampsite)

        val campsites = controller.getCampsitesByOwner("testOwner")
        val resultIds = mutableListOf<String>()
        for (campsite in campsites) {
            resultIds.add(campsite.campsiteId)
        }

        assertTrue("testCampsite" in resultIds)
        assertFalse("testCampsite2" in resultIds)
    }

    @Test
    fun updatingCampsiteWorks() = runTest {
        val campsite = Campsite(
            campsiteId = "testCampsite",
            campsiteName = "Old Name",
            address = "Old Address",
            maxOccupancy = 2
        )
        val controller = CampsiteController(CampsiteRepository())

        controller.addCampsite(campsite)

        // extra spaces are on purpose bcs the controller should trim them (remove whitespace)
        val wasUpdated = controller.updateCampsite(
            campsiteId = "testCampsite",
            name = "  New Name  ",
            address = "  New Address  ",
            maxOccupancyText = " 5 ",
            description = "New description",
            rulesPolicies = "New rules",
            tags = listOf("lake", "quiet")
        )

        val updated = controller.getCampsite("testCampsite")

        assertTrue(wasUpdated)
        assertEquals("New Name", updated?.campsiteName)
        assertEquals("New Address", updated?.address)
        assertEquals(5, updated?.maxOccupancy)
        assertEquals("New description", updated?.description)
        assertEquals("New rules", updated?.rulesPolicies)
        assertEquals(listOf("lake", "quiet"), updated?.tags)
    }

    @Test
    fun updatingACampsiteThatDoesNotExistFails() = runTest {
        val controller = CampsiteController(CampsiteRepository())

        val wasUpdated = controller.updateCampsite(
            campsiteId = "thisCampsiteDoesntExist!!",
            name = "Name",
            address = "Address",
            maxOccupancyText = "5",
            description = "",
            rulesPolicies = "",
            tags = emptyList()
        )

        assertFalse(wasUpdated)
    }

    @Test
    fun updatingCampsiteWithBlankNameFails() = runTest {
        val campsite = Campsite(campsiteId = "testCampsite", campsiteName = "Old Name")
        val controller = CampsiteController(CampsiteRepository())

        controller.addCampsite(campsite)

        val wasUpdated = controller.updateCampsite(
            campsiteId = "testCampsite",
            name = "   ",
            address = "Address",
            maxOccupancyText = "5",
            description = "",
            rulesPolicies = "",
            tags = emptyList()
        )

        assertFalse(wasUpdated)
    }

    @Test
    fun updatingCampsiteWithBlankAddressFails() = runTest {
        val campsite = Campsite(campsiteId = "testCampsite")
        val controller = CampsiteController(CampsiteRepository())

        controller.addCampsite(campsite)

        val wasUpdated = controller.updateCampsite(
            campsiteId = "testCampsite",
            name = "Name",
            address = "",
            maxOccupancyText = "5",
            description = "",
            rulesPolicies = "",
            tags = emptyList()
        )

        assertFalse(wasUpdated)
    }

    @Test
    fun updatingCampsiteWithInvalidMaxOccupancyFails() = runTest {
        val campsite = Campsite(campsiteId = "testCampsite")
        val controller = CampsiteController(CampsiteRepository())

        controller.addCampsite(campsite)

        // not a number
        val notANumber = controller.updateCampsite(
            campsiteId = "testCampsite",
            name = "Name",
            address = "Address",
            maxOccupancyText = "abc",
            description = "",
            rulesPolicies = "",
            tags = emptyList()
        )

        // less than 1
        val zero = controller.updateCampsite(
            campsiteId = "testCampsite",
            name = "Name",
            address = "Address",
            maxOccupancyText = "0",
            description = "",
            rulesPolicies = "",
            tags = emptyList()
        )

        assertFalse(notANumber)
        assertFalse(zero)
    }
}
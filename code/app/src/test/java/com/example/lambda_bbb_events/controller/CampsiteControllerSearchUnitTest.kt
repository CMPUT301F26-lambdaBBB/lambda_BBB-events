package com.example.lambda_bbb_events.controller

import androidx.compose.runtime.MutableState
import com.example.lambda_bbb_events.model.campsite.AvailabilitySchedule
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.campsite.CampsiteFilters
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.store.CampsiteStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Tests [CampsiteController] searching, filtering, and editing.
 */
class CampsiteControllerSearchUnitTest {
    private val store = mockk<CampsiteStore>()
    private val campsites = mutableMapOf<String, Campsite>()
    private lateinit var controller: CampsiteController

    private val july1: LocalDate = LocalDate.of(2026, 7, 1)

    private fun availableFrom(start: LocalDate, days: Long): AvailabilitySchedule {
        val schedule = AvailabilitySchedule()
        schedule.addAvailableDates(start, start.plusDays(days - 1))
        return schedule
    }

    private val lakeside = Campsite(
        campsiteId = "lakeside",
        ownerId = "olive",
        campsiteName = "Lakeside Retreat",
        address = "1 Lake Rd, Banff",
        maxOccupancy = 4,
        rating = 4.5f,
        tags = listOf("Fishing", "Quiet"),
        availability = availableFrom(july1, 7)
    )
    private val forest = Campsite(
        campsiteId = "forest",
        ownerId = "olive",
        campsiteName = "Forest Hideout",
        address = "9 Pine St, Jasper",
        maxOccupancy = 8,
        rating = 3f,
        tags = listOf("Hiking"),
        availability = availableFrom(july1.plusDays(3), 7)
    )
    private val meadow = Campsite(
        campsiteId = "meadow",
        ownerId = "max",
        campsiteName = "Meadow Field",
        address = "22 Prairie Ave, Canmore",
        maxOccupancy = 2,
        rating = 5f,
        tags = listOf("Lake view")
    )

    @Before
    fun setup() {
        campsites.putAll(listOf(lakeside, forest, meadow).associateBy { it.campsiteId })
        every { store.campsites } returns campsites
        every { store.updateCampsite(any(), any(), any()) } answers {
            campsites[firstArg()] = secondArg()
            true
        }
        controller = CampsiteController(store, scope = TestScope())
    }

    private fun Map<String, Campsite>.ids() = keys.sorted()
    private fun List<Campsite>.ids() = map { it.campsiteId }.sorted()

    // filters

    @Test
    fun noFiltersReturnsEverything() {
        assertEquals(listOf("forest", "lakeside", "meadow"), controller.filteredCampsites.ids())
    }

    @Test
    fun dateFilterKeepsCampsitesAvailableForWholeStay() {
        assertEquals(listOf("lakeside"), controller.applyFilters(startDate = july1, endDate = july1.plusDays(2)).ids())
        assertEquals(
            listOf("forest", "lakeside"),
            controller.applyFilters(startDate = july1.plusDays(3), endDate = july1.plusDays(6)).ids()
        )
    }

    @Test
    fun startDateAloneFiltersOnThatDay() {
        assertEquals(listOf("lakeside"), controller.applyFilters(startDate = july1).ids())
    }

    @Test
    fun starRatingIsAMinimum() {
        assertEquals(listOf("lakeside", "meadow"), controller.applyFilters(starRating = 4.5).ids())
        assertEquals(listOf("meadow"), controller.applyFilters(starRating = 4.6).ids())
    }

    @Test
    fun guestRangeMatchesMaxOccupancy() {
        assertEquals(listOf("lakeside", "meadow"), controller.applyFilters(guestRange = 1..4).ids())
    }

    @Test
    fun guestOptionsAreTheSizesOfActiveCampsites() {
        assertEquals(listOf(2, 4, 8), controller.guestOptions)

        campsites["forest"] = forest.copy(active = false)
        campsites["hall"] = meadow.copy(campsiteId = "hall", maxOccupancy = 9999)
        campsites["sameSize"] = meadow.copy(campsiteId = "sameSize")
        assertEquals(listOf(2, 4, 9999), controller.guestOptions)
    }

    @Test
    fun guestOptionsIgnoreFilters() {
        controller.applyFilters(guestRange = 3..4)

        assertEquals(listOf(2, 4, 8), controller.guestOptions)
    }

    @Test
    fun noCampsitesHaveNoGuestOptions() {
        campsites.clear()

        assertEquals(emptyList<Int>(), controller.guestOptions)
    }

    @Test
    fun locationMatchesAddressOrNameIgnoringCase() {
        assertEquals(listOf("forest"), controller.applyFilters(location = "  jasper ").ids())
        assertEquals(listOf("meadow"), controller.applyFilters(location = "MEADOW").ids())
    }

    @Test
    fun filtersCombine() {
        assertEquals(
            listOf("lakeside"),
            controller.applyFilters(starRating = 4.0, guestRange = 3..10).ids()
        )
    }

    @Test
    fun appliedFiltersCanBeRead() {
        assertEquals(CampsiteFilters(), controller.filters)

        controller.applyFilters(july1, july1.plusDays(2), " Banff ", 4.0, 2..8)
        assertEquals(
            CampsiteFilters(july1, july1.plusDays(2), "Banff", 4.0, 2..8),
            controller.filters
        )

        controller.clearFilters()
        assertEquals(CampsiteFilters(), controller.filters)
    }

    @Test
    fun clearFiltersReturnsEverything() {
        controller.applyFilters(starRating = 5.0, location = "Canmore")

        controller.clearFilters()

        assertEquals(listOf("forest", "lakeside", "meadow"), controller.filteredCampsites.ids())
    }

    // search

    @Test
    fun searchMatchesNameAddressAndTagsIgnoringCase() {
        controller.search("  LAKE ")

        assertEquals("LAKE", controller.searchQuery)
        assertEquals(listOf("lakeside", "meadow"), controller.getSearchResults().ids())

        controller.search("hiking")
        assertEquals(listOf("forest"), controller.getSearchResults().ids())
    }

    @Test
    fun blankSearchReturnsFilteredCampsites() {
        controller.search("   ")
        controller.applyFilters(guestRange = 5..10)

        assertEquals(listOf("forest"), controller.getSearchResults().ids())
    }

    @Test
    fun searchAppliesOnTopOfFilters() {
        controller.applyFilters(starRating = 5.0)
        controller.search("lake")

        assertEquals(listOf("meadow"), controller.getSearchResults().ids())
    }

    // lookups

    @Test
    fun campsitesAreFoundByIdAndOwner() {
        assertEquals(forest, controller.getCampsite("forest"))
        assertNull(controller.getCampsite("missing"))
        assertEquals(listOf("forest", "lakeside"), controller.getCampsitesByOwner("olive").ids())
        assertTrue(controller.getCampsitesByOwner("nobody").isEmpty())
    }

    // editing

    @Test
    fun updateTrimsInputAndKeepsOtherFields() {
        val images = listOf(ImageMetaData(imageId = "img-1"))
        campsites["lakeside"] = lakeside.copy(imageIds = images, latitude = 51.0, ratingCount = 9)

        assertTrue(
            controller.updateCampsite(
                campsiteId = "lakeside",
                name = "  New Name  ",
                address = "  New Address  ",
                maxOccupancyText = " 5 ",
                description = " New description ",
                rulesPolicies = " New rules ",
                tags = listOf("lake", "quiet")
            )
        )

        val updated = campsites.getValue("lakeside")
        assertEquals("New Name", updated.campsiteName)
        assertEquals("New Address", updated.address)
        assertEquals(5, updated.maxOccupancy)
        assertEquals("New description", updated.description)
        assertEquals("New rules", updated.rulesPolicies)
        assertEquals(listOf("lake", "quiet"), updated.tags)
        // fields the form does not edit are kept
        assertEquals("lakeside", updated.campsiteId)
        assertEquals("olive", updated.ownerId)
        assertEquals(images, updated.imageIds)
        assertEquals(51.0, updated.latitude, 0.0)
        assertEquals(9, updated.ratingCount)
        assertEquals(lakeside.availability, updated.availability)
    }

    private fun updateLakeside(
        name: String = "Name",
        address: String = "Address",
        maxOccupancyText: String = "5"
    ) = controller.updateCampsite("lakeside", name, address, maxOccupancyText, "", "", emptyList())

    @Test
    fun updateRejectsInvalidInput() {
        assertFalse(updateLakeside(name = "   "))
        assertFalse(updateLakeside(address = ""))
        assertFalse(updateLakeside(maxOccupancyText = "abc"))
        assertFalse(updateLakeside(maxOccupancyText = "0"))
        assertFalse(updateLakeside(maxOccupancyText = "-2"))

        verify(exactly = 0) { store.updateCampsite(any(), any(), any()) }
        assertEquals(lakeside.campsiteName, campsites.getValue("lakeside").campsiteName)
    }

    @Test
    fun updateMissingCampsiteFails() {
        assertFalse(controller.updateCampsite("missing", "Name", "Address", "5", "", "", emptyList()))
    }

    @Test
    fun addAndRemoveDelegateToStore() {
        val added = slot<String>()
        every {
            store.addCampsite(
                campsiteId = capture(added), ownerId = any(), campsiteName = any(), description = any(),
                address = any(), latitude = any(), longitude = any(), maxOccupancy = any(),
                rulesPolicies = any(), amenities = any(), tags = any(), imageIds = any(), rating = any(),
                ratingCount = any(), availability = any(), active = any(), returnMessage = any()
            )
        } returns true
        every { store.removeCampsite(any(), any()) } returns true
        val message = mockk<MutableState<String>>(relaxed = true)

        assertTrue(controller.addCampsite(campsiteId = "new", campsiteName = "New"))
        assertTrue(controller.removeCampsite(forest, message))

        assertEquals("new", added.captured)
        verify(exactly = 1) { store.removeCampsite(forest, message) }
    }
}

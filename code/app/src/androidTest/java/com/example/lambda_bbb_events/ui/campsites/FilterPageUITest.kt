package com.example.lambda_bbb_events.ui.campsites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.CampsiteController
import com.example.lambda_bbb_events.model.campsite.CampsiteFilters
import com.example.lambda_bbb_events.view.ui.components.filter.HomeCampsiteFilter
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FilterPageUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val campsiteController =
        mockk<CampsiteController>(relaxed = true)

    private var showResultsCalled = false
    private var filters = CampsiteFilters()
    private var isOpen by mutableStateOf(true)
    private var guestOptions by mutableStateOf(listOf(10, 2000, 9999))

    @Before
    fun setup() {
        showResultsCalled = false
        // Remembers the filters like the real controller
        every { campsiteController.filters } answers { filters }
        every { campsiteController.applyFilters(any(), any(), any(), any(), any()) } answers {
            filters = CampsiteFilters(arg(0), arg(1), arg(2), arg(3), arg(4))
            emptyMap()
        }
        every { campsiteController.guestOptions } answers { guestOptions }

        composeTestRule.setContent {
            if (isOpen) {
                HomeCampsiteFilter(
                    campsiteController = campsiteController,
                    onDismiss = {},
                    onShowResults = {
                        showResultsCalled = true
                    }
                )
            }
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    private fun showResults() {
        composeTestRule.onNodeWithTag("showResultsButton").performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    private fun reopen() {
        isOpen = false
        composeTestRule.waitForIdle()
        isOpen = true
        composeTestRule.waitForIdle()
    }

    private fun enterFilters() {
        composeTestRule.onNodeWithTag("fromDateInput").performTextInput("20261210")
        composeTestRule.onNodeWithTag("toDateInput").performTextInput("20261215")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Banff")
        dragSlider(from = 0.98f, to = 0.5f)
        composeTestRule.onNodeWithTag("ratingStar3").performScrollTo().performClick()
    }

    private val enteredFilters = CampsiteFilters(
        LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 15), "Banff", 3.0, 10..2000
    )

    @Test
    fun reopeningShowsTheAppliedFilters() {
        enterFilters()
        showResults()
        assertEquals(enteredFilters, filters)
        reopen()

        composeTestRule.onNodeWithTag("fromDateInput").assertTextEquals("2026-12-10")
        composeTestRule.onNodeWithTag("toDateInput").assertTextEquals("2026-12-15")
        composeTestRule.onNodeWithTag("locationInput").assertTextEquals("Banff")
        composeTestRule.onNodeWithText("10-2000").assertExists()
        composeTestRule.onNodeWithTag("ratingStar3").assertIsSelected()
        composeTestRule.onNodeWithTag("ratingStar4").assertIsNotSelected()
    }

    @Test
    fun changingOneFilterKeepsTheOthers() {
        enterFilters()
        showResults()
        reopen()
        composeTestRule.onNodeWithTag("ratingStar5").performScrollTo().performClick()
        showResults()
        assertEquals(enteredFilters.copy(starRating = 5.0), filters)

        // Dates stay the same however many times the filters are applied
        reopen()
        showResults()
        assertEquals(enteredFilters.copy(starRating = 5.0), filters)
    }

    @Test
    fun resetFiltersStayResetAfterReopening() {
        enterFilters()
        showResults()
        reopen()
        for (tag in listOf("dateResetButton", "locationResetButton", "guestResetButton", "ratingResetButton")) {
            composeTestRule.onNodeWithTag(tag).performScrollTo().performClick()
        }
        showResults()
        assertEquals(CampsiteFilters(), filters)
        reopen()

        composeTestRule.onNodeWithText("10-9999").assertExists()
        composeTestRule.onNodeWithTag("ratingStar1").assertIsNotSelected()
    }

    @Test
    fun closingWithoutApplyingKeepsTheOldFilters() {
        enterFilters()
        showResults()
        reopen()
        composeTestRule.onNodeWithTag("ratingStar5").performScrollTo().performClick()
        reopen()

        assertEquals(enteredFilters, filters)
        composeTestRule.onNodeWithTag("ratingStar4").assertIsNotSelected()
    }

    @Test
    fun dateRangeInputWorks() {
        composeTestRule
            .onNodeWithTag("fromDateInput")
            .performTextInput("2026-12-10")

        composeTestRule
            .onNodeWithTag("toDateInput")
            .performTextInput("2026-12-15")

        composeTestRule
            .onNodeWithTag("fromDateInput")
            .assertTextEquals("2026-12-10")

        composeTestRule
            .onNodeWithTag("toDateInput")
            .assertTextEquals("2026-12-15")
    }

    @Test
    fun locationInputWorks() {
        composeTestRule
            .onNodeWithTag("locationInput")
            .performTextInput("University")

        composeTestRule
            .onNodeWithTag("locationInput")
            .assertTextEquals("University")
    }

    /** Drags across the slider, as fractions of its width. The track is in its lower half. */
    private fun dragSlider(from: Float, to: Float) {
        composeTestRule
            .onNodeWithTag("guestRangeInput")
            .performScrollTo()
            .performTouchInput {
                swipe(
                    start = percentOffset(from, 0.7f),
                    end = percentOffset(to, 0.7f),
                    durationMillis = 600
                )
            }
        composeTestRule.waitForIdle()
    }

    private fun appliedGuests(): IntRange? {
        composeTestRule.onNodeWithTag("showResultsButton").performScrollTo().performClick()

        val guests = mutableListOf<IntRange?>()
        verify { campsiteController.applyFilters(any(), any(), any(), any(), captureNullable(guests)) }
        return guests.last()
    }

    @Test
    fun guestSliderCoversTheCampsites() {
        composeTestRule.onNodeWithText("10-9999").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun fullGuestRangeDoesNotFilter() {
        assertNull(appliedGuests())
    }

    @Test
    fun stopsAreEvenlySpacedCampsiteSizes() {
        dragSlider(from = 0.98f, to = 0.5f) // halfway is the middle size, not the middle number

        assertEquals(10..2000, appliedGuests())
        composeTestRule.onNodeWithText("10-2000").assertExists()
    }

    @Test
    fun draggingBetweenStopsSnapsToTheClosest() {
        dragSlider(from = 0.98f, to = 0.62f)

        assertEquals(10..2000, appliedGuests())
    }

    @Test
    fun bothEndsCanBeMoved() {
        dragSlider(from = 0.98f, to = 0.5f)
        dragSlider(from = 0.02f, to = 0.45f)

        assertEquals(2000..2000, appliedGuests())
    }

    @Test
    fun slowDragStillMovesTheSlider() {
        composeTestRule.onNodeWithTag("guestRangeInput").performScrollTo().performTouchInput {
            swipe(percentOffset(0.98f, 0.7f), percentOffset(0.5f, 0.7f), durationMillis = 4_000)
        }

        assertEquals(10..2000, appliedGuests())
    }

    @Test
    fun draggingBackToTheEndsDoesNotFilter() {
        dragSlider(from = 0.98f, to = 0.5f)
        dragSlider(from = 0.5f, to = 1f)
        assertNull(appliedGuests())

        guestOptions = listOf(10, 2000, 9999, 20_000)
        assertNull(appliedGuests())
    }

    @Test
    fun resetRestoresTheFullGuestRange() {
        dragSlider(from = 0.98f, to = 0.5f)
        composeTestRule.onNodeWithTag("guestResetButton").performScrollTo().performClick()

        composeTestRule.onNodeWithText("10-9999").assertExists()
        assertNull(appliedGuests())
    }

    @Test
    fun sliderFollowsCampsitesThatLoadLater() {
        guestOptions = listOf(1, 6, 300)

        composeTestRule.onNodeWithText("1-300").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun chosenRangeIsKeptWhenCampsitesAreAdded() {
        dragSlider(from = 0.98f, to = 0.5f)
        guestOptions = listOf(4, 10, 50, 2000, 9999)

        composeTestRule.onNodeWithText("10-2000").assertExists()
        assertEquals(10..2000, appliedGuests())
    }

    @Test
    fun twoSizesCanBeChosenBetween() {
        guestOptions = listOf(4, 60)
        dragSlider(from = 0.98f, to = 0.1f)

        assertEquals(4..4, appliedGuests())
    }

    @Test
    fun sliderIsHiddenWhenThereIsNothingToChoose() {
        guestOptions = listOf(4)
        composeTestRule.onNodeWithTag("guestRangeInput").assertDoesNotExist()
        composeTestRule.onNodeWithText("Every campsite allows up to 4 guests.").assertExists()

        guestOptions = emptyList()
        composeTestRule.onNodeWithText("No campsites to filter.").assertExists()
        assertNull(appliedGuests())
    }

    @Test
    fun ratingInputWorks() {
        composeTestRule
            .onNodeWithTag("ratingStar3")
            .performClick()

        composeTestRule.onNodeWithTag("ratingStar1").assertIsSelected()
        composeTestRule.onNodeWithTag("ratingStar2").assertIsSelected()
        composeTestRule.onNodeWithTag("ratingStar3").assertIsSelected()

        composeTestRule.onNodeWithTag("ratingStar4").assertIsNotSelected()
        composeTestRule.onNodeWithTag("ratingStar5").assertIsNotSelected()
    }

    @Test
    fun showResultsButtonWorks() {
        composeTestRule
            .onNodeWithTag("showResultsButton")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertTrue(showResultsCalled)
        }
    }
}

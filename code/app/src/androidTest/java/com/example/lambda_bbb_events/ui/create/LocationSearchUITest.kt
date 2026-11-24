package com.example.lambda_bbb_events.ui.create

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.services.PlaceDetails
import com.example.lambda_bbb_events.model.services.PlaceSearch
import com.example.lambda_bbb_events.model.services.PlaceSearchLimitException
import com.example.lambda_bbb_events.model.services.PlaceSuggestion
import com.example.lambda_bbb_events.view.ui.user_screens.create.CreateCampsiteFormState
import com.example.lambda_bbb_events.view.ui.user_screens.create.CreateLocationStep
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Tests searching for a campsite's location by name. The search is mocked, so nothing is sent
 * to Google.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class LocationSearchUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val places = mockk<PlaceSearch>()
    private val form = CreateCampsiteFormState()

    @Before
    fun setup() {
        coEvery { places.suggest(any(), any(), any(), any()) } returns listOf(
            PlaceSuggestion("lake", "Lake Louise", "AB, Canada"),
            PlaceSuggestion("jasper", "Jasper", "AB, Canada")
        )
        coEvery { places.getPlace("lake", any()) } returns
                PlaceDetails(51.4, -116.2, "Lake Louise, AB, Canada", "T0L 1E0")
        coEvery { places.getPlace("jasper", any()) } returns
                PlaceDetails(52.9, -118.1, "Jasper, AB, Canada", "")

        composeTestRule.setContent {
            CreateLocationStep(
                form = form,
                showMap = false,
                placeSearch = places,
                onBack = {},
                onNext = {}
            )
        }
    }

    @After
    fun cleanUp() {
        clearAllMocks()
    }

    private fun type(text: String) {
        composeTestRule.onNodeWithTag("locationSearchInput").performTextInput(text)
    }

    private fun waitForSuggestions() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("locationSuggestions"), 5_000)
    }

    @Test
    fun typingRecommendsPlaces() {
        type("Lake")
        waitForSuggestions()

        composeTestRule.onNodeWithText("Lake Louise").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jasper").assertIsDisplayed()
    }

    @Test
    fun choosingAPlaceFillsInTheLocation() {
        form.postalCode = "X0X 0X0"
        type("Jas")
        waitForSuggestions()
        composeTestRule.onNodeWithTag("locationSuggestion_jasper").performClick()
        composeTestRule.waitForIdle()

        assertEquals(52.9, form.latitude, 0.0)
        assertEquals(-118.1, form.longitude, 0.0)
        assertEquals("Jasper, AB, Canada", form.address)
        assertEquals("", form.postalCode) // not left over from the last location
        composeTestRule.onNodeWithTag("locationSearchInput").assertTextContains("Jasper")
        composeTestRule.onNodeWithTag("locationSuggestions").assertDoesNotExist()
    }

    @Test
    fun keyboardSearchChoosesTheFirstPlace() {
        type("Lake")
        waitForSuggestions()
        composeTestRule.onNodeWithTag("locationSearchInput").performImeAction()
        composeTestRule.waitForIdle()

        assertEquals("Lake Louise, AB, Canada", form.address)
        assertEquals("T0L 1E0", form.postalCode)
    }

    @Test
    fun keyboardSearchDoesNotChooseAPlaceForEarlierText() {
        type("Lake")
        waitForSuggestions()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onNodeWithTag("locationSearchInput").performTextReplacement("Jasper")
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.onNodeWithTag("locationSearchInput").performImeAction()
        composeTestRule.mainClock.advanceTimeByFrame()

        coVerify(exactly = 0) { places.getPlace(any(), any()) }
    }

    @Test
    fun onlyTheLastTextTypedIsSearchedFor() {
        composeTestRule.mainClock.autoAdvance = false
        type("Lak")
        composeTestRule.mainClock.advanceTimeBy(100)
        composeTestRule.onNodeWithTag("locationSearchInput").performTextReplacement("Lake Lou")
        composeTestRule.mainClock.autoAdvance = true
        waitForSuggestions()

        coVerify(exactly = 1) { places.suggest(any(), any(), any(), any()) }
        coVerify { places.suggest("Lake Lou", any(), any(), any()) }
    }

    @Test
    fun shortTextIsNotSearchedFor() {
        type("La")
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()

        coVerify(exactly = 0) { places.suggest(any(), any(), any(), any()) }
    }

    @Test
    fun addressSetByTheMapIsNotSearchedFor() {
        type("Lake")
        waitForSuggestions()
        composeTestRule.runOnIdle { form.searchLocation = "1 Tapped Road" }
        composeTestRule.mainClock.advanceTimeBy(2_000)

        composeTestRule.onNodeWithTag("locationSuggestions").assertDoesNotExist()
        coVerify(exactly = 1) { places.suggest(any(), any(), any(), any()) }
    }

    @Test
    fun clearingRemovesTheTextAndPlaces() {
        type("Lake")
        waitForSuggestions()
        composeTestRule.onNodeWithTag("locationSearchClear").performClick()

        assertEquals("", form.searchLocation)
        composeTestRule.onNodeWithTag("locationSuggestions").assertDoesNotExist()
    }

    @Test
    fun noMatchesAreExplained() {
        coEvery { places.suggest(any(), any(), any(), any()) } returns emptyList()
        type("zzzzzz")

        composeTestRule.waitUntilAtLeastOneExists(hasText("No places found."), 5_000)
    }

    @Test
    fun failuresAreExplained() {
        coEvery { places.suggest(any(), any(), any(), any()) } throws IOException("offline")
        type("Lake")

        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Search is unavailable.", substring = true), 5_000
        )
    }

    @Test
    fun reachingTheLimitIsExplained() {
        coEvery { places.suggest(any(), any(), any(), any()) } throws PlaceSearchLimitException()
        type("Lake")

        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Search limit reached.", substring = true), 5_000
        )
    }
}

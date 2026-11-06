package com.example.lambda_bbb_events

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.CampsiteController
import com.example.lambda_bbb_events.view.ui.components.HomeCampsiteFilter
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterPageUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val campsiteController =
        mockk<CampsiteController>(relaxed = true)

    private var showResultsCalled = false

    @Before
    fun setup() {
        showResultsCalled = false

        composeTestRule.setContent {
            HomeCampsiteFilter(
                campsiteController = campsiteController,
                onDismiss = {},
                onShowResults = {
                    showResultsCalled = true
                }
            )
        }
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

    @Test
    fun numberOfGuestsInputWorks() {
        composeTestRule
            .onNodeWithTag("guestRangeInput")
            .assertIsDisplayed()
            .performTouchInput {
                swipe(
                    start = percentOffset(0.75f, 0.5f),
                    end = percentOffset(0.35f, 0.5f)
                )
            }
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

package com.example.lambda_bbb_events

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.HomeScreen
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        val controllers = mockk<AppControllers>(relaxed = true)

        composeTestRule.setContent {
            HomeScreen(
                navController = rememberNavController(),
                controllers = controllers
            )
        }
    }

    @Test
    fun HomeTitleIsDisplayed() {
        composeTestRule.onNodeWithTag("screenTitle")
            .assertIsDisplayed().assertTextEquals("Home")
    }

    @Test
    fun searchInputWorks(){
        composeTestRule.onNodeWithTag("searchInput")
            .assertIsDisplayed()
            .performTextInput("University")

        composeTestRule.onNodeWithTag("searchInput")
            .assertTextContains("University")
    }

    @Test
    fun filterButtonWorks() {
        composeTestRule.onNodeWithTag("filterButton")
            .assertIsDisplayed()
            .assertHasClickAction().performClick()

        composeTestRule.onNodeWithText("Filters").assertExists()
        composeTestRule.onNodeWithText("Date Range").assertExists()
        composeTestRule.onNodeWithText("Location").assertExists()
        composeTestRule.onNodeWithText("Number of Guests").assertExists()
        composeTestRule.onNodeWithText("Rating").assertExists()
    }

    @Test
    fun recentlyViewedIsDisplayed() {
        composeTestRule.onNodeWithTag("recentlyViewedSection")
            .assertIsDisplayed()
    }

    @Test
    fun featuredCampsitesIsDisplayed() {
        composeTestRule.onNodeWithTag("featuredCampsitesSection")
            .assertIsDisplayed()
    }
}
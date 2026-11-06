package com.example.lambda_bbb_events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.view.ui.CampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.ExploreMapScreen
import com.example.lambda_bbb_events.view.ui.ExploreDefaultScreen
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExploreScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        val controllers = mockk<AppControllers>(relaxed = true)
        val campsite = mockk<Campsite>(relaxed = true)
        every { campsite.campsiteId } returns "test-campsite-1"
        every { campsite.campsiteName } returns "Test Campsite"
        every { controllers.campsites.campsites } returns listOf(campsite)
        every {
            controllers.campsites.getCampsite("test-campsite-1")
        } returns campsite

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "explore_map"
            ) {
                composable("explore_map") {
                    Box(
                        Modifier.fillMaxSize().testTag("test_explore_map")
                    ) {
                        ExploreMapScreen(
                            navController = navController,
                            controllers = controllers,
                            showMap = false
                        )
                    }
                }

                composable("explore_default") {
                    Box(
                        Modifier.fillMaxSize().testTag("test_explore_default")
                    ) {
                        ExploreDefaultScreen(navController, controllers)
                    }
                }

                composable("campsite_details/{campsiteId}") { backStackEntry ->
                    val campsiteId = backStackEntry.arguments?.getString("campsiteId")
                        ?: return@composable

                    CampsiteDetailsScreen(
                        navController = navController,
                        controllers = controllers,
                        campsiteId = campsiteId
                    )
                }
            }
        }
    }

    @Test
    fun exploreScreenIsDisplayed() {
        composeTestRule.onNodeWithTag("test_explore_map")
            .assertIsDisplayed()
    }

    @Test
    fun searchCampsiteNavigatesToExploreDefault() {
        composeTestRule.onNodeWithTag("test_explore_map")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("exploreSearchInput")
            .performTextInput("University")

        composeTestRule.onNodeWithTag("exploreSearchInput")
            .performImeAction()

        composeTestRule.onNodeWithTag("test_explore_default")
            .assertIsDisplayed()
    }

    @Test
    fun navigateSeeAll() {
        composeTestRule.onNodeWithTag("test_explore_map")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("seeAllButton")
            .performClick()

        composeTestRule.onNodeWithTag("test_explore_default")
            .assertIsDisplayed()
    }

    @Test
    fun navigateBackToExploreMap() {
        composeTestRule.onNodeWithTag("seeAllButton")
            .performClick()

        composeTestRule.onNodeWithTag("test_explore_default")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_explore")
            .performClick()

        composeTestRule.onNodeWithTag("test_explore_map")
            .assertIsDisplayed()
    }

    @Test
    fun navigateToCampsiteDetails() {
        composeTestRule.onNodeWithTag("test_explore_map")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test Campsite")
            .performClick()

        composeTestRule.onNodeWithTag("screen_campsite_details")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test Campsite")
            .assertIsDisplayed()
    }
}

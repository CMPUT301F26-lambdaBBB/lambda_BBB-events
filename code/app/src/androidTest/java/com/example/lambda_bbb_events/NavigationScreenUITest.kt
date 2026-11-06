package com.example.lambda_bbb_events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.HomeScreen
import com.example.lambda_bbb_events.view.ui.ActiveBookingsScreen
import com.example.lambda_bbb_events.view.ui.ProfileScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.MyCampsitesScreen
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.navigation.NavHostController
import com.example.lambda_bbb_events.view.ui.ExploreMapScreen

@RunWith(AndroidJUnit4::class)
class NavigationScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var navController: NavHostController

    @Before
    fun setup() {
        val controllers = mockk<AppControllers>(relaxed = true)

        composeTestRule.setContent {
            navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(navController, controllers)
                }

                composable("explore_map") {
                    Box(
                        Modifier.fillMaxSize().testTag("test_explore")
                    ) {
                        ExploreMapScreen(navController, controllers)
                    }
                }

                composable("active_bookings") {
                    Box(Modifier.fillMaxSize().testTag("test_bookings")) {
                        ActiveBookingsScreen(navController)
                    }
                }

                composable("my_campsites") {
                    Box(Modifier.fillMaxSize().testTag("test_my_campsites")) {
                        MyCampsitesScreen(navController, controllers)
                    }
                }

                composable("profile") {
                    Box(Modifier.fillMaxSize().testTag("test_profile")) {
                        ProfileScreen(navController, controllers)
                    }
                }
            }
        }
    }

    @Test
    fun navigateHomeToHome() {
        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_home")
            .performClick()

        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()
    }

    @Test
    fun navigateToExplore() {
        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_explore")
            .performClick()

        composeTestRule.onNodeWithTag("test_explore")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_home")
            .performClick()

        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()
    }

    @Test
    fun navigateToBookings() {
        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_bookings")
            .performClick()

        composeTestRule.onNodeWithTag("test_bookings")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_home")
            .performClick()

        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()
    }

    @Test
    fun navigateToCreate() {
        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_create")
            .performClick()

        composeTestRule.onNodeWithTag("test_my_campsites")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_home")
            .performClick()

        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()
    }

    @Test
    fun navigateToProfile() {
        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_profile")
            .performClick()

        composeTestRule.onNodeWithTag("test_profile")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_home")
            .performClick()

        composeTestRule.onNodeWithTag("screen_home")
            .assertIsDisplayed()
    }
}

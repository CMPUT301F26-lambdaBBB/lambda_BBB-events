package com.example.lambda_bbb_events

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.ActiveBookingsScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.PastBookingsScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.LotteryBookingsScreen
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyReservationsScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val controllers = mockk<AppControllers>(relaxed = true)

            NavHost(
                navController = navController,
                startDestination = "active_bookings"
            ) {
                composable("active_bookings") {
                    ActiveBookingsScreen(navController, controllers)
                }

                composable("past_bookings") {
                    PastBookingsScreen(navController)
                }

                composable("lottery_bookings") {
                    LotteryBookingsScreen(navController, controllers)
                }
            }
        }
    }

    @Test
    fun activeBookingsIsDisplayed() {
        composeTestRule.onNodeWithTag("screen_bookings")
            .assertIsDisplayed()
    }

    @Test
    fun navigateActiveToPast() {
        composeTestRule.onNodeWithTag("screen_bookings")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Past")
            .performClick()

        composeTestRule.onNodeWithTag("past_bookings")
            .assertIsDisplayed()
    }

    @Test
    fun navigateActiveToLottery() {
        composeTestRule.onNodeWithTag("screen_bookings")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Lottery")
            .performClick()

        composeTestRule.onNodeWithTag("lottery_bookings")
            .assertIsDisplayed()
    }

    @Test
    fun navigatePastToActive() {
        composeTestRule.onNodeWithText("Past")
            .performClick()

        composeTestRule.onNodeWithTag("past_bookings")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Active")
            .performClick()

        composeTestRule.onNodeWithTag("screen_bookings")
            .assertIsDisplayed()
    }

    @Test
    fun navigateLotteryToActive() {
        composeTestRule.onNodeWithText("Lottery")
            .performClick()

        composeTestRule.onNodeWithTag("lottery_bookings")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Active")
            .performClick()

        composeTestRule.onNodeWithTag("screen_bookings")
            .assertIsDisplayed()
    }
}

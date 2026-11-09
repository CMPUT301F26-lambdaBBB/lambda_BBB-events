package com.example.lambda_bbb_events

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.view.ui.user_screens.explore.CampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.CampsiteCheckAvailabilityScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.lottery.RegistrationStatusScreen
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CampsiteDetailsScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setup(startDestination: String = "campsite_details") {
        val controllers = mockk<AppControllers>(relaxed = true)
        val campsite = mockk<Campsite>(relaxed = true)

        every { campsite.campsiteId } returns "test-campsite-1"
        every { campsite.campsiteName } returns "Test campsite"
        every { campsite.address } returns "123 Test St"
        every { campsite.maxOccupancy } returns 4
        every { campsite.description } returns "Test description"

        every {
            controllers.campsites.getCampsite("test-campsite-1")
        } returns campsite

        every {
            controllers.lotteries.getOpenLotteryForCampsite("test-campsite-1")
        } returns null

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable("campsite_details") {
                    CampsiteDetailsScreen(
                        navController = navController,
                        controllers = controllers,
                        campsiteId = campsite.campsiteId
                    )
                }

                composable("campsite_check_availability/{campsiteId}") { entry ->
                    val campsiteId = entry.arguments
                        ?.getString("campsiteId")
                        ?: return@composable

                    CampsiteCheckAvailabilityScreen(
                        navController = navController,
                        controllers = controllers,
                        campsiteId = campsiteId
                    )
                }

                composable("registration_status_test") {
                    RegistrationStatusScreen(
                        navController = navController,
                        controllers = controllers,
                        campsiteId = campsite.campsiteId,
                        fromDate = LocalDate.of(2026, 11, 6),
                        endDate = LocalDate.of(2026, 11, 8),
                        numPeople = 2
                    )
                }
            }
        }
    }

    @Test
    fun campsiteDetailsScreenIsDisplayed() {
        setup()

        composeTestRule.onNodeWithTag("screen_campsite_details")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test campsite")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("123 Test St")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("4 people")
            .assertIsDisplayed()
    }

    @Test
    fun navigateToCheckAvailability() {
        setup()

        composeTestRule.onNodeWithTag("screen_campsite_details")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Check Availability")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Select Availability")
            .assertIsDisplayed()
    }

    @Test
    fun checkAvailabilityShowsSameCampsite() {
        setup()

        composeTestRule.onNodeWithText("Test campsite")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Check Availability")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Test campsite")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("123 Test St")
            .assertIsDisplayed()
    }

    @Test
    fun registrationStatusScreenIsDisplayed() {
        setup(startDestination = "registration_status_test")

        composeTestRule.onNodeWithText("Lottery Required")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("You Selected: Test campsite")
            .assertIsDisplayed()
    }
}

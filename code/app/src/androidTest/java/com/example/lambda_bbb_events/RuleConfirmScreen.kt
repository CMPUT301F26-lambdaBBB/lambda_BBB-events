package com.example.lambda_bbb_events

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.LotteryStatus
import com.example.lambda_bbb_events.view.ui.RuleConfirmScreen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RuleConfirmScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setupScreen(
        lottery: Lottery? = null,
        onNavigateToLotteryEntered: () -> Unit = {}
    ): AppControllers {
        val controllers = mockk<AppControllers>(relaxed = true)
        val campsite = mockk<Campsite>(relaxed = true)

        every { campsite.campsiteId } returns "campsite-101"
        every { campsite.campsiteName } returns "Pine Lake Campsite"
        every { campsite.rulesPolicies } returns "Custom rules: No loud music after 9 PM."
        every { controllers.campsites.getCampsite("campsite-101") } returns campsite
        every { controllers.lotteries.getOpenLotteryForCampsite("campsite-101") } returns lottery
        every { controllers.users.deviceId } returns "test-device-id"

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "rule_confirm"
            ) {
                composable("rule_confirm") {
                    RuleConfirmScreen(
                        navController = navController,
                        controllers = controllers,
                        campsiteId = "campsite-101",
                        fromDate = LocalDate.of(2026, 7, 10),
                        endDate = LocalDate.of(2026, 7, 15),
                        numPeople = 2
                    )
                }

                composable("lottery_entered/{campsiteId}/{fromDate}/{endDate}/{numPeople}") {
                    onNavigateToLotteryEntered()
                }
            }
        }

        return controllers
    }

    @Test
    fun ruleConfirmScreenDisplaysTitleAndRules() {
        val lottery = Lottery(
            lotteryId = "lottery-1",
            campsiteIds = listOf("campsite-101"),
            status = LotteryStatus.OPEN
        )

        setupScreen(lottery = lottery)

        composeTestRule.onNodeWithText("Rules & Policies").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pine Lake Campsite").assertIsDisplayed()
        composeTestRule.onNodeWithText("Campsite Additional Rules").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lottery Service Policy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom rules: No loud music after 9 PM.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Decline").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
    }

    @Test
    fun confirmButtonClickTriggersJoinLotteryAndNavigates() {
        var navigated = false

        val lottery = Lottery(
            lotteryId = "lottery-1",
            campsiteIds = listOf("campsite-101"),
            status = LotteryStatus.OPEN
        )

        val controllers = setupScreen(
            lottery = lottery,
            onNavigateToLotteryEntered = { navigated = true }
        )

        every {
            controllers.lotteries.joinLotteryEntry(
                lotteryId = "lottery-1",
                entrantId = "test-device-id",
                numberOfGuests = 2,
                onResult = any()
            )
        } answers {
            val callback = arg<(Boolean, String) -> Unit>(3)
            callback(true, "Success")
        }

        composeTestRule.onNodeWithText("Confirm")
            .assertIsDisplayed()
            .performClick()

        verify {
            controllers.lotteries.joinLotteryEntry(
                lotteryId = "lottery-1",
                entrantId = "test-device-id",
                numberOfGuests = 2,
                onResult = any()
            )
        }

        composeTestRule.runOnIdle {
            assertEquals(true, navigated)
        }
    }

    @Test
    fun confirmButtonClickShowsErrorMessageOnFailure() {
        val lottery = Lottery(
            lotteryId = "lottery-1",
            campsiteIds = listOf("campsite-101"),
            status = LotteryStatus.OPEN
        )

        val controllers = setupScreen(lottery = lottery)

        every {
            controllers.lotteries.joinLotteryEntry(
                lotteryId = "lottery-1",
                entrantId = "test-device-id",
                numberOfGuests = 2,
                onResult = any()
            )
        } answers {
            val callback = arg<(Boolean, String) -> Unit>(3)
            callback(false, "Failed to join lottery")
        }

        composeTestRule.onNodeWithText("Confirm")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("Failed to join lottery").assertIsDisplayed()
    }
}

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
import com.example.lambda_bbb_events.view.ui.RegistrationStatusScreen
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.Date

@RunWith(AndroidJUnit4::class)
class RegistrationStatusScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setupScreen(
        openLottery: Lottery? = null,
        onNavigateToRuleConfirm: () -> Unit = {}
    ): AppControllers {
        val controllers = mockk<AppControllers>(relaxed = true)
        val campsite = mockk<Campsite>(relaxed = true)

        every { campsite.campsiteId } returns "campsite-101"
        every { campsite.campsiteName } returns "Pine Lake Campsite"
        every { controllers.campsites.getCampsite("campsite-101") } returns campsite
        every { controllers.lotteries.getOpenLotteryForCampsite("campsite-101") } returns openLottery
        every { controllers.users.deviceId } returns "test-device-id"

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "registration_status"
            ) {
                composable("registration_status") {
                    RegistrationStatusScreen(
                        navController = navController,
                        controllers = controllers,
                        campsiteId = "campsite-101",
                        fromDate = LocalDate.of(2026, 7, 10),
                        endDate = LocalDate.of(2026, 7, 15),
                        numPeople = 2
                    )
                }

                composable("rule_confirm/{campsiteId}/{fromDate}/{endDate}/{numPeople}") {
                    onNavigateToRuleConfirm()
                }
            }
        }

        return controllers
    }

    @Test
    fun registrationStatusScreenDisplaysDetails() {
        val lottery = Lottery(
            lotteryId = "lottery-1",
            campsiteIds = listOf("campsite-101"),
            status = LotteryStatus.OPEN,
            registrationStart = Date(System.currentTimeMillis() - 3600000L),
            registrationEnd = Date(System.currentTimeMillis() + 3600000L),
            waitlistCapacity = 10,
            entryIds = emptyList()
        )

        setupScreen(openLottery = lottery)

        composeTestRule.onNodeWithTag("screenTitle").assertIsDisplayed().assertTextEquals("Registration Status")
        composeTestRule.onNodeWithText("Lottery Required").assertIsDisplayed()
        composeTestRule.onNodeWithText("You Selected: Pine Lake Campsite").assertIsDisplayed()
        composeTestRule.onNodeWithText("Join Lottery").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun joinLotteryButtonNavigatesToRuleConfirm() {
        var navigated = false

        val lottery = Lottery(
            lotteryId = "lottery-1",
            campsiteIds = listOf("campsite-101"),
            status = LotteryStatus.OPEN,
            registrationStart = Date(System.currentTimeMillis() - 3600000L),
            registrationEnd = Date(System.currentTimeMillis() + 3600000L),
            waitlistCapacity = 10,
            entryIds = emptyList()
        )

        setupScreen(openLottery = lottery, onNavigateToRuleConfirm = { navigated = true })

        composeTestRule.onNodeWithText("Join Lottery")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(true, navigated)
        }
    }

    @Test
    fun displaysNoOpenLotteryMessageWhenLotteryIsNull() {
        setupScreen(openLottery = null)

        composeTestRule.onNodeWithText("No open lottery available").assertIsDisplayed()
        composeTestRule.onNodeWithText("Join Lottery").assertIsNotEnabled()
    }

    @Test
    fun displaysLotteryWaitlistFullMessageWhenFull() {
        val fullLottery = Lottery(
            lotteryId = "lottery-full",
            campsiteIds = listOf("campsite-101"),
            status = LotteryStatus.OPEN,
            registrationStart = Date(System.currentTimeMillis() - 3600000L),
            registrationEnd = Date(System.currentTimeMillis() + 3600000L),
            waitlistCapacity = 1,
            entryIds = listOf("entry-1")
        )

        setupScreen(openLottery = fullLottery)

        composeTestRule.onNodeWithText("Lottery waitlist is full").assertIsDisplayed()
        composeTestRule.onNodeWithText("Join Lottery").assertIsNotEnabled()
    }
}

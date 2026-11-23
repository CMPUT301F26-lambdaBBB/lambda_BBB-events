package com.example.lambda_bbb_events

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.model.WaitlistStatus
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.lottery.RegistrationStatusScreen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the screen showing the status of an entrant's lottery registration.
 */
@RunWith(AndroidJUnit4::class)
class RegistrationStatusScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val controllers = mockk<AppControllers>(relaxed = true)
    private val waitlistEntries = controllers.waitlistEntries
    private var openedStatusScreen = false

    private fun entry(status: WaitlistStatus, guests: Int = 2) = WaitlistEntry(
        entryId = "entry-1",
        entrantId = "test-device-id",
        lotteryId = "lottery-1",
        numberOfGuests = guests,
        status = status
    )

    private fun setupScreen(entry: WaitlistEntry?) = setupScreen { entry }

    private fun setupScreen(entryProvider: () -> WaitlistEntry?) {
        every { waitlistEntries.getEntryById("entry-1") } answers { entryProvider() }

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "previous") {
                composable("previous") {
                    Text("Previous screen")
                    // Open the status screen once, so going back stays on this screen
                    LaunchedEffect(Unit) {
                        if (!openedStatusScreen) {
                            openedStatusScreen = true
                            navController.navigate("registration_status")
                        }
                    }
                }
                composable("registration_status") {
                    RegistrationStatusScreen(
                        navController = navController,
                        controllers = controllers,
                        entryId = "entry-1"
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun showsTitleAndGuestCount() {
        setupScreen(entry(WaitlistStatus.IN_WAITLIST, guests = 3))

        composeTestRule.onNodeWithTag("screenTitle").assertTextEquals("Registration Status")
        composeTestRule.onNodeWithText("Number of Guests: 3").assertIsDisplayed()
    }

    @Test
    fun showsLoadingWhenEntryIsMissing() {
        setupScreen(null)

        composeTestRule.onNodeWithText("Loading registration information...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leave Waitlist").assertDoesNotExist()
    }

    @Test
    fun showsMessageForEachStatus() {
        val expected = mapOf(
            WaitlistStatus.WON to "Lottery Won",
            WaitlistStatus.LOST to "Lottery Lost",
            WaitlistStatus.ACCEPTED to "Reservation Accepted",
            WaitlistStatus.DECLINED to "Reservation Declined"
        )
        // Reading Compose state inside the mock makes the screen recompose when it changes
        val current = mutableStateOf(entry(WaitlistStatus.WON))
        setupScreen { current.value }

        for ((status, text) in expected) {
            composeTestRule.runOnIdle { current.value = entry(status) }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(text).assertExists()
            composeTestRule.onNodeWithText("Leave Waitlist").assertDoesNotExist()
        }
    }

    @Test
    fun inWaitlistShowsStatusAndLeaveButton() {
        setupScreen(entry(WaitlistStatus.IN_WAITLIST))

        composeTestRule.onNodeWithText("In Waitlist").assertIsDisplayed()
        composeTestRule.onNodeWithText("You have successfully joined the lottery. Waiting for results.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Leave Waitlist").assertIsDisplayed()
    }

    @Test
    fun cancellingLeaveDialogKeepsEntry() {
        setupScreen(entry(WaitlistStatus.IN_WAITLIST))

        composeTestRule.onNodeWithText("Leave Waitlist").performClick()
        composeTestRule.onNodeWithText("Leave Waitlist?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("Leave Waitlist?").assertDoesNotExist()
        coVerify(exactly = 0) { waitlistEntries.leaveWaitlist(any()) }
    }

    @Test
    fun confirmingLeaveRemovesEntryAndGoesBack() {
        val inWaitlist = entry(WaitlistStatus.IN_WAITLIST)
        coEvery { waitlistEntries.leaveWaitlist(inWaitlist) } just runs
        setupScreen(inWaitlist)

        composeTestRule.onNodeWithText("Leave Waitlist").performClick()
        composeTestRule.onNodeWithText("Leave").performClick()
        composeTestRule.waitForIdle()

        coVerify(exactly = 1) { waitlistEntries.leaveWaitlist(inWaitlist) }
        composeTestRule.onNodeWithText("Previous screen").assertIsDisplayed()
    }

    @Test
    fun failedLeaveShowsError() {
        val inWaitlist = entry(WaitlistStatus.IN_WAITLIST)
        coEvery { waitlistEntries.leaveWaitlist(inWaitlist) } throws IllegalStateException("Network down")
        setupScreen(inWaitlist)

        composeTestRule.onNodeWithText("Leave Waitlist").performClick()
        composeTestRule.onNodeWithText("Leave").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Network down").assertIsDisplayed()
    }
}

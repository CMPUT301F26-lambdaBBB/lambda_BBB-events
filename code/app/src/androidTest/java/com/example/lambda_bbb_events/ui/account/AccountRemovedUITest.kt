package com.example.lambda_bbb_events.ui.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.LambdaEventsApp
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.user.Entrant
import com.example.lambda_bbb_events.model.user.User
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests that the app leaves whatever screen is open and goes back to the welcome screen, without
 * crashing, when the signed-in user's account is removed by someone else, such as an admin.
 */
@RunWith(AndroidJUnit4::class)
class AccountRemovedUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val controllers = mockk<AppControllers>(relaxed = true)
    private val repository = mockk<UserRepository>(relaxed = true)

    /** This device's account. Compose state, like the repository's cache. */
    private var account by mutableStateOf<User?>(
        Entrant(
            deviceId = "device-1",
            name = "Ed",
            email = "ed@example.com",
            phone = "+1 7805551234"
        )
    )

    @Before
    fun startApp() {
        every { repository.usersFullyLoaded } returns true
        every { repository.getCachedUser("device-1") } answers { account }
        every { controllers.users } returns UserController(repository, "device-1")
        every { controllers.reservations.getReservationsForEntrant(any()) } returns emptyList()
        every { controllers.waitlistEntries.getEntriesForEntrant(any()) } returns emptyList()
        every { controllers.campsites.getCampsitesByOwner(any()) } returns emptyList()

        composeTestRule.setContent {
            LambdaEventsApp(controllers = controllers, darkTheme = false, onDarkThemeChange = {})
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    private fun removeAccountAndCheckWelcomeIsShown() {
        composeTestRule.runOnIdle { account = null }

        composeTestRule.onNodeWithTag("welcomeScreen").assertIsDisplayed()
    }

    private fun openProfile() {
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithTag("screen_profile").assertIsDisplayed()
    }

    private fun openFromProfile(title: String) {
        openProfile()
        composeTestRule.onNodeWithText(title).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun onHome() {
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()
        removeAccountAndCheckWelcomeIsShown()
    }

    @Test
    fun onBookings() {
        composeTestRule.onNodeWithText("Bookings").performClick()
        composeTestRule.onNodeWithTag("screen_bookings").assertIsDisplayed()
        removeAccountAndCheckWelcomeIsShown()
    }

    @Test
    fun onMyCampsites() {
        composeTestRule.onNodeWithText("Create").performClick()
        composeTestRule.onNodeWithText("My Campsites").assertIsDisplayed()
        removeAccountAndCheckWelcomeIsShown()
    }

    @Test
    fun onProfile() {
        openProfile()
        removeAccountAndCheckWelcomeIsShown()
    }

    @Test
    fun onEditProfile() {
        openFromProfile("Edit Profile")
        composeTestRule.onNodeWithTag("edit_profile_name").assertExists()
        removeAccountAndCheckWelcomeIsShown()
    }

    @Test
    fun onSettings() {
        openFromProfile("Settings")
        composeTestRule.onNodeWithTag("screen_settings").assertIsDisplayed()
        removeAccountAndCheckWelcomeIsShown()
    }

    @Test
    fun onNotificationSettings() {
        openFromProfile("Settings")
        composeTestRule.onNodeWithText("Notifications").performClick()
        composeTestRule.onNodeWithTag("screen_notification_settings").assertIsDisplayed()
        removeAccountAndCheckWelcomeIsShown()
    }
}

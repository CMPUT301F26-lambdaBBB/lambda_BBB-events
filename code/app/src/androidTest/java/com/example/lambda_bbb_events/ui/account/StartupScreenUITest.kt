package com.example.lambda_bbb_events.ui.account

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.LambdaEventsApp
import com.example.lambda_bbb_events.controller.AppControllers
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the screens shown when the app starts, before the main app: the loading screen
 * while the account is checked, and sign up for devices without an account.
 */
@RunWith(AndroidJUnit4::class)
class StartupScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val controllers = mockk<AppControllers>(relaxed = true)
    private val users = controllers.users

    private fun startApp() {
        composeTestRule.setContent {
            LambdaEventsApp(controllers = controllers, darkTheme = false, onDarkThemeChange = {})
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun loadingScreenShowsLogoAndName() {
        every { users.usersFullyLoaded } returns false
        startApp()

        composeTestRule.onNodeWithTag("loadingScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lambda Camping").assertIsDisplayed()
    }

    @Test
    fun deviceWithoutAccountSeesSignUpWithLogo() {
        every { users.usersFullyLoaded } returns true
        every { users.currentUser } returns null
        startApp()

        composeTestRule.onNodeWithTag("signupScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loadingScreen").assertDoesNotExist()
    }
}

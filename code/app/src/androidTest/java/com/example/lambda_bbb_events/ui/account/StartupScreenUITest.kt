package com.example.lambda_bbb_events.ui.account

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.LambdaEventsApp
import com.example.lambda_bbb_events.controller.AppControllers
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
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
    fun deviceWithoutAccountSeesWelcomeWithLoginAndSignUp() {
        every { users.usersFullyLoaded } returns true
        every { users.currentUser } returns null
        startApp()

        composeTestRule.onNodeWithTag("welcomeScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signupButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loadingScreen").assertDoesNotExist()
        composeTestRule.onNodeWithTag("signupScreen").assertDoesNotExist()
    }

    @Test
    fun signUpButtonOpensSignUpAndBackReturnsToWelcome() {
        every { users.usersFullyLoaded } returns true
        every { users.currentUser } returns null
        startApp()

        composeTestRule.onNodeWithTag("signupButton").performClick()
        composeTestRule.onNodeWithTag("signupScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithTag("welcomeScreen").assertIsDisplayed()
    }

    @Test
    fun loginButtonOpensLoginScreen() {
        every { users.usersFullyLoaded } returns true
        every { users.currentUser } returns null
        startApp()

        composeTestRule.onNodeWithTag("loginButton").performClick()
        composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcomeScreen").assertDoesNotExist()
    }

    @Test
    fun loginSubmitsEmailAndPhoneAndShowsErrorWhenNoAccountMatches() {
        every { users.usersFullyLoaded } returns true
        every { users.currentUser } returns null
        every { users.login(any(), any()) } returns false
        startApp()

        composeTestRule.onNodeWithTag("loginButton").performClick()
        composeTestRule.onNodeWithTag("loginEmailInput").performTextInput("ed@example.com")
        composeTestRule.onNodeWithTag("loginPhoneInput").performTextInput("7805551234")
        composeTestRule.onNodeWithTag("loginSubmitButton").performClick()

        composeTestRule.onNodeWithTag("loginError").assertIsDisplayed()
        verify { users.login("ed@example.com", "+1 7805551234") }
    }
}

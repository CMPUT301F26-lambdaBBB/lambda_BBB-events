package com.example.lambda_bbb_events

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.view.ui.EditProfileScreen
import com.example.lambda_bbb_events.view.ui.ProfileScreen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val controllers = mockk<AppControllers>(relaxed = true)
    private val userController = controllers.users

    @Before
    fun setup() {
        val user = mockk<User>(relaxed = true)

        every { user.name } returns "Test User"
        every { user.email } returns "test@example.com"
        every { user.phone } returns "1234567890"

        every { userController.user } returns user

        coEvery {
            userController.updateEntrant(any(), any(), any())
        } returns true

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "profile"
            ) {
                composable("profile") {
                    ProfileScreen(navController, controllers)
                }

                composable("edit_profile") {
                    EditProfileScreen(navController, controllers)
                }
            }
        }
    }

    @Test
    fun profileScreenIsDisplayed() {
        composeTestRule.onNodeWithTag("screen_profile")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Test User")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("test@example.com")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Settings")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Edit Profile")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Logout")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun navigateToEditProfile() {
        composeTestRule.onNodeWithTag("screen_profile")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Edit Profile")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Name")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Email")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Phone Number")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Save Changes")
            .assertExists()
    }

    @Test
    fun editProfileDisplaysExistingInformation() {
        composeTestRule.onNodeWithText("Edit Profile")
            .performClick()

        composeTestRule.onNodeWithTag("edit_profile_name")
            .assertTextContains("Test User")

        composeTestRule.onNodeWithTag("edit_profile_email")
            .assertTextContains("test@example.com")

        composeTestRule.onNodeWithTag("edit_profile_phone")
            .assertTextContains("1234567890")
    }

    @Test
    fun saveProfileChanges() {
        composeTestRule.onNodeWithText("Edit Profile")
            .performClick()

        composeTestRule.onNodeWithTag("edit_profile_name")
            .performTextReplacement("Updated User")

        composeTestRule.onNodeWithTag("edit_profile_email")
            .performTextReplacement("updated@example.com")

        composeTestRule.onNodeWithTag("edit_profile_phone")
            .performTextReplacement("7809876543")

        composeTestRule.onNodeWithText("Save Changes")
            .performClick()

        coVerify(exactly = 1) {
            userController.updateEntrant(
                "Updated User",
                "updated@example.com",
                "7809876543"
            )
        }

        composeTestRule.onNodeWithTag("screen_profile")
            .assertIsDisplayed()
    }

    @Test
    fun failedProfileUpdateDisplaysError() {
        coEvery {
            userController.updateEntrant(any(), any(), any())
        } returns false

        composeTestRule.onNodeWithText("Edit Profile")
            .performClick()

        composeTestRule.onNodeWithText("Save Changes")
            .performClick()

        composeTestRule.onNodeWithText(
            "Unable to create account. Please review your information."
        ).assertIsDisplayed()
    }
}

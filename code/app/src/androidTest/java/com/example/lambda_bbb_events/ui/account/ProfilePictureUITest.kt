package com.example.lambda_bbb_events.ui.account

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.user.Entrant
import com.example.lambda_bbb_events.view.ui.user_screens.profile.EditProfileScreen
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests adding and removing a profile picture on the edit profile screen.
 *
 * Picking a photo opens Android's photo picker, which is a separate app that UI tests
 * cannot control, so only the buttons around it are tested here.
 */
@RunWith(AndroidJUnit4::class)
class ProfilePictureUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val controllers = mockk<AppControllers>(relaxed = true)
    private val users = controllers.users
    private val images = controllers.images

    private fun showEditProfile(pictureId: String) {
        val user = Entrant(
            deviceId = "device-1",
            name = "Ed",
            email = "ed@example.com",
            phone = "+1 7805551234",
            profilePictureImageId = pictureId
        )
        every { users.user } returns user
        coEvery { users.updateProfilePicture(any()) } returns pictureId
        coEvery { images.deleteImage(any()) } returns true

        composeTestRule.setContent {
            EditProfileScreen(rememberNavController(), controllers)
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun userWithoutPictureCanAddOne() {
        showEditProfile(pictureId = "")

        composeTestRule.onNodeWithTag("edit_profile_picture").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add photo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("remove_profile_picture").assertDoesNotExist()
    }

    @Test
    fun userWithPictureCanChangeOrRemoveIt() {
        showEditProfile(pictureId = "img-old")

        composeTestRule.onNodeWithText("Change photo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("remove_profile_picture").assertIsDisplayed()
    }

    @Test
    fun removingPictureSavesAndDeletesTheOldOne() {
        showEditProfile(pictureId = "img-old")

        composeTestRule.onNodeWithTag("remove_profile_picture").performClick()
        composeTestRule.waitForIdle()

        coVerify(exactly = 1) { users.updateProfilePicture("") }
        coVerify(exactly = 1) { images.deleteImage("img-old") }
        composeTestRule.onNodeWithText("Add photo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("remove_profile_picture").assertDoesNotExist()
    }
}

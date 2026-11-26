package com.example.lambda_bbb_events.ui.admin

import androidx.compose.material3.Text
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.repository.WrongAdminPasswordException
import com.example.lambda_bbb_events.model.user.Admin
import com.example.lambda_bbb_events.model.user.CampsiteOwner
import com.example.lambda_bbb_events.model.user.Entrant
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.view.ui.admin_screens.AdminProfileDetailsScreen
import com.example.lambda_bbb_events.view.ui.admin_screens.AdminProfilesScreen
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests messaging, promoting users to admin, and removing users from [AdminProfileDetailsScreen], opened
 * by clicking the user in [AdminProfilesScreen]. Controllers are mocked, so nothing is written
 * to the database.
 */
@RunWith(AndroidJUnit4::class)
class AdminProfileDetailsScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val controllers = mockk<AppControllers>(relaxed = true)
    private val users = controllers.users

    private val admin = Admin(deviceId = "admin-1", name = "Ada", email = "ada@example.com")
    private val entrant = Entrant(
        deviceId = "user-1",
        name = "Ed",
        email = "ed@example.com",
        phone = "+1 7805551234"
    )
    private val promoted = Admin(
        deviceId = "user-1",
        name = "Ed",
        email = "ed@example.com",
        phone = "+1 7805551234"
    )

    /** Shows the profile list to [signedIn], and opens the profile of [shown] if given. */
    private fun openProfile(shown: User?, signedIn: User = admin) {
        every { users.currentUser } returns signedIn
        coEvery { users.getAllUsers() } returns listOfNotNull(shown)
        coEvery { users.getUser(any()) } returns shown

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "admin_profiles") {
                composable("admin_profiles") {
                    AdminProfilesScreen(navController, controllers)
                }
                composable("admin_profile_details/{userId}") { entry ->
                    val userId = entry.arguments?.getString("userId") ?: ""
                    AdminProfileDetailsScreen(navController, controllers, userId)
                }
                composable("chat/{conversationId}") { entry ->
                    Text("Chat ${entry.arguments?.getString("conversationId")}")
                }
            }
        }

        composeTestRule.onNodeWithTag("adminProfileItem").performClick()
        composeTestRule.onNodeWithTag("screen_admin_profile_details").assertIsDisplayed()
    }

    /** Shows the profile details screen by itself, for cases the list cannot lead to. */
    private fun showDetails(shown: User?, signedIn: User) {
        every { users.currentUser } returns signedIn
        coEvery { users.getUser(any()) } returns shown

        composeTestRule.setContent {
            AdminProfileDetailsScreen(rememberNavController(), controllers, "user-1")
        }
    }

    private fun clickMakeAdmin() {
        composeTestRule.onNodeWithTag("adminMakeAdminButton").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Make Admin?").assertIsDisplayed()
    }

    private fun confirmWithPassword(password: String) {
        composeTestRule.onNodeWithTag("adminPasswordInput").performTextReplacement(password)
        composeTestRule.onNodeWithTag("adminPasswordConfirm").performClick()
        composeTestRule.waitForIdle()
    }

    private fun clickRemove() {
        composeTestRule.onNodeWithTag("adminRemoveProfileButton").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Remove Profile?").assertIsDisplayed()
    }

    private fun click(text: String) {
        composeTestRule.onNodeWithText(text).performClick()
        composeTestRule.waitForIdle()
    }

    private fun assertRoleShown(role: String) {
        composeTestRule
            .onNode(
                hasTestTag("adminProfileRole") and hasAnyDescendant(hasText(role)),
                useUnmergedTree = true
            )
            .performScrollTo()
            .assertIsDisplayed()
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun makeAdminButtonIsShownForEntrants() {
        openProfile(entrant)

        assertRoleShown("Entrant")
        composeTestRule.onNodeWithTag("adminMakeAdminButton")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("Make Admin")
    }

    @Test
    fun makeAdminButtonIsShownForOwners() {
        openProfile(CampsiteOwner(deviceId = "user-1", name = "Olive"))

        assertRoleShown("Owner")
        composeTestRule.onNodeWithTag("adminMakeAdminButton").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun makeAdminButtonIsHiddenForAdmins() {
        openProfile(promoted)

        assertRoleShown("Admin")
        composeTestRule.onNodeWithTag("adminMakeAdminButton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("adminRemoveProfileButton").assertExists()
    }

    @Test
    fun confirmingMakeAdminPromotesTheUser() {
        coEvery { users.makeAdmin("user-1", "password") } returns promoted
        openProfile(entrant)

        clickMakeAdmin()
        coVerify(exactly = 0) { users.makeAdmin(any(), any()) }
        confirmWithPassword("password")

        coVerify(exactly = 1) { users.makeAdmin("user-1", "password") }
        composeTestRule.onNodeWithText("Make Admin?").assertDoesNotExist()
        composeTestRule.onNodeWithTag("adminMakeAdminButton").assertDoesNotExist()
        assertRoleShown("Admin")
    }

    @Test
    fun cancellingMakeAdminChangesNothing() {
        openProfile(entrant)

        clickMakeAdmin()
        click("Cancel")

        coVerify(exactly = 0) { users.makeAdmin(any(), any()) }
        composeTestRule.onNodeWithText("Make Admin?").assertDoesNotExist()
        assertRoleShown("Entrant")
    }

    @Test
    fun rejectedMakeAdminShowsAnError() {
        coEvery { users.makeAdmin("user-1", "password") } returns null
        openProfile(entrant)

        clickMakeAdmin()
        confirmWithPassword("password")

        composeTestRule.onNodeWithText("Could not make this user an admin. Please try again.")
            .performScrollTo()
            .assertIsDisplayed()
        assertRoleShown("Entrant")
    }

    @Test
    fun failedMakeAdminShowsAnErrorAndCanBeRetried() {
        coEvery {
            users.makeAdmin("user-1", "password")
        } throws RuntimeException("offline") andThen promoted
        openProfile(entrant)
        val error = "Could not make this user an admin. Please try again."

        clickMakeAdmin()
        confirmWithPassword("password")
        composeTestRule.onNodeWithText(error).performScrollTo().assertIsDisplayed()

        clickMakeAdmin()
        composeTestRule.onNodeWithText(error).assertDoesNotExist()
        confirmWithPassword("password")
        assertRoleShown("Admin")
    }

    @Test
    fun makeAdminNeedsAPassword() {
        openProfile(entrant)

        clickMakeAdmin()

        composeTestRule.onNodeWithTag("adminPasswordConfirm").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("adminPasswordInput").performTextInput("password")
        composeTestRule.onNodeWithTag("adminPasswordConfirm").assertIsEnabled()
    }

    @Test
    fun adminPasswordIsHiddenWhileTyping() {
        openProfile(entrant)

        clickMakeAdmin()
        composeTestRule.onNodeWithTag("adminPasswordInput").performTextInput("password")

        composeTestRule.onNodeWithTag("adminPasswordInput")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
            .assertTextContains("••••••••")
    }

    @Test
    fun wrongPasswordStaysOpenAndCanBeRetried() {
        coEvery { users.makeAdmin("user-1", "wrong") } throws WrongAdminPasswordException()
        coEvery { users.makeAdmin("user-1", "password") } returns promoted
        openProfile(entrant)

        clickMakeAdmin()
        confirmWithPassword("wrong")
        composeTestRule.onNodeWithTag("adminPasswordError").assertIsDisplayed()

        confirmWithPassword("password")
        composeTestRule.onNodeWithText("Make Admin?").assertDoesNotExist()
        assertRoleShown("Admin")
    }

    @Test
    fun reopeningMakeAdminForgetsThePasswordAndError() {
        coEvery { users.makeAdmin("user-1", "wrong") } throws WrongAdminPasswordException()
        openProfile(entrant)

        clickMakeAdmin()
        confirmWithPassword("wrong")
        click("Cancel")
        clickMakeAdmin()

        composeTestRule.onNodeWithTag("adminPasswordError").assertDoesNotExist()
        composeTestRule.onNodeWithTag("adminPasswordConfirm").assertIsNotEnabled()
        assertRoleShown("Entrant")
    }

    @Test
    fun confirmingRemoveDeletesTheUserAfterTheirLotteriesAndCampsites() {
        coEvery { users.removeUser("user-1") } returns true
        openProfile(entrant)

        clickRemove()
        coVerify(exactly = 0) { users.removeUser(any()) }
        click("Remove")

        coVerifyOrder {
            controllers.lotteries.cancelLotteriesOfDeletedOwner("user-1", any())
            controllers.campsites.archiveCampsitesByOwner("user-1")
            users.removeUser("user-1")
        }
        coVerify(exactly = 1) { users.removeUser("user-1") }
        composeTestRule.onNodeWithTag("screen_admin_profiles").assertIsDisplayed()
    }

    @Test
    fun cancellingRemoveChangesNothing() {
        openProfile(entrant)

        clickRemove()
        click("Cancel")

        composeTestRule.onNodeWithText("Remove Profile?").assertDoesNotExist()
        composeTestRule.onNodeWithTag("screen_admin_profile_details").assertIsDisplayed()
        coVerify(exactly = 0) {
            controllers.lotteries.cancelLotteriesOfDeletedOwner(any(), any())
        }
        coVerify(exactly = 0) { controllers.campsites.archiveCampsitesByOwner(any()) }
        coVerify(exactly = 0) { users.removeUser(any()) }
    }

    @Test
    fun rejectedRemoveShowsAnErrorAndStaysOpen() {
        coEvery { users.removeUser("user-1") } returns false
        openProfile(entrant)

        clickRemove()
        click("Remove")

        composeTestRule.onNodeWithText("Could not remove this profile. Please try again.")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("screen_admin_profile_details").assertIsDisplayed()
    }

    @Test
    fun userIsKeptWhenTheirCampsitesCannotBeArchived() {
        coEvery {
            controllers.campsites.archiveCampsitesByOwner("user-1")
        } throws RuntimeException("offline")
        openProfile(entrant)

        clickRemove()
        click("Remove")

        coVerify(exactly = 0) { users.removeUser(any()) }
        composeTestRule.onNodeWithText("Could not remove this profile. Please try again.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Okay so there are no conversations yet so can I have admins be able to message users
     *   from the profile admin screen"
     */
    @Test
    fun messageButtonOpensTheAdminsConversationWithTheUser() {
        coEvery { controllers.messages.startConversationWithUser("user-1") } returns "admin_user-1_admin-1"
        openProfile(entrant)

        composeTestRule.onNodeWithTag("adminMessageUserButton").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        coVerify { controllers.messages.startConversationWithUser("user-1") }
        composeTestRule.onNodeWithText("Chat admin_user-1_admin-1").assertIsDisplayed()
    }

    /**
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Okay so there are no conversations yet so can I have admins be able to message users
     *   from the profile admin screen"
     */
    @Test
    fun failedMessageShowsAnErrorAndStaysOnTheProfile() {
        coEvery { controllers.messages.startConversationWithUser("user-1") } throws IllegalStateException()
        openProfile(entrant)

        composeTestRule.onNodeWithTag("adminMessageUserButton").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Could not open a conversation. Please try again.")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("screen_admin_profile_details").assertIsDisplayed()
    }

    /**
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Okay so there are no conversations yet so can I have admins be able to message users
     *   from the profile admin screen"
     */
    @Test
    fun messageButtonIsHiddenOnTheAdminsOwnProfile() {
        showDetails(shown = admin, signedIn = admin)

        composeTestRule.onNodeWithTag("adminRemoveProfileButton").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("adminMessageUserButton").assertDoesNotExist()
    }

    @Test
    fun usersWhoAreNotAdminsCannotSeeTheProfile() {
        showDetails(shown = entrant, signedIn = CampsiteOwner(deviceId = "owner-1"))

        composeTestRule.onNodeWithText("You do not have access to this page.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("adminMakeAdminButton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("adminRemoveProfileButton").assertDoesNotExist()
        coVerify(exactly = 0) { users.getUser(any()) }
    }

    @Test
    fun missingProfileHasNoActions() {
        showDetails(shown = null, signedIn = admin)

        composeTestRule.onNodeWithText("Profile not found.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("adminMakeAdminButton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("adminRemoveProfileButton").assertDoesNotExist()
    }
}

package com.example.lambda_bbb_events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.view.ui.CampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.CreateScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.EditCampsiteScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.OwnerCampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.OwnerManageLotteryScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.MyCampsitesScreen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import io.mockk.coEvery
import io.mockk.coVerify


@RunWith(AndroidJUnit4::class)
class CreateCampsiteScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()
    private val controllers = mockk<AppControllers>(relaxed = true)

    @Before
    fun setup() {
        every { controllers.users.user.deviceId } returns "test-owner"
        val campsite = mockk<Campsite>(relaxed = true)

        every { campsite.campsiteId } returns "test-campsite-1"
        every { campsite.campsiteName } returns "Test campsite"
        every { campsite.address } returns "123 Test St"
        every { campsite.maxOccupancy } returns 4
        every { campsite.description } returns "Test description"
        every { campsite.rulesPolicies } returns "No fires"
        every { campsite.tags } returns emptyList()

        every {
            controllers.campsites.getCampsitesByOwner("test-owner")
        } returns listOf(campsite)

        every {
            controllers.campsites.getCampsite("test-campsite-1")
        } returns campsite

        coEvery {

            controllers.campsites.updateCampsite(
                any(), any(), any(), any(), any(), any(), any()
            )
        } returns true

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "create_campsite"
            ) {
                composable("create_campsite") {
                    CreateScreen(
                        navController = navController,
                        controllers = controllers,
                        showMap = false
                    )
                }

                composable("home") {
                    Box(
                        Modifier.fillMaxSize()
                            .testTag("test_home")
                    )
                }

                composable("my_campsites") {
                    MyCampsitesScreen(navController, controllers)
                }

                composable("owner_campsite_details/{campsiteId}") { entry ->
                    val campsiteId = entry.arguments?.getString("campsiteId")
                        ?: return@composable

                    Box(
                        Modifier.fillMaxSize().testTag("test_owner_details")
                    ) {
                        OwnerCampsiteDetailsScreen(
                            navController,
                            controllers,
                            campsiteId
                        )
                    }
                }

                composable("edit_campsite/{campsiteId}") { entry ->
                    val campsiteId = entry.arguments?.getString("campsiteId")
                        ?: return@composable

                    Box(
                        Modifier.fillMaxSize().testTag("test_edit_campsite")
                    ) {
                        EditCampsiteScreen(
                            navController,
                            controllers,
                            campsiteId
                        )
                    }
                }

                composable("campsite_details/{campsiteId}") { entry ->
                    val campsiteId = entry.arguments?.getString("campsiteId")
                        ?: return@composable

                    Box(
                        Modifier.fillMaxSize().testTag("test_view_campsite")
                    ) {
                        CampsiteDetailsScreen(
                            navController,
                            controllers,
                            campsiteId
                        )
                    }
                }

                composable("manage_lottery/{campsiteId}") { entry ->
                    val campsiteId = entry.arguments?.getString("campsiteId")
                        ?: return@composable

                    Box(
                        Modifier.fillMaxSize().testTag("test_manage_lottery")
                    ) {
                        OwnerManageLotteryScreen(
                            navController,
                            controllers,
                            campsiteId
                        )
                    }
                }

            }
        }
    }

    private fun openMyCampsites() {
        composeTestRule.onNodeWithTag("nav_create")
            .performClick()

        composeTestRule.onNodeWithText("My Campsites")
            .assertIsDisplayed()
    }

    private fun openOwnerDetails() {
        openMyCampsites()

        composeTestRule.onNodeWithText("Test campsite")
            .performClick()

        composeTestRule.onNodeWithTag("test_owner_details")
            .assertIsDisplayed()
    }

    @Test
    fun createCampsiteScreenIsDisplayed() {
        composeTestRule.onNodeWithTag("screen_create")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Basic Info")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("create_name")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("create_description")
            .assertIsDisplayed()
    }

    @Test
    fun enterBasicCampsiteInformation() {
        composeTestRule.onNodeWithTag("create_name")
            .performTextInput("Test campsite")

        composeTestRule.onNodeWithTag("create_description")
            .performScrollTo()
            .performTextInput("Test description")

        composeTestRule.onNodeWithTag("create_name")
            .assertTextContains("Test campsite")

        composeTestRule.onNodeWithTag("create_description")
            .assertTextContains("Test description")
    }

    @Test
    fun navigateBasicInfoToLocation() {
        composeTestRule.onNodeWithText("Add Locations →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("create_address")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("create_postal_code")
            .assertIsDisplayed()
    }

    @Test
    fun navigateLocationToLottery() {
        composeTestRule.onNodeWithText("Add Locations →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("create_address")
            .performTextInput("123 Test St")

        composeTestRule.onNodeWithTag("create_postal_code")
            .performTextInput("1A2 3B4")

        composeTestRule.onNodeWithText("Set Lottery →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Lottery Date *")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Start Registration *")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("End Registration *")
            .assertIsDisplayed()
    }

    @Test
    fun navigateBackToBasicInfo() {
        composeTestRule.onNodeWithText("Add Locations →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Set Lottery →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("← Back")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("create_address")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("← Back")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("create_name")
            .assertIsDisplayed()
    }

    @Test
    fun createCampsiteAndNavigateHome() {
        composeTestRule.onNodeWithTag("create_name")
            .performTextInput("Test campsite")

        composeTestRule.onNodeWithTag("create_description")
            .performScrollTo()
            .performTextInput("Test description")

        composeTestRule.onNodeWithText("Add Locations →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("create_address")
            .performTextInput("123 Test St")

        composeTestRule.onNodeWithTag("create_postal_code")
            .performTextInput("T6G 2R3")

        composeTestRule.onNodeWithText("Set Lottery →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Lottery Date *")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Done")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("test_home")
            .assertIsDisplayed()

        coVerify (exactly = 1) {
            controllers.campsites.addCampsite(
                match {
                    it.campsiteName == "Test campsite" &&
                            it.description == "Test description" &&
                            it.address == "123 Test St, T6G 2R3"
                }
            )
        }
    }

    @Test
    fun navigateMyCampsitesToCreate() {
        openMyCampsites()

        composeTestRule.onNodeWithText("+ Create Campsite")
            .performClick()

        composeTestRule.onNodeWithTag("screen_create")
            .assertIsDisplayed()
    }

    @Test
    fun navigateMyCampsitesToOwnerDetails() {
        openOwnerDetails()

        composeTestRule.onNodeWithText("Test campsite")
            .assertIsDisplayed()
    }

    @Test
    fun navigateOwnerDetailsToEdit() {
        openOwnerDetails()

        composeTestRule.onNodeWithText("Edit")
            .performClick()

        composeTestRule.onNodeWithTag("test_edit_campsite")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Edit campsite")
            .assertIsDisplayed()
    }

    @Test
    fun saveEditedCampsite() {
        openOwnerDetails()

        composeTestRule.onNodeWithText("Edit")
            .performClick()

        composeTestRule.onNodeWithTag("test_edit_campsite")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Save changes")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("test_owner_details")
            .assertIsDisplayed()

        val campsiteController = controllers.campsites

        coVerify(exactly = 1) {
            campsiteController.updateCampsite(
                "test-campsite-1",
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun navigateOwnerDetailsToView() {
        openOwnerDetails()

        composeTestRule.onNodeWithText("View")
            .performClick()

        composeTestRule.onNodeWithTag("test_view_campsite")
            .assertIsDisplayed()
    }

    @Test
    fun navigateOwnerDetailsToManageLottery() {
        openOwnerDetails()

        composeTestRule.onNodeWithText("Manage Lottery")
            .performClick()

        composeTestRule.onNodeWithTag("test_manage_lottery")
            .assertIsDisplayed()
    }
}


package com.example.lambda_bbb_events.ui.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.printToLog
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.view.ui.user_screens.explore.CampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.user_screens.create.CreateScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.EditCampsiteScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.OwnerCampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.OwnerManageLotteryScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.MyCampsitesScreen
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateCampsiteScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val controllers = mockk<AppControllers>(relaxed = true)
    private lateinit var navController: NavHostController

    @Before
    fun setup() {
        val testUser = mockk<User>(relaxed = true)

        every { testUser.deviceId } returns "test-owner"
        every { controllers.users.currentUser } returns testUser

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

        every {
            controllers.campsites.updateCampsite(
                any(), any(), any(), any(), any(), any(), any<List<String>>()
            )
        } returns true

        composeTestRule.setContent {
            val controller = rememberNavController()
            navController = controller

            NavHost(
                navController = controller,
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
                        Modifier
                            .fillMaxSize()
                            .testTag("test_home")
                    )
                }

                composable("my_campsites") {
                    MyCampsitesScreen(
                        navController,
                        controllers
                    )
                }

                composable("owner_campsite_details/{campsiteId}") { entry ->
                    val campsiteId = entry.arguments?.getString("campsiteId")
                        ?: return@composable

                    Box(
                        Modifier
                            .fillMaxSize()
                            .testTag("test_owner_details")
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
                        Modifier
                            .fillMaxSize()
                            .testTag("test_edit_campsite")
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
                        Modifier
                            .fillMaxSize()
                            .testTag("test_view_campsite")
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
                        Modifier
                            .fillMaxSize()
                            .testTag("test_manage_lottery")
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

    private fun enterLotteryDates() {
        listOf(
            "create_start_registration" to "20300101",
            "create_end_registration" to "20300105",
            "create_start_booking" to "20300110",
            "create_end_booking" to "20300115"
        ).forEach { (tag, digits) ->
            composeTestRule.onNodeWithTag(tag)
                .performScrollTo()
                .performTextInput(digits)
        }
    }

    private fun openMyCampsites() {
        composeTestRule.runOnIdle {
            navController.navigate("my_campsites")
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("+ Create Campsite")
            .assertExists()

        composeTestRule.onRoot().printToLog("CAMPSITE_TEST")

        verify(atLeast = 1) {
            controllers.campsites.getCampsitesByOwner("test-owner")
        }
    }

    // Navigate from My Campsites to Owner Details
    private fun openOwnerDetails() {
        openMyCampsites()

        composeTestRule.onNodeWithText("Test campsite")
            .performClick()

        composeTestRule.onNodeWithTag("test_owner_details")
            .assertIsDisplayed()
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
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
            .performScrollTo()
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
            .assertExists()

        composeTestRule.onNodeWithTag("create_postal_code")
            .assertExists()
    }

    @Test
    fun navigateLocationToLottery() {
        composeTestRule.onNodeWithText("Add Locations →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("create_address")
            .performScrollTo()
            .performTextInput("123 Test St")

        composeTestRule.onNodeWithTag("create_postal_code")
            .performScrollTo()
            .performTextInput("T6G 2R3")

        composeTestRule.onNodeWithText("Set Lottery →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Start Registration *")
            .assertExists()

        composeTestRule.onNodeWithText("End Registration *")
            .assertExists()
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
            .assertExists()

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
            .performScrollTo()
            .performTextInput("123 Test St")

        composeTestRule.onNodeWithTag("create_postal_code")
            .performScrollTo()
            .performTextInput("T6G 2R3")

        composeTestRule.onNodeWithText("Set Lottery →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Start Registration *")
            .assertExists()

        enterLotteryDates()

        composeTestRule.onNodeWithText("Done")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("test_home")
            .assertIsDisplayed()

        verify(exactly = 1) {
            controllers.campsites.addCampsite(
                campsiteId = any(),
                ownerId = any(),
                campsiteName = "Test campsite",
                description = "Test description",
                address = "123 Test St, T6G 2R3",
                latitude = any(),
                longitude = any(),
                maxOccupancy = any(),
                rulesPolicies = any(),
                amenities = any(),
                tags = any(),
                imageIds = any(),
                rating = any(),
                ratingCount = any(),
                availability = any(),
                active = any(),
                returnMessage = any()
            )
        }
    }

    private fun openLotteryStep() {
        composeTestRule.onNodeWithTag("create_name")
            .performTextInput("Test campsite")

        composeTestRule.onNodeWithText("Add Locations →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("create_address")
            .performScrollTo()
            .performTextInput("123 Test St")

        composeTestRule.onNodeWithText("Set Lottery →")
            .performScrollTo()
            .performClick()
    }

    private fun enterSpots(text: String) {
        composeTestRule.onNodeWithTag("create_number_of_spots")
            .performScrollTo()
            .performTextReplacement(text)
    }

    @Test
    fun typedNumberOfSpotsIsUsedForLottery() {
        openLotteryStep()
        enterLotteryDates()
        enterSpots("37")

        composeTestRule.onNodeWithText("Done")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        coVerify(exactly = 1) {
            controllers.lotteries.createLottery(
                ownerIds = any<List<String>>(),
                campsiteIds = any(),
                registrationStart = any(),
                registrationEnd = any(),
                bookingStart = any(),
                bookingEnd = any(),
                numberOfWinnersString = "37",
                waitlistCapacityString = "37"
            )
        }
    }

    @Test
    fun numberOfSpotsOnlyAcceptsDigits() {
        openLotteryStep()
        enterSpots("1a2-5.")

        composeTestRule.onNodeWithTag("create_number_of_spots")
            .assertTextEquals("125")
    }

    @Test
    fun missingNumberOfSpotsShowsError() {
        openLotteryStep()
        enterLotteryDates()
        enterSpots("")

        composeTestRule.onNodeWithText("Done")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Enter the number of spots (at least 1).")
            .performScrollTo()
            .assertIsDisplayed()
        verify(exactly = 0) {
            controllers.campsites.addCampsite(
                campsiteId = any(),
                ownerId = any(),
                campsiteName = any(),
                description = any(),
                address = any(),
                latitude = any(),
                longitude = any(),
                maxOccupancy = any(),
                rulesPolicies = any(),
                amenities = any(),
                tags = any(),
                imageIds = any(),
                rating = any(),
                ratingCount = any(),
                availability = any(),
                active = any(),
                returnMessage = any()
            )
        }
    }

    @Test
    fun zeroSpotsShowsError() {
        openLotteryStep()
        enterLotteryDates()
        enterSpots("0")

        composeTestRule.onNodeWithText("Done")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Enter the number of spots (at least 1).")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun typedMaxOccupancyIsSaved() {
        composeTestRule.onNodeWithTag("create_max_occupancy")
            .performScrollTo()
            .performTextReplacement("12")
        openLotteryStep()
        enterLotteryDates()

        composeTestRule.onNodeWithText("Done")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        verify(exactly = 1) {
            controllers.campsites.addCampsite(
                campsiteId = any(),
                ownerId = any(),
                campsiteName = any(),
                description = any(),
                address = any(),
                latitude = any(),
                longitude = any(),
                maxOccupancy = 12,
                rulesPolicies = any(),
                amenities = any(),
                tags = any(),
                imageIds = any(),
                rating = any(),
                ratingCount = any(),
                availability = any(),
                active = any(),
                returnMessage = any()
            )
        }
    }

    @Test
    fun maxOccupancyOnlyAcceptsDigits() {
        composeTestRule.onNodeWithTag("create_max_occupancy")
            .performScrollTo()
            .performTextReplacement("4 people")

        composeTestRule.onNodeWithTag("create_max_occupancy")
            .assertTextEquals("4")
    }

    @Test
    fun missingMaxOccupancyBlocksNextStep() {
        composeTestRule.onNodeWithTag("create_max_occupancy")
            .performScrollTo()
            .performTextReplacement("")

        composeTestRule.onNodeWithText("Add Locations →")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Enter a max occupancy of at least 1.")
            .assertExists()
        composeTestRule.onNodeWithTag("create_address")
            .assertDoesNotExist()
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
    }

    @Test
    fun saveEditedCampsite() {
        openOwnerDetails()

        composeTestRule.onNodeWithText("Edit")
            .performClick()

        composeTestRule.onNodeWithTag("test_edit_campsite")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Save changes")
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("test_owner_details")
            .assertIsDisplayed()

        val campsiteController = controllers.campsites

        verify(exactly = 1) {
            campsiteController.updateCampsite(
                campsiteId = "test-campsite-1",
                name = "Test campsite",
                address = "123 Test St",
                maxOccupancyText = "4",
                description = "Test description",
                rulesPolicies = "No fires",
                tags = match { it.isEmpty() }
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

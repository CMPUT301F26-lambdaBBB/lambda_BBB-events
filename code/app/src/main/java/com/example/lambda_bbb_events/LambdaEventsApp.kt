package com.example.lambda_bbb_events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.owner_screens.CreateLotteryScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.ActiveBookingsScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.CampsiteCheckAvailabilityScreen
import com.example.lambda_bbb_events.view.ui.user_screens.explore.CampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.user_screens.create.CreateScreen
import com.example.lambda_bbb_events.view.ui.user_screens.profile.EditProfileScreen
import com.example.lambda_bbb_events.view.ui.user_screens.explore.ExploreMapScreen
import com.example.lambda_bbb_events.view.ui.user_screens.explore.ExploreDefaultScreen
import com.example.lambda_bbb_events.view.ui.user_screens.explore.QRCodeScreen
import com.example.lambda_bbb_events.view.ui.user_screens.explore.QRScannerScreen
import com.example.lambda_bbb_events.view.ui.user_screens.home.HomeScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.LotteryBookingsScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.lottery.LotteryEnteredScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.MyCampsitesScreen
import com.example.lambda_bbb_events.view.ui.user_screens.notification.NotificationsScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.OwnerCampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.PastBookingsScreen
import com.example.lambda_bbb_events.view.ui.user_screens.profile.ProfileScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.lottery.RegistrationStatusScreen
import com.example.lambda_bbb_events.view.ui.user_screens.signup.RuleConfirmScreen
import com.example.lambda_bbb_events.view.ui.user_screens.signup.SignupScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.EditCampsiteScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.EditLotteryScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.OwnerLotteryDetailsScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.OwnerManageLotteryScreen
import com.example.lambda_bbb_events.view.ui.user_screens.bookings.lottery.LotteryAvailabilityScreen
import com.example.lambda_bbb_events.view.ui.user_screens.profile.Settings
import com.example.lambda_bbb_events.view.ui.user_screens.signup.Test_screen
import java.time.LocalDate

/*
 * Displays loading and signup states before opening the
 * main application, and defines navigation routes for
 * campsite browsing, reservations, lotteries, and profiles.
 *
 * layout issues on smaller screens could be an issue.
 */

/**
 * Root composable that controls application startup.
 *
 * Displays a loading indicator while user data loads,
 * then opens either the signup screen or the main application
 * depending on whether a user has an account.
 *
 * @param controllers The [AppControllers] managing application data.
 */
@Composable
fun LambdaEventsApp(
    controllers: AppControllers
) {
    if (!controllers.users.usersFullyLoaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (controllers.users.currentUser == null) {
        // no profile so show sign up
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp)
            ) {
                SignupScreen(rememberNavController(), controllers)
            }
        }
    } else {
        MainApp(controllers)
    }
}

/**
 * Configures the application's navigation graph and screens.
 *
 * Defines routes for campsite browsing, bookings, lottery
 * registration, profile management, and owner features.
 *
 * @param controllers The [AppControllers] shared across screens.
 */
@Composable
private fun MainApp(controllers: AppControllers) {
    // NOTE: Screen looks too zoomed on small phone so cant login, someone test
    // and try to fix if happens for u too thx
    val navController = rememberNavController()
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 32.dp)
        ) {
            composable("signup") { SignupScreen(navController, controllers) }
            composable("home") { HomeScreen(navController, controllers) }
            composable("active_bookings") { ActiveBookingsScreen(navController, controllers) }
            composable("past_bookings") { PastBookingsScreen(navController) }
            composable("lottery_bookings") { LotteryBookingsScreen(navController, controllers) }
            composable("explore_default") { ExploreDefaultScreen(navController, controllers) }
            composable("explore_map") { ExploreMapScreen(navController, controllers) }
            composable("qr_scanner") { QRScannerScreen() }
            composable("campsite_details/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                CampsiteDetailsScreen(navController, controllers, campsiteId)
            }
            composable("campsite_qr_code/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                QRCodeScreen(navController, controllers, campsiteId)
            }
            composable("campsite_check_availability/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                CampsiteCheckAvailabilityScreen(navController, controllers, campsiteId)
            }
            composable("owner_campsite_details/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                OwnerCampsiteDetailsScreen(navController, controllers, campsiteId)
            }
            composable("create_lottery/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                CreateLotteryScreen(navController, controllers, campsiteId)
            }
            composable("edit_lottery/{lotteryId}") { backStackEntry ->
                val lotteryId = backStackEntry.arguments?.getString("lotteryId") ?: ""
                EditLotteryScreen(navController, controllers, lotteryId)
            }
            composable("lottery_details/{lotteryId}") { backStackEntry ->
                val lotteryId = backStackEntry.arguments?.getString("lotteryId") ?: ""
                OwnerLotteryDetailsScreen(navController, controllers, lotteryId)
            }
            composable("registrationStatus/{entryId}") { backStackEntry ->
                val entryId =
                    backStackEntry.arguments?.getString("entryId") ?: ""

                RegistrationStatusScreen(
                    navController = navController,
                    controllers = controllers,
                    entryId = entryId
                )
            }

            composable(
                route = "lotteryAvailability/{campsiteId}/{fromDate}/{endDate}/{numPeople}"
            ) { backStackEntry ->

                val campsiteId =
                    backStackEntry.arguments?.getString("campsiteId") ?: ""

                val fromDate = LocalDate.parse(
                    backStackEntry.arguments?.getString("fromDate")
                )

                val endDate = LocalDate.parse(
                    backStackEntry.arguments?.getString("endDate")
                )

                val numPeople =
                    backStackEntry.arguments?.getString("numPeople")?.toIntOrNull() ?: 1

                LotteryAvailabilityScreen(
                    navController = navController,
                    controllers = controllers,
                    campsiteId = campsiteId,
                    fromDate = fromDate,
                    endDate = endDate,
                    numPeople = numPeople
                )
            }

            composable(
                route = "rule_confirm/{campsiteId}/{fromDate}/{endDate}/{numPeople}"
            ) { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                val fromDate = LocalDate.parse(backStackEntry.arguments?.getString("fromDate"))
                val endDate = LocalDate.parse(backStackEntry.arguments?.getString("endDate"))
                val numPeople = backStackEntry.arguments?.getString("numPeople")?.toIntOrNull() ?: 1

                RuleConfirmScreen(
                    navController = navController,
                    controllers = controllers,
                    campsiteId = campsiteId,
                    fromDate = fromDate,
                    endDate = endDate,
                    numPeople = numPeople
                )
            }
            composable(
                route = "lottery_entered/{campsiteId}/{fromDate}/{endDate}/{numPeople}"
            ) { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                val fromDate = LocalDate.parse(backStackEntry.arguments?.getString("fromDate"))
                val endDate = LocalDate.parse(backStackEntry.arguments?.getString("endDate"))
                val numPeople = backStackEntry.arguments?.getString("numPeople")?.toIntOrNull() ?: 1

                LotteryEnteredScreen(
                    navController = navController,
                    controllers = controllers,
                    campsiteId = campsiteId,
                    fromDate = fromDate,
                    endDate = endDate,
                    numPeople = numPeople
                )
            }

            composable("notifications") { NotificationsScreen(navController) }
            composable("my_campsites") { MyCampsitesScreen(navController, controllers) }
            composable("create_campsite") { CreateScreen(navController, controllers) }
            composable("edit_campsite/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                EditCampsiteScreen(navController, controllers, campsiteId)
            }
            composable("manage_lottery/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                OwnerManageLotteryScreen(navController, controllers, campsiteId)
            }
            composable("profile") {
                ProfileScreen(navController, controllers)
            }
            composable("edit_profile") {
                EditProfileScreen(navController, controllers)
            }
            composable("settings") {
                Settings(navController, controllers)
            }
            composable("test_nav"){
                Test_screen(navController, controllers)
            }
        }
    }
}
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
import com.example.lambda_bbb_events.view.ui.ActiveBookingsScreen
import com.example.lambda_bbb_events.view.ui.CampsiteCheckAvailabilityScreen
import com.example.lambda_bbb_events.view.ui.CampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.CreateScreen
import com.example.lambda_bbb_events.view.ui.EditProfileScreen
import com.example.lambda_bbb_events.view.ui.ExploreMapScreen
import com.example.lambda_bbb_events.view.ui.ExploreDefaultScreen
import com.example.lambda_bbb_events.view.ui.HomeScreen
import com.example.lambda_bbb_events.view.ui.LotteryBookingsScreen
import com.example.lambda_bbb_events.view.ui.LotteryEnteredScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.MyCampsitesScreen
import com.example.lambda_bbb_events.view.ui.NotificationsScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.OwnerCampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.PastBookingsScreen
import com.example.lambda_bbb_events.view.ui.ProfileScreen
import com.example.lambda_bbb_events.view.ui.RegistrationStatusScreen
import com.example.lambda_bbb_events.view.ui.SignupScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.EditCampsiteScreen
import com.example.lambda_bbb_events.view.ui.owner_screens.OwnerManageLotteryScreen
import java.time.LocalDate

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

@Composable
private fun MainApp(controllers: AppControllers) {
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
            composable("active_bookings") { ActiveBookingsScreen(navController) }
            composable("past_bookings") { PastBookingsScreen(navController) }
            composable("lottery_bookings") { LotteryBookingsScreen(navController, controllers) }
            composable("explore_default") { ExploreDefaultScreen(navController, controllers) }
            composable("explore_map") { ExploreMapScreen(navController, controllers) }
            composable("campsite_details/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                CampsiteDetailsScreen(navController, controllers, campsiteId)
            }
            composable("campsite_check_availability/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                CampsiteCheckAvailabilityScreen(navController, controllers, campsiteId)
            }
            composable("owner_campsite_details/{campsiteId}") { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                OwnerCampsiteDetailsScreen(navController, controllers, campsiteId)
            }
            composable(
                route = "registrationStatus/{campsiteId}/{fromDate}/{endDate}/{numPeople}"
            ) { backStackEntry ->
                val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                val fromDate = LocalDate.parse(backStackEntry.arguments?.getString("fromDate"))
                val endDate = LocalDate.parse(backStackEntry.arguments?.getString("endDate"))
                val numPeople = backStackEntry.arguments?.getString("numPeople")?.toIntOrNull() ?: 1

                RegistrationStatusScreen(
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
        }
    }
}
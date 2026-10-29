package com.example.lambda_bbb_events

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lambda_bbb_events.view.ui.ActiveBookingsScreen
import com.example.lambda_bbb_events.view.ui.CampsiteDetailsScreen
import com.example.lambda_bbb_events.view.ui.CreateScreen
import com.example.lambda_bbb_events.view.ui.ExploreMapScreen
import com.example.lambda_bbb_events.view.ui.ExploreDefaultScreen
import com.example.lambda_bbb_events.view.ui.HomeScreen
import com.example.lambda_bbb_events.view.ui.LotteryBookingsScreen
import com.example.lambda_bbb_events.view.ui.NotificationsScreen
import com.example.lambda_bbb_events.view.ui.PastBookingsScreen
import com.example.lambda_bbb_events.view.ui.ProfileScreen

@Composable
fun LambdaEventsApp() {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding).padding(all = 32.dp)
        ) {
            composable("home") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.HomeScreen(
                    navController
                )
            }
            composable("active_bookings") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.ActiveBookingsScreen(
                    navController
                )
            }
            composable("past_bookings") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.PastBookingsScreen(
                    navController
                )
            }
            composable("lottery_bookings") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.LotteryBookingsScreen(
                    navController
                )
            }
            composable("campsite_details") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.CampsiteDetailsScreen(
                    navController
                )
            }
            composable("exploreDefault") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.ExploreDefaultScreen(
                    navController
                )
            }
            composable("exploreMap") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.ExploreMapScreen(
                    navController
                )
            }
            composable("notifications") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.NotificationsScreen(
                    navController
                )
            }
            composable("create") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.CreateScreen(
                    navController
                )
            }
            composable("profile") {
                _root_ide_package_.com.example.lambda_bbb_events.view.ui.ProfileScreen(
                    navController
                )
            }
        }
    }
}
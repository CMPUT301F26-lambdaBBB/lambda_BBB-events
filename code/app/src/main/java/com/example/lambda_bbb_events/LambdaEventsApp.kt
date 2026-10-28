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
import com.example.lambda_bbb_events.ui.ActiveBookingsScreen
import com.example.lambda_bbb_events.ui.CampsiteDetailsScreen
import com.example.lambda_bbb_events.ui.CreateScreen
import com.example.lambda_bbb_events.ui.ExploreMapScreen
import com.example.lambda_bbb_events.ui.ExploreDefaultScreen
import com.example.lambda_bbb_events.ui.HomeScreen
import com.example.lambda_bbb_events.ui.LotteryBookingsScreen
import com.example.lambda_bbb_events.ui.NotificationsScreen
import com.example.lambda_bbb_events.ui.PastBookingsScreen
import com.example.lambda_bbb_events.ui.ProfileScreen

@Composable
fun LambdaEventsApp() {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding).padding(all = 32.dp)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("active_bookings") { ActiveBookingsScreen(navController) }
            composable("past_bookings") { PastBookingsScreen(navController) }
            composable("lottery_bookings") { LotteryBookingsScreen(navController) }
            composable("campsite_details") { CampsiteDetailsScreen(navController) }
            composable("exploreDefault") { ExploreDefaultScreen(navController) }
            composable("exploreMap") { ExploreMapScreen(navController) }
            composable("notifications") { NotificationsScreen(navController) }
            composable("create") { CreateScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
        }
    }
}
package com.example.lambda_bbb_events

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lambda_bbb_events.ui.ActiveBookingsScreen
import com.example.lambda_bbb_events.ui.CampsiteDetailsScreen
import com.example.lambda_bbb_events.ui.ExploreScreen
import com.example.lambda_bbb_events.ui.HomeScreen
import com.example.lambda_bbb_events.ui.LotteryBookingsScreen
import com.example.lambda_bbb_events.ui.PastBookingsScreen

@Composable
fun LambdaEventsApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "Home",
        modifier = Modifier.fillMaxSize().padding(all = 32.dp)
    ) {
        composable("home") { HomeScreen(navController) }
        composable("explore") { ExploreScreen(navController) }
        composable("active_bookings") { ActiveBookingsScreen(navController) }
        composable("past_bookings") { PastBookingsScreen(navController) }
        composable("lottery_bookings") { LotteryBookingsScreen(navController) }
        composable("campsiteDetails") { CampsiteDetailsScreen(navController) }
    }
}
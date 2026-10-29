package com.example.lambda_bbb_events.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BookingsTypeBar(navController: NavController) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Active Reservations
        Box (
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (currentRoute == "active_bookings") _root_ide_package_.com.example.lambda_bbb_events.view.ui.theme.Purple80
                    else Color.Transparent
                )
                .clickable{
                    navController.navigate("active_bookings") {
                        launchSingleTop = true
                    }
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Active")
        }

        // Past Reservations
        Box (
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (currentRoute == "past_bookings") _root_ide_package_.com.example.lambda_bbb_events.view.ui.theme.Purple80
                    else Color.Transparent
                )
                .clickable{
                    navController.navigate("past_bookings") {
                        launchSingleTop = true
                    }
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Past")
        }

        // Lottery Reservations
        Box (
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (currentRoute == "lottery_bookings") _root_ide_package_.com.example.lambda_bbb_events.view.ui.theme.Purple80
                    else Color.Transparent
                )
                .clickable{
                    navController.navigate("lottery_bookings") {
                        launchSingleTop = true
                    }
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Lottery")
        }
    }
}
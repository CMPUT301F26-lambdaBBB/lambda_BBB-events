package com.example.lambda_bbb_events.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.ui.components.ScreenTitle
import com.example.lambda_bbb_events.ui.components.SubTitle
import com.example.lambda_bbb_events.ui.components.VerticalCampsiteCard
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar

@Composable
fun PastBookingsScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            ScreenTitle(title = "My Reservations", navController = navController)
            BookingsTypeBar(navController)
            Spacer(modifier = Modifier.height(25.dp))
            SubTitle(title = "Past Reservations")
            Spacer(modifier = Modifier.height(25.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(10) {
                    VerticalCampsiteCard()
                }
            }
        }
    }
}

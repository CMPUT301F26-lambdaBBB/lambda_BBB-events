package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Reservation
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.model.repository.ReservationRepository
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SubTitle
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar


/**
 * Displays the user's campsite lottery reservations.
 *
 * Provides navigation between active, past, and lottery
 * reservations, along with the application's main screens.
 *
 * @param navController Handles screen navigation.
 */
@Composable
fun LotteryBookingsScreen(navController: NavController, controllers: AppControllers) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController
            )
        }
    ){ innerPadding ->

        Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding).testTag("lottery_bookings")
        ) {
            ScreenTitle(
                title = "My Reservations",
                navController = navController
            )
            BookingsTypeBar(navController)
            Spacer(modifier = Modifier.height(25.dp))
            SubTitle(title = "In Lottery")
            Spacer(modifier = Modifier.height(25.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


//                items(reservations.size) { index ->
//                    VerticalCampsiteCard(res_all[index])
//                }
            }
        }
    }
}

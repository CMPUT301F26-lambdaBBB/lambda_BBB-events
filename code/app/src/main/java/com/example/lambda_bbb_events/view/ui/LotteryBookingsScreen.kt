package com.example.lambda_bbb_events.view.ui

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

@Composable
fun LotteryBookingsScreen(navController: NavController, controllers: AppControllers) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController
            )
        }
    ){ innerPadding ->

        val reservationRepo = ReservationRepository()
        val reservations: List<Reservation> = reservationRepo.getReservationsForEntrant( controllers.users.user.deviceId)


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
                items(reservations.size) { index ->
                    VerticalCampsiteCard(reservations[index])
                }
            }
        }
    }
}

@Composable
fun VerticalCampsiteCard(reservation: Reservation){

    val campsite = CampsiteRepository().get(reservation.campsiteId)
}

package com.example.lambda_bbb_events.view.ui.user_screens.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.reservation.ReservationStatus
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar
import java.time.LocalDate

/**
 * Displays the user's past campsite reservations.
 *
 * Provides navigation between reservation categories
 * and a scrollable list of past reservations.
 *
 * @param navController The [NavController] used for navigation.
 * @param controllers The [AppControllers] used to access reservation and campsite data.
 */
@Composable
fun PastBookingsScreen(
    navController: NavController,
    controllers: AppControllers
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        val reservations = controllers.reservations
            .getReservationsForEntrant(controllers.users.user.deviceId)
            .filter { reservation ->
                val isCancelled = reservation.status == ReservationStatus.CANCELLED ||
                        reservation.status == ReservationStatus.REJECTED
                val endDate = runCatching { LocalDate.parse(reservation.endDate) }.getOrNull()
                val hasEnded = reservation.status == ReservationStatus.COMPLETED ||
                        (endDate != null && endDate.isBefore(LocalDate.now()))
                !isCancelled && hasEnded
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .testTag("past_bookings")
        ) {
            ScreenTitle(
                title = "My Reservations",
                navController = navController
            )

            BookingsTypeBar(navController)

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Past Reservations",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(25.dp))

            if (reservations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No past reservations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reservations) { reservation ->
                        ReservationCard(
                            modifier = Modifier.fillMaxWidth(),
                            reservation = reservation,
                            campsite = controllers.campsites
                                .getCampsite(reservation.campsiteId)
                        )
                    }
                }
            }
        }
    }
}

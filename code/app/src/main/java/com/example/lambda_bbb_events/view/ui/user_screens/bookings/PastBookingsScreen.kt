package com.example.lambda_bbb_events.view.ui.user_screens.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.lambda_bbb_events.model.formatting.parseDateOrNull
import com.example.lambda_bbb_events.model.reservation.ReservationStatus
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar
import java.time.LocalDate

/**
 * Displays the user's past campsite reservations.
 *
 * Includes completed, cancelled, rejected, and expired
 * reservations, sorted by the most recent ending date.
 *
 * Provides navigation between reservation categories
 * and displays reservation history in a scrollable list.
 *
 * @param navController Handles screen navigation.
 * @param controllers Provides access to reservation and campsite data.
 */
@Composable
fun PastBookingsScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val reservations = controllers.reservations
        .getReservationsForEntrant(
            controllers.users.user.deviceId
        )
        .filter { reservation ->

            val endDate = parseDateOrNull(
                reservation.endDate
            )

            val hasEnded =
                endDate != null &&
                        endDate.isBefore(LocalDate.now())

            val isInactive =
                reservation.status == ReservationStatus.CANCELLED ||
                        reservation.status == ReservationStatus.COMPLETED ||
                        reservation.status == ReservationStatus.REJECTED

            isInactive || hasEnded
        }
        .sortedByDescending {
            parseDateOrNull(it.endDate) ?: LocalDate.MIN
        }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .testTag("past_bookings")
        ) {

            ScreenTitle(
                title = "My Reservations",
                navController = navController
            )

            BookingsTypeBar(navController)

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Past Reservations: ${reservations.size}",
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(
                            "empty_past_bookings"
                        )
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = reservations,
                        key = { it.reservationId }
                    ) { reservation ->

                        ReservationCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(
                                    "past_reservation_${reservation.reservationId}"
                                ),
                            reservation = reservation,
                            campsite = controllers.campsites
                                .getCampsite(reservation.campsiteId),
                            onCancel = null,
                            showChevron = false
                        )
                    }
                }
            }
        }
    }
}
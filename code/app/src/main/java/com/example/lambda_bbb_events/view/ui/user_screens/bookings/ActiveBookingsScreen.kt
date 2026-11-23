
package com.example.lambda_bbb_events.view.ui.user_screens.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.Reservation
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar

/**
 * Displays the user's active campsite reservations.
 *
 * Provides navigation between active, past, and lottery
 * reservations, along with the application's main screens.
 *
 * @param navController Handles screen navigation.
 * @param controllers Provides access to reservation and campsite data.
 */
@Composable
fun ActiveBookingsScreen(
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
            .getReservationsForEntrant(
                controllers.users.user.deviceId
            )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("screen_bookings")
        ) {
            ScreenTitle(
                title = "My Reservations",
                navController = navController
            )

            BookingsTypeBar(navController)

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Active Reservations",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(25.dp))

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

/**
 * Displays a campsite reservation card.
 *
 * @param reservation Reservation information.
 * @param campsite Campsite associated with the reservation.
 * @param modifier Modifier for customizing the card.
 */
@Composable
fun ReservationCard(
    reservation: Reservation,
    campsite: Campsite?,
    modifier: Modifier = Modifier
) {
    val campsiteName = campsite?.campsiteName ?: "Unknown Name"
    val rating = campsite?.rating ?: 0

    Card(
        modifier = modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Campsite name and rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campsiteName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (index in 1..5) {
                        Icon(
                            imageVector = if (index <= rating.toInt()) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = rating.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reservation information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Campsite image placeholder
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Campsite image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Reservation details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DetailRow(
                        Icons.Default.Check,
                        "Confirmed"
                    )

                    DetailRow(
                        Icons.Default.LocationOn,
                        campsite?.address ?: "Unknown Address"
                    )

                    DetailRow(
                        Icons.Default.Home,
                        "Tent"
                    )

                    DetailRow(
                        Icons.Default.Hiking,
                        "${campsite?.latitude}, ${campsite?.longitude}"
                    )

                    DetailRow(
                        Icons.Default.CalendarToday,
                        reservation.startDate
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View reservation",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Displays a reservation detail with an icon.
 *
 * @param icon Icon displayed next to the text.
 * @param text Reservation detail.
 */
@Composable
fun DetailRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

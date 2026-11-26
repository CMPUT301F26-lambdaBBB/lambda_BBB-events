package com.example.lambda_bbb_events.view.ui.user_screens.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.formatting.parseDateOrNull
import com.example.lambda_bbb_events.model.reservation.Reservation
import com.example.lambda_bbb_events.model.reservation.ReservationStatus
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar
import com.example.lambda_bbb_events.view.ui.components.common.CampsiteThumbnail
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.rating.formatRating
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Displays the user's active campsite reservations.
 *
 * Only pending and confirmed reservations that have not ended
 * are displayed. Confirmed reservations can be selected to
 * open their booking receipt.
 *
 * @param navController Handles screen navigation.
 * @param controllers Provides access to reservation and campsite data.
 */
@Composable
fun ActiveBookingsScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val scope = rememberCoroutineScope()

    var reservationToCancel by remember {
        mutableStateOf<Reservation?>(null)
    }

    var isCancelling by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val reservations = controllers.reservations
        .getReservationsForEntrant(
            controllers.users.user.deviceId
        )
        .filter { reservation ->

            val endDate = parseDateOrNull(
                reservation.endDate
            )

            val isActive =
                reservation.status == ReservationStatus.CONFIRMED ||
                        reservation.status == ReservationStatus.PENDING

            val hasEnded =
                endDate != null &&
                        endDate.isBefore(LocalDate.now())

            isActive && !hasEnded
        }
        .sortedBy {
            parseDateOrNull(it.startDate) ?: LocalDate.MAX
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
                .testTag("screen_bookings")
        ) {

            ScreenTitle(
                title = "My Reservations",
                navController = navController
            )

            BookingsTypeBar(navController)

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Active Reservations: ${reservations.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(25.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(
                        "active_bookings_error"
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (reservations.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active reservations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(
                            "empty_active_bookings"
                        )
                    )
                }

            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = reservations,
                        key = { it.reservationId }
                    ) { reservation ->

                        val isConfirmed =
                            reservation.status == ReservationStatus.CONFIRMED

                        ReservationCard(
                            reservation = reservation,
                            campsite = controllers.campsites.getCampsite(
                                reservation.campsiteId
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled = isConfirmed && !isCancelling
                                ) {
                                    navController.navigate(
                                        "booking_receipt/${reservation.reservationId}"
                                    ) {
                                        launchSingleTop = true
                                    }
                                },
                            onCancel = if (
                                reservation.status == ReservationStatus.PENDING &&
                                !isCancelling
                            ) {
                                {
                                    reservationToCancel = reservation
                                }
                            } else {
                                null
                            },
                            showChevron = isConfirmed
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for cancelling pending reservations.
    reservationToCancel?.let { reservation ->

        val campsiteName = controllers.campsites
            .getCampsite(reservation.campsiteId)
            ?.campsiteName ?: "this campsite"

        AlertDialog(
            onDismissRequest = {
                if (!isCancelling) {
                    reservationToCancel = null
                }
            },

            title = {
                Text(
                    text = "Cancel Reservation?",
                    style = MaterialTheme.typography.headlineMedium
                )
            },

            text = {
                Text(
                    text = "Are you sure you want to cancel " +
                            "your reservation at $campsiteName?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = {

                        if (!isCancelling) {

                            isCancelling = true
                            errorMessage = null

                            scope.launch {
                                try {

                                    controllers.reservations
                                        .cancelReservation(reservation)

                                    reservationToCancel = null

                                } catch (e: CancellationException) {
                                    throw e

                                } catch (e: Exception) {

                                    errorMessage = e.message
                                        ?: "Failed to cancel reservation."

                                    reservationToCancel = null

                                } finally {
                                    isCancelling = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = if (isCancelling) {
                            "Cancelling..."
                        } else {
                            "Cancel Reservation"
                        }
                    )
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = {
                        reservationToCancel = null
                    }
                ) {
                    Text("Keep Reservation")
                }
            }
        )
    }
}

/**
 * Displays a campsite reservation card.
 *
 * Shows the campsite name, rating, thumbnail, reservation
 * status, address, coordinates, and reservation dates.
 *
 * @param reservation Reservation information.
 * @param campsite Campsite associated with the reservation.
 * @param modifier Modifier for customizing the card.
 * @param onCancel Optional callback for cancelling a reservation.
 */
@Composable
fun ReservationCard(
    reservation: Reservation,
    campsite: Campsite?,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
    showChevron: Boolean =
        reservation.status == ReservationStatus.CONFIRMED
) {
    val campsiteName =
        campsite?.campsiteName ?: "Unknown Name"

    val rating = campsite?.rating ?: 0f

    val colorScheme = MaterialTheme.colorScheme

    val statusText = when (reservation.status) {
        ReservationStatus.PENDING -> "Pending"
        ReservationStatus.CONFIRMED -> "Confirmed"
        ReservationStatus.COMPLETED -> "Completed"
        ReservationStatus.CANCELLED -> "Cancelled"
        ReservationStatus.REJECTED -> "Rejected"
    }

    Card(
        modifier = modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Campsite name and rating.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = campsiteName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    for (index in 1..5) {

                        Icon(
                            imageVector = if (
                                index <= rating.toInt()
                            ) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = null,
                            tint = colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatRating(rating),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reservation information.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                CampsiteThumbnail(
                    campsite = campsite,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    DetailRow(
                        icon = Icons.Default.Check,
                        text = statusText
                    )

                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        text = campsite?.address
                            ?: "Unknown Address"
                    )

                    DetailRow(
                        icon = Icons.Default.Home,
                        text = "Tent"
                    )

                    DetailRow(
                        icon = Icons.Default.Hiking,
                        text = if (campsite != null) {
                            "${campsite.latitude}, " +
                                    "${campsite.longitude}"
                        } else {
                            "Location unavailable"
                        }
                    )

                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        text = "${reservation.startDate} - " +
                                reservation.endDate
                    )
                }

                // Only confirmed reservations have booking receipts.
                if (showChevron) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View reservation",
                        tint = colorScheme.primary
                    )
                }
            }

            // Display cancellation only when a callback is provided.
            if (
                reservation.isActive() &&
                onCancel != null
            ) {

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.error
                    )
                ) {
                    Text(
                        text = "Cancel Reservation",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Displays a reservation detail alongside an icon.
 *
 * @param icon Icon displayed next to the text.
 * @param text Reservation detail to display.
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
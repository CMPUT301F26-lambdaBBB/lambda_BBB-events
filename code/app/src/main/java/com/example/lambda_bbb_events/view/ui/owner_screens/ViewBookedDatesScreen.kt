package com.example.lambda_bbb_events.view.ui.owner_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.calendar.CampsiteAvailabilityCalendar
import com.example.lambda_bbb_events.view.ui.components.rating.RatingSummary
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import java.time.YearMonth
import com.example.lambda_bbb_events.view.ui.components.common.CampsiteThumbnail
import androidx.compose.ui.draw.clip
import com.example.lambda_bbb_events.controller.ReservationFilter
import com.example.lambda_bbb_events.model.reservation.Reservation
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.view.ui.components.reservation.BookedEntrantCard

/*
 * Allows users to select campsite reservation dates
 * and guest counts before registration.
 *
 * TODO: Integrate the campsite's availability schedule
 * into calendar selection and reservation validation.
 */

/**
 * Displays campsite information and a form for selecting
 * reservation dates and guest count.
 *
 * Navigates to the registration status screen when the
 * entered dates and guest count pass validation.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves campsite information.
 * @param campsiteId The ID of the selected campsite.
 */
@Composable
fun ViewBookedDatesScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)

    var allReservations by remember(campsiteId) { mutableStateOf<List<Reservation>>(emptyList()) }
    var upcomingReservations by remember(campsiteId) { mutableStateOf<List<Reservation>>(emptyList()) }
    var entrants by remember(campsiteId) { mutableStateOf<Map<String, User>>(emptyMap()) }
    var isLoading by remember(campsiteId) { mutableStateOf(true) }

    LaunchedEffect(campsiteId) {
        val loadedAll = controllers.reservations.getReservationsForCampsite(
            campsiteId,
            ReservationFilter.ALL
        )
        val loadedUpcoming = controllers.reservations.getReservationsForCampsite(
            campsiteId,
            ReservationFilter.UPCOMING
        )

        val loadedEntrants = mutableMapOf<String, User>()
        for (reservation in loadedUpcoming) {
            if (!loadedEntrants.containsKey(reservation.entrantId)) {
                val entrant = try {
                    controllers.users.getUser(reservation.entrantId)
                } catch (_: Exception) { // if fails the card shows "Unknown guest"
                    null
                }
                if (entrant != null) {
                    loadedEntrants[reservation.entrantId] = entrant
                }
            }
        }

        allReservations = loadedAll
        upcomingReservations = loadedUpcoming
        entrants = loadedEntrants
        isLoading = false
    }



    if (campsite == null) {
        Text(
            text = "Loading campsite...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        return
    }

    val fullyBookedDates = controllers.reservations.getFullyBookedDates(
        allReservations,
        campsite.maxOccupancy
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Booked Dates",
            navController = navController,
            showBack = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Campsite image
            CampsiteThumbnail(
                campsite = campsite,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = campsite.campsiteName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                RatingSummary(
                    campsite.rating,
                    campsite.ratingCount
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = campsite.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Max Occupancy",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "Max Occupancy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${campsite.maxOccupancy} people",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )

        Text(
            text = "Booked Dates",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        CampsiteAvailabilityCalendar(
            initialMonth = YearMonth.now(),
            unavailableDates = fullyBookedDates,
            selectable = false,
        )

        HorizontalDivider()

        Text(
            text = "Entrants who have booked:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else if (upcomingReservations.isEmpty()) {
            Text(
                text = "Nobody has booked yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            for (reservation in upcomingReservations) {
                val entrant = entrants[reservation.entrantId]

                BookedEntrantCard(
                    name = entrant?.name ?: "Unknown guest",
                    profilePictureImageId = entrant?.profilePictureImageId ?: "",
                    reservation = reservation
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

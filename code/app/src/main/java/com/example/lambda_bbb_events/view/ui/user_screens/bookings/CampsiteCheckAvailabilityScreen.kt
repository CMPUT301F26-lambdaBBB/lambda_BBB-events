
package com.example.lambda_bbb_events.view.ui.user_screens.bookings

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.calendar.CampsiteAvailabilityCalendar
import com.example.lambda_bbb_events.view.ui.components.calendar.DatePickerField
import com.example.lambda_bbb_events.view.ui.components.rating.RatingSummary
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import java.time.LocalDate
import java.time.YearMonth

/*
 * Allows users to select campsite reservation dates
 * and guest counts before registration.
 *
 * TODO: Integrate the campsite's availability schedule
 * into calendar selection and reservation validation.
 * TODO: Replace the campsite image placeholder.
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
fun CampsiteCheckAvailabilityScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)

    if (campsite == null) {
        Text(
            text = "Loading campsite...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        return
    }

    var fromDate by remember(campsiteId) {
        mutableStateOf("")
    }

    var toDate by remember(campsiteId) {
        mutableStateOf("")
    }

    val startDate = runCatching {
        LocalDate.parse(fromDate)
    }.getOrNull()

    val endDate = runCatching {
        LocalDate.parse(toDate)
    }.getOrNull()

    var numberOfPeople by remember(campsiteId) {
        mutableStateOf("")
    }

    val context = LocalContext.current
    val validPeople = numberOfPeople.toIntOrNull()

    val canConfirm = startDate != null &&
            endDate != null &&
            !endDate.isBefore(startDate) &&
            validPeople != null &&
            validPeople in 1..campsite.maxOccupancy

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Check Availability",
            navController = navController,
            showBack = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Campsite image placeholder
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Image",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

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
            text = "Select Availability",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        val unavailableDates = emptySet<LocalDate>()

        CampsiteAvailabilityCalendar(
            initialMonth = YearMonth.now(),
            unavailableDates = unavailableDates,
            startDate = startDate,
            endDate = endDate,
            onDateRangeChange = { start, end ->
                fromDate = start?.toString() ?: ""
                toDate = end?.toString() ?: ""
            }
        )

        Text(
            text = "From *",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        DatePickerField(
            value = fromDate,
            onValueChange = { fromDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "To *",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        DatePickerField(
            value = toDate,
            onValueChange = { toDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        FormTextFieldWithTitle(
            title = "Number of People:",
            value = numberOfPeople,
            onValueChange = { newValue ->
                val maxDigits = campsite.maxOccupancy.toString().length

                if (
                    newValue.all { it.isDigit() } &&
                    newValue.length <= maxDigits
                ) {
                    numberOfPeople = newValue
                }
            },
            placeholder = "Enter number of people",
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Max Occupancy: ${campsite.maxOccupancy} people",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )

        Button(
            onClick = {
                val start = startDate
                val end = endDate
                val people = validPeople

                if (start != null && end != null && people != null) {
                    Toast.makeText(
                        context,
                        "Selected $start to $end for $people people",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate(
                        "registrationStatus/$campsiteId/$start/$end/$people"
                    )
                }
            },
            enabled = canConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "Confirm",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

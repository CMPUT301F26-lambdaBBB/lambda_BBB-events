
package com.example.lambda_bbb_events.view.ui.components.filter

import RangePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.CampsiteController
import com.example.lambda_bbb_events.view.ui.components.rating.PickRatingStars
import com.example.lambda_bbb_events.view.ui.components.calendar.DatePickerField
import com.example.lambda_bbb_events.view.ui.components.common.FormTextField
import com.google.firebase.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/*
 * Provides filtering campsites by date, location,
 * guest capacity, and minimum rating.
 */

/**
 * Displays a bottom sheet for filtering campsites.
 *
 * Applies selected filters and displays matching results.
 * Invalid date ranges prevent submission.
 *
 * @param campsiteController Manages campsite filters.
 * @param onDismiss Callback for closing the filter sheet.
 * @param onShowResults Callback triggered after applying filters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCampsiteFilter(
    campsiteController: CampsiteController,
    onDismiss: () -> Unit,
    onShowResults: () -> Unit
) {
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var guestRange by remember { mutableStateOf(0f..100f) }
    var rating by remember { mutableIntStateOf(0) }

    val startTimestamp = remember(fromDate) {
        parseFilterDate(fromDate)
    }

    val endTimestamp = remember(toDate) {
        parseFilterDate(toDate)
    }

    val datesValid = when {
        fromDate.isBlank() && toDate.isBlank() -> true

        startTimestamp != null && endTimestamp != null ->
            startTimestamp < endTimestamp

        else -> false
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Date range
            FilterHeader(
                title = "Date Range",
                onReset = {
                    fromDate = ""
                    toDate = ""
                },
                titleTag = "dateRangeLabel",
                resetTag = "dateResetButton"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "From",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    DatePickerField(
                        value = fromDate,
                        onValueChange = {
                            fromDate = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fromDateInput")
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    DatePickerField(
                        value = toDate,
                        onValueChange = {
                            toDate = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toDateInput")
                    )
                }
            }

            if (!datesValid) {
                Text(
                    text = "Please select a valid date range.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("dateRangeError")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Location
            FilterHeader(
                title = "Location",
                onReset = {
                    location = ""
                },
                titleTag = "locationLabel",
                resetTag = "locationResetButton"
            )

            FormTextField(
                value = location,
                onValueChange = {
                    location = it
                },
                placeholder = "Search",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("locationInput")
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Number of guests
            FilterHeader(
                title = "Number of Guests",
                onReset = {
                    guestRange = 0f..100f
                },
                titleTag = "guestsLabel",
                resetTag = "guestResetButton"
            )

            RangePicker(
                value = guestRange,
                onValueChange = {
                    guestRange = it
                },
                min = 0f,
                max = 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guestRangeInput")
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Rating
            FilterHeader(
                title = "Rating",
                onReset = {
                    rating = 0
                },
                titleTag = "ratingLabel",
                resetTag = "ratingResetButton"
            )

            PickRatingStars(
                rating = rating,
                onRatingChange = {
                    rating = it
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Apply filters
            Button(
                onClick = {
                    campsiteController.applyFilters(
                        startDate = startTimestamp,
                        endDate = endTimestamp,
                        location = location.trim(),
                        minGuests = guestRange.start.roundToInt(),
                        maxGuests = guestRange.endInclusive.roundToInt(),
                        minRating = rating
                            .takeIf { it > 0 }
                            ?.toDouble()
                    )

                    onShowResults()
                },
                enabled = datesValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("showResultsButton"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.12f
                    ),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    )
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "Show Results",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Displays a filter section heading with a reset button.
 *
 * @param title Text displayed as the filter section heading.
 * @param onReset Callback invoked when the reset button is clicked.
 * @param titleTag Test tag used to identify the section heading.
 * @param resetTag Test tag used to identify the reset button.
 */
@Composable
private fun FilterHeader(
    title: String,
    onReset: () -> Unit,
    titleTag: String,
    resetTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .testTag(titleTag)
        )

        TextButton(
            onClick = onReset,
            modifier = Modifier.testTag(resetTag)
        ) {
            Text(
                text = "Reset",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Converts a date string into a Firebase timestamp.
 *
 * Parses the provided date using the yyyy-MM-dd format
 * with the UTC time zone. Lenient date parsing is disabled
 * to reject invalid calendar dates.
 *
 * @param value Date string to convert into a timestamp.
 * @return The parsed [Timestamp], or null if parsing fails
 * or the input is blank.
 */
private fun parseFilterDate(value: String): Timestamp? {
    if (value.isBlank()) return null

    val format = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }

    return try {
        format.parse(value.trim())?.let { date ->
            Timestamp(date)
        }
    } catch (e: ParseException) {
        null
    }
}

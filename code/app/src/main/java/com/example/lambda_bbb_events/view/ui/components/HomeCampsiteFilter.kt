package com.example.lambda_bbb_events.view.ui.components

import RangePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.controller.CampsiteController
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/*
 * Provides filtering campsites by date, location, guest capacity, and minimum rating.
 *
 * content might not scrollable on smaller screens.
 *
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
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filters",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            )

            HorizontalDivider(color = Color.LightGray)

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
                Column(modifier = Modifier.weight(1f)) {
                    Text("From")

                    DatePickerField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        modifier = Modifier.fillMaxWidth().testTag("fromDateInput")
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "To")

                    DatePickerField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        modifier = Modifier.fillMaxWidth().testTag("toDateInput")
                    )
                }
            }

            if (!datesValid) {
                Text(
                    text = "Please select a valid date range.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("dateRangeError")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray)

            FilterHeader(
                title = "Location",
                onReset = { location = "" },
                titleTag = "locationLabel",
                resetTag = "locationResetButton"
            )

            FormTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = "Search",
                modifier = Modifier.fillMaxWidth().testTag("locationInput")
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray)

            FilterHeader(
                title = "Number of Guests",
                onReset = { guestRange = 0f..100f },
                titleTag = "guestsLabel",
                resetTag = "guestResetButton"
            )

            RangePicker(
                value = guestRange,
                onValueChange = { guestRange = it },
                min = 0f,
                max = 100f,
                modifier = Modifier.fillMaxWidth().testTag("guestRangeInput")
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray)

            FilterHeader(
                title = "Rating",
                onReset = { rating = 0 },
                titleTag = "ratingLabel",
                resetTag = "ratingResetButton"
            )

            PickRatingStars(
                rating = rating,
                onRatingChange = { rating = it }
            )


            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(24.dp))

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
                    containerColor = Color.LightGray,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("Show Results")
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .testTag(titleTag)
        )

        TextButton(
            onClick = onReset,
            modifier = Modifier.testTag(resetTag)
        ) {
            Text("Reset")
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
    } catch (e: java.text.ParseException) {
        null
    }
}

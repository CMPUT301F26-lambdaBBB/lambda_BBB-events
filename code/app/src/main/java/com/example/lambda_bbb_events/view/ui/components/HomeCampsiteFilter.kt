package com.example.lambda_bbb_events.view.ui.components

import RangePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.controller.CampsiteController
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

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

    ModalBottomSheet(
        onDismissRequest = onDismiss
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
                }
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("To")

                    DatePickerField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (!datesValid) {
                Text(
                    text = "Please select a valid date range.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray)

            FilterHeader(
                title = "Location",
                onReset = { location = "" }
            )

            FormTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = "Search",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray)

            FilterHeader(
                title = "Number of Guests",
                onReset = { guestRange = 0f..100f }
            )

            RangePicker(
                value = guestRange,
                onValueChange = { guestRange = it },
                min = 0f,
                max = 100f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray)

            FilterHeader(
                title = "Rating",
                onReset = { rating = 0 }
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
                    .height(52.dp),
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

@Composable
private fun FilterHeader(
    title: String,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onReset) {
            Text("Reset")
        }
    }
}

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

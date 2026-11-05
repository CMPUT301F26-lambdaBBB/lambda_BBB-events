package com.example.lambda_bbb_events.view.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.CampsiteAvailabilityCalendar
import com.example.lambda_bbb_events.view.ui.components.RatingSummary
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SubTitle
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.ui.platform.LocalContext
import com.example.lambda_bbb_events.view.ui.components.DatePickerField
import com.example.lambda_bbb_events.view.ui.components.FormTextFieldWithTitle

@Composable
fun CampsiteCheckAvailabilityScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)
    if (campsite == null) {
        Text("Loading campsite...")
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
            !endDate!!.isBefore(startDate) &&
            validPeople != null &&
            validPeople in 1..campsite.maxOccupancy

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        color = Color.LightGray,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Image",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = campsite.campsiteName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
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
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = campsite.address,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
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
                        modifier = Modifier.size(16.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "Max Occupancy",
                            fontSize = 10.sp,
                            lineHeight = 11.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "${campsite.maxOccupancy} people",
                            fontSize = 12.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            color = Color.LightGray,
            thickness = 1.dp
        )

        SubTitle("Select Availability")

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

        Text("From *", fontSize = 14.sp)

        DatePickerField(
            value = fromDate,
            onValueChange = { fromDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        Text("To *", fontSize = 14.sp)

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
                if (newValue.all { it.isDigit() } && newValue.length <= maxDigits
                ) {
                    numberOfPeople = newValue
                }
            },
            placeholder = "Enter number of people",
            modifier = Modifier.fillMaxWidth()
        )


        Text(
            text = "Max Occupancy: ${campsite.maxOccupancy} people",
            fontSize = 11.sp,
            color = Color.Gray,
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
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8529D1),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Confirm",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

package com.example.lambda_bbb_events.view.ui.owner_screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.calendar.DatePickerField
import com.example.lambda_bbb_events.view.ui.components.calendar.DateRangeCard
import com.example.lambda_bbb_events.view.ui.components.calendar.parseDateOrNull
import com.example.lambda_bbb_events.view.ui.components.campsite.VerticalCampsiteCard
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Screen where a campsite owner can create a lottery for their campsite.
 *
 * @param navController The navigation controller.
 * @param controllers The app's controllers.
 * @param campsiteId The ID of the campsite the lottery is for.
 */
@Composable
fun CreateLotteryScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)

    if (campsite == null) {
        Text("Campsite not found")
        return
    }

    val scope = rememberCoroutineScope()

    var registrationStart by remember { mutableStateOf("") }
    var registrationEnd by remember { mutableStateOf("") }
    var bookingStart by remember { mutableStateOf("") }
    var bookingEnd by remember { mutableStateOf("") }
    var numberOfWinners by remember { mutableStateOf("1") }
    var waitlistCapacity by remember { mutableStateOf("10") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle("Create lottery", navController, showBack = true)

        // the form scrolls, the Cancel and Create buttons stay at the bottom
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VerticalCampsiteCard(
                campsite,
                onClick = {}
            )

            HorizontalDivider(Modifier.padding(vertical = 10.dp))

            DateRangeCard(
                title = "Registration Period",
                startDate = registrationStart,
                endDate = registrationEnd,
                onStartDateChange = { registrationStart = it },
                onEndDateChange = { registrationEnd = it }
            )

            DateRangeCard(
                title = "Booking Period",
                startDate = bookingStart,
                endDate = bookingEnd,
                onStartDateChange = { bookingStart = it },
                onEndDateChange = { bookingEnd = it }
            )

            FormTextFieldWithTitle(
                title = "Number of winners",
                value = numberOfWinners,
                onValueChange = { numberOfWinners = it },
                placeholder = "How many entrants are drawn"
            )

            FormTextFieldWithTitle(
                title = "Waitlist capacity",
                value = waitlistCapacity,
                onValueChange = { waitlistCapacity = it },
                placeholder = "Max entrants allowed to join"
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        scope.launch {
                            // parseDateOrNull gives null for an empty or half-typed date,
                            // and createLottery answers that with an error message
                            val error = controllers.lotteries.createLottery(
                                ownerId = controllers.users.user.deviceId,
                                campsiteId = campsiteId,
                                registrationStart = parseDateOrNull(registrationStart),
                                registrationEnd = parseDateOrNull(registrationEnd),
                                bookingStart = parseDateOrNull(bookingStart),
                                bookingEnd = parseDateOrNull(bookingEnd),
                                numberOfWinnersString = numberOfWinners,
                                waitlistCapacityString = waitlistCapacity
                            )

                            if (error == null) {
                                navController.popBackStack()
                            } else {
                                errorMessage = error
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Create lottery")
                }
            }
        }
    }
}
package com.example.lambda_bbb_events.view.ui.owner_screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.view.ui.components.calendar.DateRangeCard
import com.example.lambda_bbb_events.view.ui.components.calendar.parseDateOrNull
import com.example.lambda_bbb_events.view.ui.components.campsite.VerticalCampsiteCard
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * Screen where a campsite owner can edit a lottery for their campsite.
 *
 * @param navController The navigation controller.
 * @param controllers The app's controllers.
 * @param lotteryId The ID of the lottery.
 */
@Composable
fun EditLotteryScreen(
    navController: NavController,
    controllers: AppControllers,
    lotteryId: String
) {
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(lotteryId) {
        isLoading = true
        lottery = controllers.lotteries.getLottery(lotteryId)
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val currentLottery = lottery
    if (currentLottery == null) {
        Text("Lottery not found")
        return
    }

    val currentCampsiteId = currentLottery.campsiteIds.firstOrNull()
    if (currentCampsiteId == null) {
        Text("Campsite ID not found")
        return
    }

    val currentCampsite = controllers.campsites.getCampsite(currentCampsiteId)
    if (currentCampsite == null) {
        Text("Campsite not found")
        return
    }

    var registrationStart by remember { mutableStateOf(
        dateToLocalDate(currentLottery.registrationStart).toString()) }
    var registrationEnd by remember { mutableStateOf(
        dateToLocalDate(currentLottery.registrationEnd).toString()) }
    var bookingStart by remember { mutableStateOf(
        dateToLocalDate(currentLottery.bookingStart).toString()) }
    var bookingEnd by remember { mutableStateOf(
        dateToLocalDate(currentLottery.bookingEnd).toString()) }
    var numberOfWinners by remember { mutableStateOf(currentLottery.numberOfWinners.toString()) }
    var waitlistCapacity by remember { mutableStateOf(currentLottery.waitlistCapacity.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle("Edit Lottery", navController, showBack = true)

        // the form scrolls, the Cancel and Create buttons stay at the bottom
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VerticalCampsiteCard(
                currentCampsite,
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
                            val error = controllers.lotteries.updateLottery(
                                lotteryId = currentLottery.lotteryId,
                                ownerId = controllers.users.user.deviceId,
                                campsiteId = currentCampsiteId,
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
                    Text("Save changes")
                }
            }
        }
    }
}

private fun dateToLocalDate(date: Date): LocalDate {
    return date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}
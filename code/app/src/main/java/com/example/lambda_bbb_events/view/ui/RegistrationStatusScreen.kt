package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.*
import com.example.lambda_bbb_events.view.ui.theme.Pink40
import java.time.LocalDate

@Composable
fun RegistrationStatusScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String,
    fromDate: LocalDate,
    endDate: LocalDate,
    numPeople: Int
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)

    val lottery = controllers.lotteries
        .getOpenLotteryForCampsite(campsiteId)

    val entrantId = controllers.users.deviceId

    var message by remember(campsiteId, entrantId) {
        mutableStateOf("")
    }

    var isJoining by remember(campsiteId, entrantId) {
        mutableStateOf(false)
    }

    var hasJoined by remember(campsiteId, entrantId) {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Registration Status",
            navController = navController,
            showBack = true
        )

        RegistrationStatusBox(
            registrationStatus = "Lottery Required",
            message = "Join Lottery to win the Ticket to the campsite",
            boxColor = Pink40
        )

        RegistrationInformationBox(
            campsiteName = campsite?.campsiteName ?: "Unknown Campsite",
            fromDate = fromDate,
            endDate = endDate,
            numPeople = numPeople
        )


        Button(
            onClick = {
                if (lottery != null) {
                    isJoining = true
                    message = ""

                    controllers.lotteries.joinLotteryEntry(
                        lotteryId = lottery.lotteryId,
                        entrantId = controllers.users.deviceId,
                        numberOfGuests = numPeople
                    ) { success, resultMessage ->

                        isJoining = false

                        if (success) {
                            navController.navigate(
                                "lottery_entered/$campsiteId/$fromDate/$endDate/$numPeople"
                            ) {
                                popUpTo(
                                    "registrationStatus/{campsiteId}/{fromDate}/{endDate}/{numPeople}"
                                ) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } else {
                            message = resultMessage
                        }
                    }
                }
            },
            enabled = lottery != null &&
                    lottery.isRegistrationOpen() &&
                    !lottery.isFull() &&
                    !isJoining &&
                    !hasJoined
        ) {
            Text(
                if (isJoining) "Joining..."
                else "Join Lottery"
            )
        }

        if (lottery == null) {
            Text("No open lottery available")
        } else if (lottery.isFull()) {
            Text("Lottery waitlist is full")
        }

        if (message.isNotEmpty()) {
            Text(message)
        }
    }
}
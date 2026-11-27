package com.example.lambda_bbb_events.view.ui.user_screens.bookings.lottery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.lottery.RegistrationStatusBox
import com.example.lambda_bbb_events.view.ui.components.navigation.rememberBackAction
import com.example.lambda_bbb_events.view.ui.owner_screens.VerticalCampsiteCardWithCheckbox
import com.example.lambda_bbb_events.view.ui.theme.bookingStatusColors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Displays the registration status of a waitlist entry.
 *
 * Allows entrants to review lottery results, leave the waitlist,
 * confirm a reservation, or decline a lottery win.
 *
 * After a reservation is successfully confirmed, the entrant
 * navigates directly to the booking receipt screen.
 *
 * @param navController The controller used for navigation.
 * @param controllers Shared application controllers.
 * @param entryId The unique identifier of the waitlist entry.
 * @param onConfirmReservation Callback that saves the reservation
 * and updates the waitlist status. Throws an exception on failure.
 */
@Composable
fun RegistrationStatusScreen(
    navController: NavController,
    controllers: AppControllers,
    entryId: String,
    onConfirmReservation: (
    suspend (String, String, LocalDate, LocalDate, Int) -> Unit
    )? = null
) {
    val goBack = rememberBackAction(navController)
    val scope = rememberCoroutineScope()

    val entry = controllers.waitlistEntries.getEntryById(entryId)

    val statusColors = MaterialTheme.bookingStatusColors
    val colorScheme = MaterialTheme.colorScheme

    var campsiteId by remember {
        mutableStateOf<String?>(null)
    }

    var lotteryCampsiteIds by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var fromDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    var endDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    var showLeaveDialog by remember {
        mutableStateOf(false)
    }

    var showDeclineDialog by remember {
        mutableStateOf(false)
    }

    var isLeaving by remember {
        mutableStateOf(false)
    }

    var isDeclining by remember {
        mutableStateOf(false)
    }

    var isConfirming by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // Retrieve the campsite and booking dates from the lottery.
    LaunchedEffect(entry?.lotteryId) {

        lotteryCampsiteIds = emptyList()
        campsiteId = null
        fromDate = null
        endDate = null

        if (entry != null) {
            try {
                val lottery = controllers.lotteries
                    .getLottery(entry.lotteryId)

                if (lottery != null) {

                    lotteryCampsiteIds = lottery.campsiteIds
                    campsiteId = lottery.campsiteIds.singleOrNull()

                    fromDate = lottery.bookingStart
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    endDate = lottery.bookingEnd
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                } else {
                    errorMessage = "Lottery not found."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                errorMessage =
                    e.message ?: "Failed to load lottery information."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        ScreenTitle(
            title = "Registration Status",
            navController = navController,
            showBack = true
        )

        if (entry == null) {

            Text(
                text = "Loading registration information...",
                style = MaterialTheme.typography.bodyMedium
            )

        } else {

            val registrationStatus = when (entry.status) {
                WaitlistStatus.IN_WAITLIST -> "In Waitlist"
                WaitlistStatus.WON -> "Won the Lottery"
                WaitlistStatus.LOST -> "Lost the Lottery"
                WaitlistStatus.ACCEPTED -> "Reservation Accepted"
                WaitlistStatus.DECLINED -> "Reservation Declined"
            }

            val registrationMessage = when (entry.status) {

                WaitlistStatus.IN_WAITLIST ->
                    "You have successfully joined the lottery. " +
                            "Waiting for results."

                WaitlistStatus.WON ->
                    "Congratulations! You have won the lottery. " +
                            "Please confirm or decline your reservation."

                WaitlistStatus.LOST ->
                    "Unfortunately, you were not selected in the lottery."

                WaitlistStatus.ACCEPTED ->
                    "You have accepted your lottery win."

                WaitlistStatus.DECLINED ->
                    "You have declined your lottery win."
            }

            val statusColor = when (entry.status) {

                WaitlistStatus.IN_WAITLIST ->
                    statusColors.pendingBackground

                WaitlistStatus.WON,
                WaitlistStatus.ACCEPTED ->
                    colorScheme.primaryContainer

                WaitlistStatus.LOST,
                WaitlistStatus.DECLINED ->
                    colorScheme.errorContainer
            }

            RegistrationStatusBox(
                registrationStatus = registrationStatus,
                message = registrationMessage,
                boxColor = statusColor
            )

            val campsiteName = campsiteId?.let {
                controllers.campsites.campsites[it]?.campsiteName
            }

            // Display registration and booking information.
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "You Selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = campsiteName
                            ?: "Campsite selection required",
                        style = MaterialTheme.typography.titleMedium
                    )

                    HorizontalDivider()

                    Text(
                        text = "From: ${fromDate ?: "Unavailable"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "To: ${endDate ?: "Unavailable"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Number of Guests: ${entry.numberOfGuests}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Allow entrants to leave the lottery waitlist.
            if (entry.status == WaitlistStatus.IN_WAITLIST) {

                OutlinedButton(
                    onClick = {
                        showLeaveDialog = true
                    },
                    enabled = !isLeaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.error
                    )
                ) {
                    Text(
                        text = if (isLeaving) {
                            "Leaving..."
                        } else {
                            "Leave Waitlist"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // if multiple campsite lottery then user must choose one campsite only
            if (entry.status == WaitlistStatus.WON && lotteryCampsiteIds.size > 1) {
                Text(
                    text = "Choose a Campsite",
                    style = MaterialTheme.typography.titleMedium
                )

                lotteryCampsiteIds
                    .mapNotNull { controllers.campsites.campsites[it] }
                    .forEach { campsite ->
                        VerticalCampsiteCardWithCheckbox(
                            campsite = campsite,
                            isSelected = campsite.campsiteId == campsiteId,
                            onClick = {
                                campsiteId =
                                    if (campsiteId == campsite.campsiteId) null
                                    else campsite.campsiteId
                            }
                        )
                    }
            }

            // Allow lottery winners to confirm or decline.
            if (entry.status == WaitlistStatus.WON) {

                Button(
                    onClick = confirmClick@{

                        val selectedCampsiteId =
                            campsiteId ?: return@confirmClick

                        val selectedFromDate =
                            fromDate ?: return@confirmClick

                        val selectedEndDate =
                            endDate ?: return@confirmClick

                        val confirmReservation =
                            onConfirmReservation

                        if (confirmReservation == null) {
                            errorMessage =
                                "Reservation confirmation is not connected yet."
                            return@confirmClick
                        }

                        if (isConfirming) return@confirmClick

                        isConfirming = true
                        errorMessage = null

                        scope.launch {
                            try {

                                // Save the confirmed reservation.
                                confirmReservation(
                                    entryId,
                                    selectedCampsiteId,
                                    selectedFromDate,
                                    selectedEndDate,
                                    entry.numberOfGuests
                                )

                                // Navigate directly to Booking Receipt.
                                navController.navigate("booking_receipt/lottery_$entryId") {
                                    popUpTo("registrationStatus/{entryId}") {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }

                            } catch (e: CancellationException) {
                                throw e

                            } catch (e: Exception) {
                                errorMessage = e.message
                                    ?: "Failed to confirm reservation."

                            } finally {
                                isConfirming = false
                            }
                        }
                    },
                    enabled = !isConfirming &&
                            !isDeclining &&
                            campsiteId != null &&
                            fromDate != null &&
                            endDate != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isConfirming) {
                            "Confirming..."
                        } else {
                            "Confirm Reservation"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = {
                        showDeclineDialog = true
                    },
                    enabled = !isDeclining && !isConfirming,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.error
                    )
                ) {
                    Text(
                        text = if (isDeclining) {
                            "Declining..."
                        } else {
                            "Decline Reservation"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Leave waitlist confirmation dialog.
    if (showLeaveDialog && entry != null) {

        AlertDialog(
            onDismissRequest = {
                if (!isLeaving) {
                    showLeaveDialog = false
                }
            },

            title = {
                Text("Leave Waitlist?")
            },

            text = {
                Text(
                    "Are you sure you want to leave this lottery? " +
                            "Your registration will be removed."
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !isLeaving,
                    onClick = {
                        isLeaving = true
                        errorMessage = null

                        scope.launch {
                            try {

                                controllers.waitlistEntries
                                    .leaveWaitlist(entry)

                                showLeaveDialog = false
                                goBack()

                            } catch (e: CancellationException) {
                                throw e

                            } catch (e: Exception) {
                                errorMessage =
                                    e.message ?: "Failed to leave waitlist."

                            } finally {
                                showLeaveDialog = false
                                isLeaving = false
                            }
                        }
                    }
                ) {
                    Text(
                        text = if (isLeaving) "Leaving..." else "Leave",
                        color = colorScheme.error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !isLeaving,
                    onClick = {
                        showLeaveDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Decline reservation confirmation dialog.
    if (showDeclineDialog && entry != null) {

        AlertDialog(
            onDismissRequest = {
                if (!isDeclining) {
                    showDeclineDialog = false
                }
            },

            title = {
                Text("Decline Reservation?")
            },

            text = {
                Text(
                    "Are you sure you want to decline your lottery win?"
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !isDeclining,
                    onClick = {
                        isDeclining = true
                        errorMessage = null

                        scope.launch {
                            try {

                                controllers.waitlistEntries
                                    .declineWin(entry)

                            } catch (e: CancellationException) {
                                throw e

                            } catch (e: Exception) {
                                errorMessage = e.message
                                    ?: "Failed to decline reservation."

                            } finally {
                                showDeclineDialog = false
                                isDeclining = false
                            }
                        }
                    }
                ) {
                    Text(
                        text = if (isDeclining) {
                            "Declining..."
                        } else {
                            "Decline"
                        },
                        color = colorScheme.error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !isDeclining,
                    onClick = {
                        showDeclineDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
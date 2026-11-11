
package com.example.lambda_bbb_events.view.ui.user_screens.bookings.lottery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.WaitlistStatus
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.lottery.RegistrationStatusBox
import com.example.lambda_bbb_events.view.ui.theme.bookingStatusColors
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Displays the registration status of a waitlist entry.
 *
 * Retrieves the waitlist entry using its unique ID and displays
 * its current status, corresponding message, and registration details.
 * The displayed information reflects the entry's status in Firestore.
 *
 * @param navController The [NavController] used for navigation.
 * @param controllers The [AppControllers] used to access waitlist entry information.
 * @param entryId The unique ID of the waitlist entry.
 */
@Composable
fun RegistrationStatusScreen(
    navController: NavController,
    controllers: AppControllers,
    entryId: String
) {
    val entry = controllers.waitlistEntries.getEntryById(entryId)
    val statusColors = MaterialTheme.bookingStatusColors
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    var showLeaveDialog by remember { mutableStateOf(false) }
    var isLeaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 16.dp),
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
                WaitlistStatus.WON -> "Lottery Won"
                WaitlistStatus.LOST -> "Lottery Lost"
                WaitlistStatus.ACCEPTED -> "Reservation Accepted"
                WaitlistStatus.DECLINED -> "Reservation Declined"
            }

            val registrationMessage = when (entry.status) {
                WaitlistStatus.IN_WAITLIST ->
                    "You have successfully joined the lottery. Waiting for results."

                WaitlistStatus.WON ->
                    "Congratulations! You have won the lottery. Please accept or decline."

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

                WaitlistStatus.WON, WaitlistStatus.ACCEPTED ->
                    colorScheme.primaryContainer

                WaitlistStatus.LOST, WaitlistStatus.DECLINED ->
                    colorScheme.errorContainer
            }

            RegistrationStatusBox(
                registrationStatus = registrationStatus,
                message = registrationMessage,
                boxColor = statusColor
            )

            Text(
                text = "Number of Guests: ${entry.numberOfGuests}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (entry?.status == WaitlistStatus.IN_WAITLIST) {
            OutlinedButton(
                onClick = {
                    showLeaveDialog = true
                },
                enabled = !isLeaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = if (isLeaving) "Leaving..." else "Leave Waitlist",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

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
                                controllers.waitlistEntries.leaveWaitlist(entry)

                                showLeaveDialog = false
                                navController.popBackStack()
                            } catch (e: Exception) {
                                errorMessage =
                                    e.message ?: "Failed to leave waitlist."
                                showLeaveDialog = false
                            } finally {
                                isLeaving = false
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Leave",
                        color = MaterialTheme.colorScheme.error
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
}
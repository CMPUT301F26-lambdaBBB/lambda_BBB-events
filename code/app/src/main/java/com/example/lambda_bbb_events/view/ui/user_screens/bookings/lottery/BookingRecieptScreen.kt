package com.example.lambda_bbb_events.view.ui.user_screens.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar

/**
 * Displays the receipt of a confirmed campsite reservation.
 *
 * Shows the booking status, campsite name, reservation dates,
 * number of guests, and reservation summary.
 *
 * Allows the entrant to cancel their confirmed reservation.
 *
 * @param navController Handles screen navigation.
 * @param campsiteName Name of the reserved campsite.
 * @param fromDate Starting date of the reservation.
 * @param endDate Ending date of the reservation.
 * @param numberOfGuests Number of guests included in the reservation.
 * @param onCancelReservation Callback for cancelling the reservation.
 * @param isCancelling Whether a cancellation is currently in progress.
 * @param errorMessage Error message displayed if cancellation fails.
 */
@Composable
fun BookingReceiptScreen(
    navController: NavController,
    campsiteName: String,
    fromDate: String,
    endDate: String,
    numberOfGuests: Int,
    onCancelReservation: (() -> Unit)? = null,
    isCancelling: Boolean = false,
    errorMessage: String? = null
) {
    var showCancelDialog by remember {
        mutableStateOf(false)
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .testTag("screen_booking_receipt"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ScreenTitle(
                title = "Booking Receipt",
                navController = navController,
                showNotifications = true,
                showBack = true
            )

            BookingConfirmedCard()

            BookingInformationCard(
                campsiteName = campsiteName,
                fromDate = fromDate,
                endDate = endDate,
                numberOfGuests = numberOfGuests
            )

            BookingReceiptCard(
                campsiteName = campsiteName,
                fromDate = fromDate,
                endDate = endDate,
                numberOfGuests = numberOfGuests
            )

            BookingSummaryCard()

            // Allow the entrant to cancel their reservation.
            if (onCancelReservation != null) {

                OutlinedButton(
                    onClick = {
                        showCancelDialog = true
                    },
                    enabled = !isCancelling,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cancel_reservation_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.error
                    )
                ) {
                    Text(
                        text = if (isCancelling) {
                            "Cancelling..."
                        } else {
                            "Cancel Reservation"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Display errors returned by the cancellation operation.
            errorMessage?.let {
                Text(
                    text = it,
                    color = colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(
                        "booking_receipt_error"
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Display the cancellation confirmation dialog.
    if (showCancelDialog && onCancelReservation != null) {

        AlertDialog(
            onDismissRequest = {
                if (!isCancelling) {
                    showCancelDialog = false
                }
            },

            title = {
                Text(
                    text = "Cancel Reservation?",
                    style = MaterialTheme.typography.headlineMedium
                )
            },

            text = {
                Text(
                    text = "Are you sure you want to cancel " +
                            "your campsite reservation?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = {
                        showCancelDialog = false
                        onCancelReservation.invoke()
                    },
                    modifier = Modifier.testTag(
                        "confirm_cancel_reservation"
                    )
                ) {
                    Text(
                        text = "Cancel Reservation",
                        color = colorScheme.error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = {
                        showCancelDialog = false
                    },
                    modifier = Modifier.testTag(
                        "keep_reservation"
                    )
                ) {
                    Text("Keep Reservation")
                }
            }
        )
    }
}

/**
 * Displays the confirmed booking status.
 */
@Composable
private fun BookingConfirmedCard() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_confirmed_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Text(
                text = "Booking Status",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Booking Confirmed",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Your campsite reservation is confirmed.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Displays the campsite and reservation information.
 *
 * @param campsiteName Name of the campsite.
 * @param fromDate Starting date of the reservation.
 * @param endDate Ending date of the reservation.
 * @param numberOfGuests Number of guests.
 */
@Composable
private fun BookingInformationCard(
    campsiteName: String,
    fromDate: String,
    endDate: String,
    numberOfGuests: Int
) {

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_information_card"),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "You Selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = campsiteName,
                style = MaterialTheme.typography.titleMedium
            )

            HorizontalDivider()

            Text(
                text = "From: $fromDate",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "To: $endDate",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "$numberOfGuests ${
                    if (numberOfGuests == 1) "person" else "people"
                }",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Displays a summary receipt for the confirmed reservation.
 *
 * @param campsiteName Name of the campsite.
 * @param fromDate Starting date of the reservation.
 * @param endDate Ending date of the reservation.
 * @param numberOfGuests Number of guests.
 */
@Composable
private fun BookingReceiptCard(
    campsiteName: String,
    fromDate: String,
    endDate: String,
    numberOfGuests: Int
) {

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_receipt_card"),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Receipt",
                style = MaterialTheme.typography.titleMedium
            )

            HorizontalDivider()

            ReceiptRow(
                label = "Campsite",
                value = campsiteName
            )

            ReceiptRow(
                label = "Check-in",
                value = fromDate
            )

            ReceiptRow(
                label = "Check-out",
                value = endDate
            )

            ReceiptRow(
                label = "Party Size",
                value = "$numberOfGuests ${
                    if (numberOfGuests == 1) "person" else "people"
                }"
            )

            HorizontalDivider()

            ReceiptRow(
                label = "Status",
                value = "Confirmed"
            )
        }
    }
}

/**
 * Displays a labelled value within the booking receipt.
 *
 * @param label Description of the receipt information.
 * @param value Value associated with the label.
 */
@Composable
private fun ReceiptRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.65f)
        )
    }
}

/**
 * Displays a summary of the confirmed reservation.
 */
@Composable
private fun BookingSummaryCard() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_summary_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "Booking Summary",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Your campsite reservation is confirmed. " +
                        "You can view and manage this reservation " +
                        "from your Active Bookings.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

package com.example.lambda_bbb_events.view.ui.user_screens.bookings.lottery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.lottery.RegistrationInformationBox
import com.example.lambda_bbb_events.view.ui.components.lottery.RegistrationStatusBox
import com.example.lambda_bbb_events.view.ui.theme.bookingStatusColors
import java.time.LocalDate

/**
 * Displays the lottery availability for a selected campsite.
 *
 * Shows the selected reservation details and the current
 * lottery registration status. Allows users to join the
 * lottery if registration is open and the waitlist is not full.
 *
 * @param navController The [NavController] used for navigation.
 * @param controllers The [AppControllers] used to access campsite
 * and lottery information.
 * @param campsiteId The ID of the selected campsite.
 * @param fromDate The reservation start date.
 * @param endDate The reservation end date.
 * @param numPeople The number of guests included in the entry.
 */
@Composable
fun LotteryAvailabilityScreen(
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

    val statusColors = MaterialTheme.bookingStatusColors

    val canJoin = lottery != null &&
            lottery.isRegistrationOpen() &&
            !lottery.isFull()

    val registrationStatus = when {
        lottery == null -> "Lottery Unavailable"
        !lottery.isRegistrationOpen() -> "Registration Closed"
        lottery.isFull() -> "Lottery Full"
        else -> "Lottery Open"
    }

    val registrationMessage = when {
        lottery == null ->
            "No open lottery is available for this campsite."

        !lottery.isRegistrationOpen() ->
            "Registration for this lottery is closed."

        lottery.isFull() ->
            "The lottery waitlist has reached its capacity."

        else ->
            "Join the lottery for a chance to reserve this campsite."
    }

    val statusColor = if (canJoin) {
        statusColors.pendingBackground
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Lottery Availability",
            navController = navController,
            showBack = true
        )

        RegistrationStatusBox(
            registrationStatus = registrationStatus,
            message = registrationMessage,
            boxColor = statusColor
        )

        RegistrationInformationBox(
            campsiteName = campsite?.campsiteName ?: "Unknown Campsite",
            fromDate = fromDate,
            endDate = endDate,
            numPeople = numPeople
        )

        Button(
            onClick = {
                if (canJoin) {
                    navController.navigate(
                        "rule_confirm/$campsiteId/$fromDate/$endDate/$numPeople"
                    )
                }
            },
            enabled = canJoin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Join Lottery",
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (!canJoin) {
            Text(
                text = registrationMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

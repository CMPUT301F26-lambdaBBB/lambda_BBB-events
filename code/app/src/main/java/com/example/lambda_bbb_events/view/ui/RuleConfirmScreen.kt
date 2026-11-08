package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import java.time.LocalDate

/**
 * Displays campsite rules and lottery service policies for user confirmation.
 *
 * Users must scroll to the bottom of the rules/policy text before the
 * Confirm and Decline actions are enabled. Confirming joins the lottery and
 * navigates to the lottery entered screen, while declining returns to the
 * registration status screen.
 *
 * TODO: Display a message when lottery registration is closed. And the information to display as Policy
 *
 * @param navController The [NavController] used for navigation.
 * @param controllers The [AppControllers] used to access campsite and lottery info.
 * @param campsiteId The ID of the selected campsite.
 * @param fromDate The reservation start date.
 * @param endDate The reservation end date.
 * @param numPeople The number of guests.
 */
@Composable
fun RuleConfirmScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String,
    fromDate: LocalDate,
    endDate: LocalDate,
    numPeople: Int
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)
    val lottery = controllers.lotteries.getOpenLotteryForCampsite(campsiteId)

    var isJoining by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val scrolledToEnd by remember {
        derivedStateOf {
            scrollState.maxValue == 0 || scrollState.value >= (scrollState.maxValue - 20)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Rules & Policies",
            navController = navController,
            showBack = true
        )

        Text(
            text = campsite?.campsiteName ?: "Campsite Terms & Conditions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Campsite Additional Rules",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (!campsite?.rulesPolicies.isNullOrBlank()) {
                        campsite.rulesPolicies
                    } else {
                        "• Quiet hours are enforced between 10:00 PM and 7:00 AM.\n" +
                                "• All trash must be packed out or disposed of in designated bear-proof bins.\n" +
                                "• Campfires are permitted only in provided fire rings.\n" +
                                "• Pets must be kept on a leash at all times."
                    },
                    fontSize = 14.sp
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Lottery Service Policy",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "1. Waitlist Entry: Joining the lottery places your entry on the waitlist. " +
                            "Selection is conducted randomly and is not guaranteed.\n\n" +
                            "2. Non-Transferable: Lottery entries and winning reservations are strictly non-transferable.\n\n" +
                            "3. Confirmation Window: Selected winners must complete their booking within the " +
                            "designated timeframe or the spot will be offered to the next entrant.\n\n" +
                            "4. Cancellation: Campsite cancellation rules apply to all reservations made via lottery wins.\n\n" +
                            "5. Occupancy Limits: The registered guest count must not exceed the specified maximum capacity.",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (scrolledToEnd) {
                    Text(
                        text = "✓ You have reviewed all rules and policies.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (!scrolledToEnd) {
            Text(
                text = "Please scroll to the end of the rules to enable Confirm.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f),
                enabled = !isJoining
            ) {
                Text("Decline")
            }

            Button(
                onClick = {
                    if (lottery != null) {
                        isJoining = true
                        errorMessage = ""

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
                                errorMessage = resultMessage
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = scrolledToEnd && !isJoining && lottery != null
            ) {
                Text(if (isJoining) "Joining..." else "Confirm")
            }
        }
    }
}

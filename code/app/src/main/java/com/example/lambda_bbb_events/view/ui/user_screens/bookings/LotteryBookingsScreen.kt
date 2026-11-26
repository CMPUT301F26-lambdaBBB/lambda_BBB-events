package com.example.lambda_bbb_events.view.ui.user_screens.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.lottery.LotteryEntryCard
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import kotlinx.coroutines.CancellationException

/**
 * Displays the user's lottery bookings.
 *
 * Shows lottery entries that have not been converted
 * into confirmed reservations.
 *
 * Accepted lottery entries are excluded because their
 * reservations are displayed in Active or Past Bookings.
 *
 * Provides navigation to the registration status screen.
 *
 * @param navController Handles screen navigation.
 * @param controllers Provides access to application controllers.
 */
@Composable
fun LotteryBookingsScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val entrantId = controllers.users.user.deviceId

    var entries by remember {
        mutableStateOf<List<WaitlistEntry>>(emptyList())
    }

    var lotteries by remember {
        mutableStateOf<Map<String, Lottery>>(emptyMap())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var reloadKey by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(entrantId, reloadKey) {

        isLoading = true
        errorMessage = null

        try {

            // Retrieve lottery entries belonging to the entrant.
            val loadedEntries = controllers.waitlistEntries
                .getEntriesForEntrant(entrantId)
                .filter { entry ->
                    entry.status != WaitlistStatus.ACCEPTED
                }

            val loadedLotteries = mutableMapOf<String, Lottery>()

            // Retrieve the associated lotteries.
            for (entry in loadedEntries) {

                if (entry.lotteryId in loadedLotteries) {
                    continue
                }

                val lottery = controllers.lotteries
                    .getLottery(entry.lotteryId)

                if (lottery != null) {
                    loadedLotteries[lottery.lotteryId] = lottery
                }
            }

            entries = loadedEntries
            lotteries = loadedLotteries

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {

            errorMessage = e.message
                ?: "Failed to load lottery entries."

        } finally {

            isLoading = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .testTag("lottery_bookings")
        ) {

            ScreenTitle(
                title = "My Reservations",
                navController = navController
            )

            BookingsTypeBar(navController)

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "Lottery Entries: ${entries.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(25.dp))

            when {

                // Display loading indicator.
                isLoading -> {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag(
                                "lottery_bookings_loading"
                            )
                        )
                    }
                }

                // Display loading errors.
                errorMessage != null -> {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = errorMessage
                                ?: "Failed to load lottery entries.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag(
                                "lottery_bookings_error"
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                reloadKey++
                            },
                            modifier = Modifier.testTag(
                                "retry_lottery_bookings"
                            )
                        ) {
                            Text("Try Again")
                        }
                    }
                }

                // Display an empty state.
                entries.isEmpty() -> {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "No lottery entries.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag(
                                "empty_lottery_bookings"
                            )
                        )
                    }
                }

                // Display lottery entries.
                else -> {

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp),
                        contentPadding =
                            PaddingValues(bottom = 16.dp)
                    ) {

                        items(
                            items = entries,
                            key = { it.entryId }
                        ) { entry ->

                            val lottery = lotteries[entry.lotteryId]

                            if (lottery != null) {

                                // A lottery may cover multiple campsites.
                                val lotteryCampsites =
                                    lottery.campsiteIds.mapNotNull { id ->

                                        controllers.campsites
                                            .getCampsite(id)
                                    }

                                val campsiteName =
                                    if (lotteryCampsites.isEmpty()) {

                                        "Unknown campsite"

                                    } else {

                                        lotteryCampsites.joinToString(", ") {
                                            it.campsiteName
                                        }
                                    }

                                val address =
                                    if (lotteryCampsites.size == 1) {

                                        lotteryCampsites.first().address

                                    } else {

                                        ""
                                    }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag(
                                            "lottery_entry_${entry.entryId}"
                                        )
                                ) {

                                    LotteryEntryCard(
                                        campsiteName = campsiteName,
                                        address = address,
                                        entry = entry,
                                        lottery = lottery,
                                        onClick = {

                                            navController.navigate(
                                                "registrationStatus/" +
                                                        entry.entryId
                                            ) {
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }

                            } else {

                                // Handle missing lottery information.
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {

                                        Text(
                                            text = "Lottery unavailable",
                                            style = MaterialTheme
                                                .typography.titleMedium
                                        )

                                        Text(
                                            text = "The associated lottery " +
                                                    "could not be found.",
                                            style = MaterialTheme
                                                .typography.bodyMedium,
                                            color = MaterialTheme
                                                .colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
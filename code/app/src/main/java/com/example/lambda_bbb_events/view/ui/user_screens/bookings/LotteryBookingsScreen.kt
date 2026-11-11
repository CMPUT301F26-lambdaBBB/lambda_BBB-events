
package com.example.lambda_bbb_events.view.ui.user_screens.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar
import com.example.lambda_bbb_events.view.ui.components.lottery.LotteryEntryCard

/*
 * Defines the lottery bookings screen, allowing navigation between
 * reservation categories and viewing of lottery entries.
 *
 * TODO: Retrieve and display the user's lottery entries.
 */

/**
 * Displays the user's lottery bookings screen.
 *
 * Provides navigation between active, past, and lottery
 * reservations, along with a section for lottery entries.
 *
 * @param navController Handles screen navigation.
 * @param controllers Provides access to application controllers.
 */
@Composable
fun LotteryBookingsScreen(
    navController: NavController,
    controllers: AppControllers
) {
    var entries by remember { mutableStateOf<List<WaitlistEntry>>(emptyList()) }
    var lotteries by remember { mutableStateOf<Map<String, Lottery>>(emptyMap()) }   // by lotteryId
    var campsites by remember { mutableStateOf<Map<String, Campsite>>(emptyMap()) }  // by campsiteId
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val loadedEntries = controllers.waitlistEntries.getEntriesForEntrant(
            controllers.users.user.deviceId
        )
        val loadedLotteries = mutableMapOf<String, Lottery>()
        val loadedCampsites = mutableMapOf<String, Campsite>()

        for (entry in loadedEntries) {
            val lottery = controllers.lotteries.getLottery(entry.lotteryId)

            if (lottery != null) {
                loadedLotteries[lottery.lotteryId] = lottery

                val campsiteId = lottery.campsiteIds.firstOrNull()

                if (campsiteId != null) {
                    val campsite = controllers.campsites.getCampsite(campsiteId)

                    if (campsite != null) {
                        loadedCampsites[campsiteId] = campsite
                    }
                }
            }
        }

        entries = loadedEntries
        lotteries = loadedLotteries
        campsites = loadedCampsites
        isLoading = false
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
                .padding(horizontal = 16.dp)
                .testTag("lottery_bookings")
        ) {
            ScreenTitle(
                title = "My Reservations",
                navController = navController
            )

            BookingsTypeBar(navController)

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = "In Lottery",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(25.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You have not joined any lotteries yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries) { entry ->
                        val lottery = lotteries[entry.lotteryId]

                        if (lottery != null) {
                            val campsite = campsites[lottery.campsiteIds.firstOrNull() ?: ""]

                            LotteryEntryCard(
                                campsiteName = campsite?.campsiteName ?: "Unknown campsite",
                                address = campsite?.address ?: "",
                                entry = entry,
                                lottery = lottery,
                                onClick = {
                                    if (campsite != null) {
                                        navController.navigate("campsite_details/${campsite.campsiteId}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

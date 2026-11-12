package com.example.lambda_bbb_events.view.ui.owner_screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.LotteryFilter
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.lottery.EntryStatusPill
import com.example.lambda_bbb_events.view.ui.components.lottery.OwnerLotteryCard
import com.example.lambda_bbb_events.view.ui.components.lottery.formatSingleDate

/*
 * Defines the campsite owner's screen for one lottery, showing the lottery
 * and the entrants on its waitlist.
 *
 * TODO: Implement Edit Lottery (US 02.04.06, final checkpoint).
 * TODO: Show the winners once winners can be drawn (US 02.05.01).
 */

/**
 * Displays a lottery and the list of entrants who joined its waitlist.
 *
 * Each entrant has info about their name, number of guests, when they joined, and the
 * state of their entry.
 *
 * @param navController Controls screen navigation.
 * @param controllers The controllers for the app.
 * @param lotteryId The ID of the lottery.
 */
@Composable
fun OwnerLotteryDetailsScreen(
    navController: NavController,
    controllers: AppControllers,
    lotteryId: String
) {
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var entries by remember { mutableStateOf<List<WaitlistEntry>>(emptyList()) }
    var names by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(lotteryId) {
        val loadedLottery = controllers.lotteries.getLottery(lotteryId)

        if (loadedLottery != null) {
            val loadedEntries = controllers.waitlistEntries.getEntriesForLottery(lotteryId)
            val loadedNames = mutableMapOf<String, String>()

            for (entry in loadedEntries) {
                loadedNames[entry.entrantId] = controllers.users.getUserName(entry.entrantId)
            }

            entries = loadedEntries
            names = loadedNames
        }

        lottery = loadedLottery
        isLoading = false
    }

    val currentLottery = lottery

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle(
            title = "Lottery Details",
            navController = navController,
            showBack = true
        )

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
        } else if (currentLottery == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lottery not found",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            val campsiteId = currentLottery.campsiteIds.firstOrNull() ?: ""

            // get lottery number
            val allLotteries = controllers.lotteries.getLotteriesForCampsite(
                campsiteId,
                LotteryFilter.ALL
            )
            var lotteryNumber = 0
            for (i in allLotteries.indices) {
                if (allLotteries[i].lotteryId == currentLottery.lotteryId) {
                    lotteryNumber = i + 1
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OwnerLotteryCard(
                        lotteryNumber = lotteryNumber,
                        lottery = currentLottery,
                        phase = controllers.lotteries.getPhase(currentLottery),
                        onClick = {}
                    )
                }

                item {
                    OutlinedButton(
                        onClick = {
                            navController.navigate("edit_lottery/${currentLottery.lotteryId}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Lottery")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { /* TODO: open the edit lottery screen */ },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Lottery")
                    }
                }

                item {
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Waitlist (${entries.size} / ${currentLottery.waitlistCapacity})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (entries.isEmpty()) {
                    item {
                        Text(
                            text = "Nobody has joined this lottery yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(entries) { entry ->
                    EntrantRow(
                        name = names[entry.entrantId] ?: "Unknown user",
                        entry = entry
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

/**
 * Displays info of an entrant on the waitlist including their name, number of guests,
 * the day they joined, and the status of their entry.
 *
 * @param name The entrant's name.
 * @param entry The entrant's waitlist entry.
 */
@Composable
private fun EntrantRow(name: String, entry: WaitlistEntry) {
    val rowShape = RoundedCornerShape(12.dp)
    val guestWord = if (entry.numberOfGuests == 1) "guest" else "guests"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, rowShape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${entry.numberOfGuests} $guestWord  |  Joined ${formatSingleDate(entry.joinedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        EntryStatusPill(entry.status)
    }
}
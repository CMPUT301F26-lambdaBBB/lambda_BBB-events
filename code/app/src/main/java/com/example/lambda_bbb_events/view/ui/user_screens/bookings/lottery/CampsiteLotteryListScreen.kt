package com.example.lambda_bbb_events.view.ui.user_screens.bookings.lottery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.LotteryFilter
import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.view.ui.components.campsite.VerticalCampsiteCard
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.lottery.OwnerLotteryCard

/**
 * Displays the campsite owner's lottery management screen.
 *
 * Organizes lotteries into active, past, and canceled sections.
 * Shows an error message if the campsite cannot be found.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves campsite and lottery information.
 * @param campsiteId The ID of the campsite being managed.
 */
@Composable
fun CampsiteLotteryListScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)

    if (campsite == null) {
        Text(
            text = "Campsite not found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    val allLotteries = controllers.lotteries.getLotteriesForCampsite(campsiteId, LotteryFilter.ALL)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScreenTitle(
                    title = "Lottery List",
                    navController = navController,
                    showBack = true
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VerticalCampsiteCard(
                        campsite = campsite,
                        onClick = {},
                        modifier = Modifier
                    )

                    if (allLotteries.isEmpty()) {
                        Text(
                            text = "There are no current lotteries for this campsite.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LotteryList("Lotteries", allLotteries, navController, controllers)

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

/**
 * Displays a list of lotteries for a particular campsite.
 *
 * @param title The section title.
 * @param allLotteries Every lottery of the campsite in order. Used to number the cards.
 * @param controllers The app's controllers.
 */
@Composable
private fun LotteryList(
    title: String,
    allLotteries: List<Lottery>,
    navController: NavController,
    controllers: AppControllers
) {
    if (allLotteries.isEmpty()) {
        return
    }

    HorizontalDivider(Modifier.padding(top = 5.dp))

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )

    for (lottery in allLotteries) {
        OwnerLotteryCard(
            lotteryNumber = allLotteries.indexOf(lottery) + 1,
            lottery = lottery,
            phase = controllers.lotteries.getPhase(lottery),
            onClick = { navController.navigate("lottery_details/${lottery.lotteryId}") }
        )
    }
}
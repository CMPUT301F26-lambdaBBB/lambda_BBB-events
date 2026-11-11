
package com.example.lambda_bbb_events.view.ui.owner_screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.LotteryFilter
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.view.ui.user_screens.explore.SectionTitle
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.lottery.OwnerLotteryCard

/*
 * Defines the campsite owner's lottery management screen.
 *
 * TODO: Implement the View Booked Dates and Create Lottery buttons.
 * TODO: Display the campsite's actual image.
 */

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
fun OwnerManageLotteryScreen(
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
    val activeLotteries = controllers.lotteries.getLotteriesForCampsite(campsiteId, LotteryFilter.ACTIVE)
    val pastLotteries = controllers.lotteries.getLotteriesForCampsite(campsiteId, LotteryFilter.PAST)
    val cancelledLotteries = controllers.lotteries.getLotteriesForCampsite(campsiteId, LotteryFilter.CANCELLED)
    val activeLotteriesCount = activeLotteries.size

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
                    title = "Manage Lottery",
                    navController = navController,
                    showBack = true
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Campsite information card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row {
                            // Image placeholder
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .fillMaxHeight()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "image",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 10.dp
                                    ),
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Campsite name
                                Text(
                                    text = campsite.campsiteName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                // Location
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = "Location",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = campsite.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                // Maximum occupancy
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = "Max occupancy",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = "${campsite.maxOccupancy} people",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                // Active lottery count
                                Text(
                                    text = "$activeLotteriesCount Lotteries Active",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // View booked dates
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            // TODO: Implement booked dates navigation.
                        }
                    ) {
                        Text(
                            text = "View Booked Dates",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Create lottery
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            navController.navigate("create_lottery/${campsite.campsiteId}")
                        }
                    ) {
                        Text(
                            text = "+ Create Lottery",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    if (allLotteries.isEmpty()) {
                        HorizontalDivider()
                        Text("No lotteries yet. Tap \"+ Create Lottery\" to list this campsite in one.")
                    }

                    LotterySection("Active Lotteries", activeLotteries, allLotteries, controllers)
                    LotterySection("Past Lotteries", pastLotteries, allLotteries, controllers)
                    LotterySection("Cancelled Lotteries", cancelledLotteries, allLotteries, controllers)

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

/**
 * Each lottery has a divider, title, and card with info in it. Nothing is shown when the
 * list is empty, meaning a section is only shown if it has at least one lottery.
 *
 * @param title The section title, like "Active Lotteries".
 * @param lotteries The lotteries in this section.
 * @param allLotteries Every lottery of the campsite in order, used to number the cards.
 * @param controllers The app's controllers, used to get each lottery's phase.
 */
@Composable
private fun LotterySection(
    title: String,
    lotteries: List<Lottery>,
    allLotteries: List<Lottery>,
    controllers: AppControllers
) {
    if (lotteries.isEmpty()) {
        return
    }

    HorizontalDivider(Modifier.padding(top = 5.dp))
    SectionTitle(title)

    for (lottery in lotteries) {
        OwnerLotteryCard(
            lotteryNumber = allLotteries.indexOf(lottery) + 1,
            lottery = lottery,
            phase = controllers.lotteries.getPhase(lottery),
            onClick = { /* TODO: open the lottery's details and waitlist (US 02.09.01) */ }
        )
    }
}

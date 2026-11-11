package com.example.lambda_bbb_events.view.ui.owner_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.LotteryFilter
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.view.ui.user_screens.explore.SectionTitle
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.lottery.LotteryCard

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
        Text("Campsite not found")
        return
    }

    val allLotteries = controllers.lotteries.getLotteriesForCampsite(campsiteId, LotteryFilter.ALL)
    val activeLotteries = controllers.lotteries.getLotteriesForCampsite(campsiteId, LotteryFilter.ACTIVE)
    val pastLotteries = controllers.lotteries.getLotteriesForCampsite(campsiteId, LotteryFilter.PAST)
    val cancelledLotteries = controllers.lotteries.getLotteriesForCampsite(campsiteId, LotteryFilter.CANCELLED)
    val activeLotteriesCount = activeLotteries.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ScreenTitle(
                    "Manage Lottery",
                    navController,
                    showBack = true
                )

                val cardShape = RoundedCornerShape(12.dp)

                // modified version of the VerticalCampsiteCard.kt
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(cardShape)
                        .border(1.dp, Color(0xFFE5E5E5), cardShape)
                        .background(Color.White)
                ) {
                    // image placeholder, replace with the real campsite image later
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .fillMaxHeight()
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("image", color = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(-2.dp, Alignment.CenterVertically)
                    ) {
                        // name
                        Text(
                            text = campsite.campsiteName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // location
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = campsite.address,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // max occupancy
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Max occupancy",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${campsite.maxOccupancy} people",
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // active lottery count
                        Text(
                            text = "$activeLotteriesCount Lotteries Active",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO */ }
                ) {
                    Text("View Booked Dates")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("create_lottery/${campsite.campsiteId}") }
                ) {
                    Text("+ Create Lottery")
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

    HorizontalDivider()
    SectionTitle(title)

    for (lottery in lotteries) {
        LotteryCard(
            lotteryNumber = allLotteries.indexOf(lottery) + 1,
            lottery = lottery,
            phase = controllers.lotteries.getPhase(lottery),
            onClick = { /* TODO: open the lottery's details and waitlist (US 02.09.01) */ }
        )
    }
}
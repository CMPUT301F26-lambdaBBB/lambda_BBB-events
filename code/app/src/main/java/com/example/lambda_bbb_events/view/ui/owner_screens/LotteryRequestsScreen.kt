package com.example.lambda_bbb_events.view.ui.owner_screens

import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.lottery.formatDateRange
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen where an owner accepts or declines multiple campsite lotteries that other
 * owners added their campsites to. Declining cancels the whole lottery.
 *
 * @param navController The navigation controller.
 * @param controllers The app's controllers.
 */
@Composable
fun LotteryRequestsScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val cardShape = RoundedCornerShape(16.dp)

    val currentDeviceId = controllers.users.user.deviceId
    val scope = rememberCoroutineScope()

    var requests by remember { mutableStateOf<List<Lottery>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var reload by remember { mutableStateOf(0) } // for refresh

    LaunchedEffect(reload) {
        requests = controllers.lotteries.getLotteryRequests(currentDeviceId)
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScreenTitle("Lottery Requests", navController, showBack = true)

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
        } else if (requests.isEmpty()) {
            Text(
                text = "You don't currently have any lottery requests.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (lottery in requests) {
                    val campsiteNames = lottery.campsiteIds
                        .mapNotNull { controllers.campsites.campsites[it]?.campsiteName }
                        .joinToString(", ")

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Campsites: $campsiteNames",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = "Registration",
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy((2).dp)) {
                                Text(
                                    text = "Registration",
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatDateRange(
                                        lottery.registrationStart,
                                        lottery.registrationEnd
                                    ),
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = "Booking",
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy((2).dp)) {
                                Text(
                                    text = "Booking",
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatDateRange(lottery.bookingStart, lottery.bookingEnd),
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Winners and waitlist",
                                modifier = Modifier.size(20.dp)
                            )

                            val winnerWord = if (lottery.numberOfWinners == 1) "winner" else "winners"
                            Text(
                                text = "${lottery.numberOfWinners} $winnerWord",
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        controllers.lotteries.declineLotteryRequest(lottery)
                                        reload++
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Decline")
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        controllers.lotteries.acceptLotteryRequest(
                                            lottery,
                                            currentDeviceId
                                        )
                                        reload++
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Accept")
                            }
                        }
                    }
                }
            }
        }
    }
}
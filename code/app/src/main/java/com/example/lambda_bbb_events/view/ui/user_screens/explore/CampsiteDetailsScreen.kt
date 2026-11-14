
package com.example.lambda_bbb_events.view.ui.user_screens.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.generateQR
import com.example.lambda_bbb_events.view.ui.components.rating.RatingSummary
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
/*
 * Displays campsite information, including ratings, location,
 * tags, description, and reservation availability.
 *
 * TODO: Replace the image carousel placeholder with campsite images.
 * TODO: Make the QR code scannable and navigate to campsite details.
 * TODO: Display the campsite location using Google Maps.
 * TODO: Implement the campsite reviews section.
 */

/**
 * Displays detailed information about a selected campsite.
 *
 * Shows campsite tags, description, and placeholders for images,
 * QR code, and map. Allows users to check campsite availability.
 *
 * The title remains fixed while the campsite details scroll.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves campsite information.
 * @param campsiteId The ID of the selected campsite.
 */
@Composable
fun CampsiteDetailsScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)

    val entrantId = controllers.users.deviceId

    val lottery = controllers.lotteries
        .getOpenLotteryForCampsite(campsiteId)

    val existingEntry = if (lottery != null) {
        controllers.waitlistEntries
            .getEntriesForEntrant(entrantId)
            .firstOrNull { entry ->
                entry.lotteryId == lottery.lotteryId
            }
    } else {
        null
    }

    val hasEnteredLottery = existingEntry != null

    var showQrDialog by remember { mutableStateOf(false) }

    val qrThumbnail = remember(campsiteId) {
        generateQR(campsiteId, 300, 300)
    }

    if (campsite == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Campsite not found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen_campsite_details")
            .padding(horizontal = 16.dp)
    ) {
        // Fixed title
        ScreenTitle(
            title = campsite.campsiteName,
            navController = navController,
            showBack = true
        )

        // Scrollable campsite information
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 12.dp
            )
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Image carousel placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Campsite Images",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Campsite rating
                    RatingSummary(
                        campsite.rating,
                        campsite.ratingCount
                    )

                    // Campsite location
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = campsite.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Maximum occupancy
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Max People",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Text(
                                text = "Max Occupancy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "${campsite.maxOccupancy} people",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (tag in campsite.tags) {
                            DisplayTag(
                                text = tag,
                                bgColor = MaterialTheme.colorScheme.secondaryContainer,
                                borderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = "Campsite QR Code",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                showQrDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrThumbnail != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    bitmap = qrThumbnail.asImageBitmap(),
                                    contentDescription = "QR Code Preview",
                                    modifier = Modifier.size(110.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap to enlarge QR Code",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "Tap to view QR Code",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (showQrDialog) {
                        QRCodeDialog(
                            campsiteName = campsite.campsiteName,
                            campsiteId = campsiteId,
                            onDismiss = { showQrDialog = false }
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = campsite.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = "Map Location",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Google Map",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Check availability or view registration status
                    Button(
                        onClick = {
                            if (hasEnteredLottery) {
                                navController.navigate(
                                    "registrationStatus/${existingEntry.entryId}"
                                )
                            } else {
                                navController.navigate(
                                    "campsite_check_availability/$campsiteId"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (hasEnteredLottery) {
                                "Registration Status"
                            } else {
                                "Check Availability"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // TODO: Implement reviews section.
                }
            }
        }
    }
}

/**
 * Displays a campsite tag with customizable colors.
 *
 * @param text The text displayed inside the tag.
 * @param bgColor The background color of the tag.
 * @param borderColor The color of the tag's border.
 */
@Composable
fun DisplayTag(
    text: String,
    bgColor: Color,
    borderColor: Color
) {
    Surface(
        color = bgColor,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
        )
    }
}

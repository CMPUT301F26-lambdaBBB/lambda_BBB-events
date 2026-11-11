
package com.example.lambda_bbb_events.view.ui.owner_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.rating.RatingSummary
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle

/*
 * Displays campsite details and provides management actions
 * for owners in managing lotteries.
 *
 * TODO: Replace the image carousel placeholder with campsite images.
 * TODO: Display actual upcoming, past, and cancelled reservations.
 * TODO: Implement campsite deletion with confirmation.
 */

/**
 * Displays campsite details and management options for owners.
 *
 * Allows owners to edit, view, and manage campsite lotteries.
 * Includes reservation sections and a placeholder delete action.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves campsite information.
 * @param campsiteId The ID of the campsite being managed.
 */
@Composable
fun OwnerCampsiteDetailsScreen(
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
                    title = campsite.campsiteName,
                    navController = navController,
                    showBack = true
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
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
                            text = "some images here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Campsite rating
                    RatingSummary(
                        rating = campsite.rating,
                        reviewCount = campsite.ratingCount
                    )

                    // Campsite address
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
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
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
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

                    // Owner management actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OwnerActionButton(
                            label = "Edit",
                            icon = Icons.Outlined.Edit,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = {
                                navController.navigate(
                                    "edit_campsite/${campsite.campsiteId}"
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        OwnerActionButton(
                            label = "View",
                            icon = Icons.Outlined.Visibility,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = {
                                navController.navigate(
                                    "campsite_details/${campsite.campsiteId}"
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        OwnerActionButton(
                            label = "Manage Lottery",
                            icon = Icons.Outlined.ManageAccounts,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = {
                                navController.navigate(
                                    "manage_lottery/${campsite.campsiteId}"
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        OwnerActionButton(
                            label = "Delete",
                            icon = Icons.Outlined.Delete,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            onClick = {
                                // TODO: Implement campsite deletion with confirmation.
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Upcoming reservations
                    Text(
                        text = "Upcoming Reservations",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OwnerReservationPlaceholder("upcoming")
                    OwnerReservationPlaceholder("lalalalala")

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Past reservations
                    Text(
                        text = "Past Reservations",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OwnerReservationPlaceholder("pssst")

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Cancelled reservations
                    Text(
                        text = "Cancelled Reservations",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OwnerReservationPlaceholder("im a can")
                    OwnerReservationPlaceholder("canada")

                    Spacer(
                        modifier = Modifier.height(50.dp)
                    )
                }
            }
        }
    }
}

/**
 * Displays a customizable button for campsite management actions.
 *
 * @param label Text displayed below the button icon.
 * @param icon The [ImageVector] displayed above the label.
 * @param backgroundColor Background color of the button.
 * @param contentColor Color used for the icon and text.
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier Modifier used to customize the button's
 * layout and appearance.
 */
@Composable
fun OwnerActionButton(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/**
 * Displays placeholder content for a reservation.
 *
 * Uses theme colors until real reservation data
 * is implemented.
 *
 * @param text Placeholder text to display.
 */
@Composable
private fun OwnerReservationPlaceholder(
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

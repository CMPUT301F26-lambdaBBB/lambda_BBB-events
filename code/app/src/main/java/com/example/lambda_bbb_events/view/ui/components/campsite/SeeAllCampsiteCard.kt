package com.example.lambda_bbb_events.view.ui.components.campsite

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.view.ui.components.common.CampsiteThumbnail
import com.example.lambda_bbb_events.view.ui.components.rating.formatRating

/**
 * Displays a campsite in an expanded campsite list.
 *
 * @param campsite The campsite to display.
 * @param onClick Callback when the campsite is selected.
 * @param modifier Modifier for customizing the card.
 */
@Composable
fun SeeAllCampsiteCard(
    campsite: Campsite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CampsiteThumbnail(
                campsite = campsite,
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            bottomStart = 12.dp
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Campsite name.
                Text(
                    text = campsite.campsiteName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Campsite address.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = campsite.address,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Campsite rating.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = if (campsite.ratingCount == 0) {
                            "No reviews"
                        } else {
                            "${formatRating(campsite.rating)} " +
                                    "(${campsite.ratingCount})"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Maximum occupancy.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Maximum occupancy",
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = "${campsite.maxOccupancy}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
package com.example.lambda_bbb_events.view.ui.components.campsite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.view.ui.components.common.CampsiteThumbnail
import com.example.lambda_bbb_events.view.ui.components.common.ImageFraming

/*
 * Defines a compact campsite card for horizontally scrolling lists.
 */

/**
 * Displays a campsite card with its image above its key details.
 *
 * Shows the campsite image, name, location, rating, and maximum occupancy.
 *
 * @param campsite The campsite information to display.
 * @param onClick Callback triggered when the card is selected.
 * @param modifier Customizes the card's layout and appearance.
 */
@Composable
fun HorizontalCampsiteCard(
    campsite: Campsite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .width(220.dp)
            .clip(cardShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = cardShape
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .testTag("horizontal_campsite_card_${campsite.campsiteId}")
    ) {
        // Campsite image
        // Framed like the list thumbnail and campsite details, so they all match
        CampsiteThumbnail(
            campsite = campsite,
            framing = ImageFraming.THUMBNAIL,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Campsite name
            Text(
                text = campsite.campsiteName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Campsite location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = campsite.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Campsite rating and maximum occupancy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Rating",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (campsite.ratingCount > 0) {
                        "${String.format(LocalConfiguration.current.locales[0], "%.1f", campsite.rating)} (${campsite.ratingCount})"
                    } else {
                        "No reviews"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Maximum occupancy",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = campsite.maxOccupancy.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

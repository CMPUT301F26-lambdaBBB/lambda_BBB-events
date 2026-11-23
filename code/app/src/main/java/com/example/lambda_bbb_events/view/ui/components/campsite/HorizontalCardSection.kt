package com.example.lambda_bbb_events.view.ui.components.campsite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.Campsite

/*
 * Defines a horizontally scrollable section of campsite cards
 * with a title and a "See All" action.
 */

/**
 * Displays a horizontally scrollable section of campsite cards.
 *
 * Uses [HorizontalCampsiteCard] to render each campsite card.
 *
 * @param title The heading displayed above the campsite cards.
 * @param campsites The list of [Campsite] objects to display.
 * @param onCampsiteClick Callback invoked with the selected campsite.
 * @param onSeeAll Callback invoked when the user clicks "See All".
 * @param modifier Modifier used to customize the section layout.
 * @param emptyMessage Text displayed when there are no campsites.
 */
@Composable
fun HorizontalCardSection(
    title: String,
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "No campsites found."
) {
    Column(
        modifier = modifier.padding(bottom = 25.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "See All",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onSeeAll()
                }
            )
        }

        if (campsites.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(campsites, key = { it.campsiteId }) { campsite ->
                    HorizontalCampsiteCard(
                        campsite = campsite,
                        onClick = {
                            onCampsiteClick(campsite)
                        }
                    )
                }
            }
        }
    }
}

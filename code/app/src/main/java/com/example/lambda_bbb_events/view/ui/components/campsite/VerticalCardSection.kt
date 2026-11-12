
package com.example.lambda_bbb_events.view.ui.components.campsite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.Campsite

/**
 * Displays a section of vertically arranged campsite cards.
 *
 * Uses [VerticalCampsiteCard] to display individual campsites.
 * The parent screen handles vertical scrolling.
 *
 * @param title The heading displayed above the campsite list.
 * @param campsites The list of [Campsite] objects to display.
 * @param onCampsiteClick Callback invoked with the selected campsite.
 * @param onSeeAll Callback invoked when "See All" is clicked.
 * @param modifier Modifier used to customize the section layout.
 */
@Composable
fun VerticalCardSection(
    title: String,
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 25.dp),
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
                modifier = Modifier
                    .testTag("seeAllButton")
                    .clickable {
                        onSeeAll()
                    }
            )
        }

        if (campsites.isEmpty()) {
            Text(
                text = "No campsites found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                campsites.forEach { campsite ->
                    VerticalCampsiteCard(
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

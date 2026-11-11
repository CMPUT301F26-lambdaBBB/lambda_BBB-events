package com.example.lambda_bbb_events.view.ui.components.campsite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
 * Defines a horizontally scrollable section of campsite cards
 * with a title and a "See All" action.
 *
 * TODO: Replace placeholder cards with actual campsite data.
 */

/**
 * Displays a horizontally scrollable section of campsite cards.
 *
 * Uses [HorizontalCampsiteCard] to render each campsite card.
 *
 * @param title The heading displayed above the campsite cards.
 * @param onSeeAll Callback invoked when the user clicks "See All".
 * @param modifier Modifier used to customize the section layout.
 * @param itemCount Number of campsite cards to display.
 * Defaults to five.
 */
@Composable
fun HorizontalCardSection(
    title: String,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    itemCount: Int = 5
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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(itemCount) {
                HorizontalCampsiteCard()
            }
        }
    }
}

package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays a horizontally scrollable section of campsite cards.
 *
 * Uses [HorizontalCampsiteCard] to render each campsite card.
 *
 * @param title The heading displayed above the campsite cards.
 * @param onSeeAll Callback invoked when the user clicks "See All".
 * @param modifier Modifier intended to customize the section layout.
 * Currently unused by the implementation.
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
        modifier = Modifier.padding(bottom = 25.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                // to yuta: make sure we have consistent text styling, create custom in ui.theme.Type and replace
                title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5a4ef5),
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
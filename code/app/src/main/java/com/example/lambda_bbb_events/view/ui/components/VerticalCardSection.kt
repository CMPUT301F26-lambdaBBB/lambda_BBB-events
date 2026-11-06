package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.model.Campsite

/*
 * Defines a vertically scrollable section of campsite cards
 * and function to press "See All" to view more campsites
 */

/**
 * Displays a vertically scrollable section of campsite cards.
 *
 * Uses [VerticalCampsiteCard] to display individual campsites.
 *
 * @param title The heading displayed above the campsite list.
 * @param campsites The list of [Campsite] objects to display.
 * @param onCampsiteClick Callback invoked with the selected
 * campsite when a card is clicked.
 * @param onSeeAll Callback invoked when "See All" is clicked.
 * @param modifier Modifier used to customize the section layout.
 */
@Composable
fun VerticalCardSection(
    title: String,
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(bottom = 25.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5a4ef5),
                modifier = Modifier
                    .testTag("seeAllButton")
                    .clickable {
                    onSeeAll()
                }
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(campsites) { campsite ->
                VerticalCampsiteCard(campsite, onClick = { onCampsiteClick(campsite) })
            }
        }
    }
}
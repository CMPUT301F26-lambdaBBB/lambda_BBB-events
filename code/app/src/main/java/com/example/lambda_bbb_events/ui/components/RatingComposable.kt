package com.example.lambda_bbb_events.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun RatingStars(
    rating: Float,
    modifier: Modifier = Modifier
) {
    // to yuta: change this later to display actually star icons instead of whatever this is
    Text(
        text = "★★★★☆",
        modifier = modifier,
        fontSize = 20.sp,
        color = Color(0xFFFFC107)
    )
}

@Composable
fun RatingSummary(
    rating: Float,
    reviewCount: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RatingStars(rating = rating)
        Text("  $rating ($reviewCount reviews)")
    }
}
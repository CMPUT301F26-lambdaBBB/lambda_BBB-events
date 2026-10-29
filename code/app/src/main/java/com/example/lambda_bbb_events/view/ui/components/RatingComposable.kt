package com.example.lambda_bbb_events.view.ui.components

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
    // we'll most likely have a rating class, so this function might be the RatingView
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
        _root_ide_package_.com.example.lambda_bbb_events.view.ui.components.RatingStars(rating = rating)
        Text("  $rating ($reviewCount reviews)")
    }
}
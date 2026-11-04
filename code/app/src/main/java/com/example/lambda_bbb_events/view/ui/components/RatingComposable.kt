package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        RatingStars(rating = rating)
        Text("  $rating")
        Text("  ($reviewCount)", color = Color.Gray)
    }
}



@Composable
fun PickRatingStars(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 1..5) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { onRatingChange(i) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (i <= rating) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFE6C84A),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.StarBorder,
                            contentDescription = "$i stars",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(text = "$i")
            }
        }
    }
}

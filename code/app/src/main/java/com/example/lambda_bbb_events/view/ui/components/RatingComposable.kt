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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays a visual representation of a campsite rating.
 *
 * @param rating The campsite's rating value.
 * Currently unused when displaying the stars.
 * @param modifier Modifier used to customize the appearance
 * and layout of the rating display.
 */
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

/**
 * Displays a summary of a campsite's rating and review count.
 *
 * Uses [RatingStars] to display the star representation.
 *
 * @param rating The campsite's numeric rating.
 * @param reviewCount The total number of reviews.
 */
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




/**
 * Displays an interactive five-star rating selector.
 *
 * Highlights selected stars and allows users to change their rating.
 *
 * @param rating The current rating from 0 to 5.
 * @param onRatingChange Callback for updating the selected rating.
 * @param modifier Customizes the selector's layout.
 */
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
                    onClick = { onRatingChange(i) },
                    modifier = Modifier.testTag("ratingStar$i").semantics{selected = i <= rating}
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

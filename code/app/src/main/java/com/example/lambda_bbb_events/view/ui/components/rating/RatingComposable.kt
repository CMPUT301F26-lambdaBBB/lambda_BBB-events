
package com.example.lambda_bbb_events.view.ui.components.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/*
 * Provides components for displaying campsite ratings,
 * review counts, and selecting star ratings.
 *
 * Uses the application's Material 3 theme.
 */

/**
 * Displays a visual representation of a campsite rating.
 *
 * The number of filled stars reflects the rating value.
 *
 * @param rating The campsite's rating value.
 * @param modifier Modifier used to customize the rating display.
 */
@Composable
fun RatingStars(
    rating: Float,
    modifier: Modifier = Modifier
) {
    val filledStars = rating
        .coerceIn(0f, 5f)
        .roundToInt()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= filledStars) {
                    Icons.Filled.Star
                } else {
                    Icons.Outlined.StarBorder
                },
                contentDescription = null,
                tint = if (i <= filledStars) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RatingStars(rating = rating)

        Text(
            text = "$rating",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "($reviewCount)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                    onClick = {
                        onRatingChange(i)
                    },
                    modifier = Modifier
                        .testTag("ratingStar$i")
                        .semantics {
                            selected = i <= rating
                        }
                ) {
                    Icon(
                        imageVector = if (i <= rating) {
                            Icons.Filled.Star
                        } else {
                            Icons.Outlined.StarBorder
                        },
                        contentDescription = "$i stars",
                        tint = if (i <= rating) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "$i",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (i <= rating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

package com.example.lambda_bbb_events.view.ui.components.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.review.Review
import com.example.lambda_bbb_events.view.ui.components.rating.PickRatingStars

/**
 * Displays a dropdown that opens in place to write a review, or edit one that was already
 * written. Closing it by its heading keeps what was entered, so it can be opened again.
 *
 * @param title The heading, which opens and closes the dropdown.
 * @param isOpen Whether the dropdown is open.
 * @param rating The chosen stars, or 0 if none are chosen yet.
 * @param comment The comment entered so far.
 * @param isSaving Whether the review is being saved, so nothing can be changed.
 * @param errorMessage Why the review could not be saved, or `null` if there is no error.
 * @param onToggle Called when the heading is tapped.
 * @param onRatingChange Called with the newly chosen stars.
 * @param onCommentChange Called with the newly entered comment.
 * @param onSubmit Called when the review should be saved.
 * @param onCancel Called when what was entered should be discarded.
 * @param modifier Customizes the dropdown's layout.
 */
@Composable
fun ReviewForm(
    title: String,
    isOpen: Boolean,
    rating: Int,
    comment: String,
    isSaving: Boolean,
    errorMessage: String?,
    onToggle: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSaving, role = Role.Button, onClick = onToggle)
                    .semantics { stateDescription = if (isOpen) "Expanded" else "Collapsed" }
                    .padding(16.dp)
                    .testTag("writeReview"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isOpen) {
                        Icons.Outlined.KeyboardArrowUp
                    } else {
                        Icons.Outlined.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isOpen) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        .testTag("reviewForm"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PickRatingStars(
                        rating = rating,
                        onRatingChange = {
                            if (!isSaving) {
                                onRatingChange(it)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { onCommentChange(it.take(Review.MAX_COMMENT_LENGTH)) },
                        label = { Text("Comment (optional)") },
                        supportingText = { Text("${comment.length}/${Review.MAX_COMMENT_LENGTH}") },
                        enabled = !isSaving,
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reviewCommentInput")
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("reviewError")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = onCancel,
                            enabled = !isSaving,
                            modifier = Modifier.testTag("reviewCancel")
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = onSubmit,
                            enabled = rating >= Review.MIN_RATING && !isSaving,
                            modifier = Modifier.testTag("reviewSubmit")
                        ) {
                            Text(if (isSaving) "Saving..." else "Save")
                        }
                    }
                }
            }
        }
    }
}

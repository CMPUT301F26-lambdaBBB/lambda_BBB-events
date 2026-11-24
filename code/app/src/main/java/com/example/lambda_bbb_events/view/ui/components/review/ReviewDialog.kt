package com.example.lambda_bbb_events.view.ui.components.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.review.Review
import com.example.lambda_bbb_events.view.ui.components.rating.PickRatingStars

/**
 * Displays a dialog for writing a review, or editing one that was already written.
 *
 * @param existing The review to edit, or `null` to write a new one.
 * @param isSaving Whether the review is being saved, so nothing can be changed.
 * @param errorMessage Why the review could not be saved, or `null` if there is no error.
 * @param onSubmit Called with the chosen stars and the comment.
 * @param onDismiss Called when the dialog is closed without saving.
 */
@Composable
fun ReviewDialog(
    existing: Review?,
    isSaving: Boolean,
    errorMessage: String?,
    onSubmit: (rating: Int, comment: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by rememberSaveable { mutableIntStateOf(existing?.rating ?: 0) }
    var comment by rememberSaveable { mutableStateOf(existing?.comment ?: "") }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = { Text(if (existing == null) "Write a Review" else "Edit Your Review") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PickRatingStars(
                    rating = rating,
                    onRatingChange = {
                        if (!isSaving) {
                            rating = it
                        }
                    }
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it.take(Review.MAX_COMMENT_LENGTH) },
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating, comment) },
                enabled = rating >= Review.MIN_RATING && !isSaving,
                modifier = Modifier.testTag("reviewSubmit")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}

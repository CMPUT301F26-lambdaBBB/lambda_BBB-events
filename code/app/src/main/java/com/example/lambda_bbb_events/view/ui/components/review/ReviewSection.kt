package com.example.lambda_bbb_events.view.ui.components.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.review.Review
import com.example.lambda_bbb_events.view.ui.components.profile.ShowAlertDialog
import kotlinx.coroutines.launch

/** How many reviews are shown before the rest are asked for. */
private const val REVIEWS_SHOWN_AT_FIRST = 3

/**
 * Displays a campsite's reviews, newest first, with buttons to write, edit, and delete the
 * ones the signed-in user is allowed to. Updates by itself when reviews change.
 *
 * @param campsite The campsite whose reviews are shown.
 * @param controllers Provides the reviews and the signed-in user.
 * @param modifier Customizes the section's layout.
 */
@Composable
fun ReviewSection(
    campsite: Campsite,
    controllers: AppControllers,
    modifier: Modifier = Modifier
) {
    val user = controllers.users.currentUser
    val reviews = controllers.reviews.getReviewsForCampsite(campsite.campsiteId)
    val ownReview = user?.let { controllers.reviews.getReview(campsite.campsiteId, it.deviceId) }

    var showAll by rememberSaveable(campsite.campsiteId) { mutableStateOf(false) }
    var isWriting by rememberSaveable(campsite.campsiteId) { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf<Review?>(null) }
    var deleteError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reviewSection"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (reviews.isEmpty()) "Reviews" else "Reviews (${reviews.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (controllers.reviews.canReview(campsite, user)) {
            OutlinedButton(
                onClick = {
                    saveError = null
                    isWriting = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("writeReview")
            ) {
                Text(if (ownReview == null) "Write a Review" else "Edit Your Review")
            }
        }

        if (deleteError) {
            Text(
                text = "Could not delete the review. Please try again.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (reviews.isEmpty()) {
            Text(
                text = "No reviews yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val shownReviews = if (showAll) reviews else reviews.take(REVIEWS_SHOWN_AT_FIRST)
        for (review in shownReviews) {
            ReviewCard(
                review = review,
                reviewer = controllers.users.getCachedUser(review.reviewerId),
                onDelete = if (controllers.reviews.canDelete(review, user)) {
                    {
                        deleteError = false
                        deleting = review
                    }
                } else {
                    null
                }
            )
        }

        if (reviews.size > REVIEWS_SHOWN_AT_FIRST) {
            TextButton(
                onClick = { showAll = !showAll },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggleAllReviews")
            ) {
                Text(if (showAll) "Show fewer reviews" else "Show all ${reviews.size} reviews")
            }
        }
    }

    if (isWriting) {
        ReviewDialog(
            existing = ownReview,
            isSaving = isSaving,
            errorMessage = saveError,
            onSubmit = { rating, comment ->
                isSaving = true
                saveError = null
                scope.launch {
                    saveError = controllers.reviews.submitReview(campsite, user, rating, comment)
                    isSaving = false
                    if (saveError == null) {
                        isWriting = false
                    }
                }
            },
            onDismiss = { isWriting = false }
        )
    }

    val reviewToDelete = deleting
    if (reviewToDelete != null) {
        ShowAlertDialog(
            dialogLabel = "Delete Review?",
            dialogCaption = "This review will be removed from the campsite and its rating.\n" +
                    "This action cannot be undone.",
            confirmButton = {
                deleting = null
                scope.launch {
                    deleteError = !controllers.reviews.deleteReview(reviewToDelete, user)
                }
            },
            confirmText = "Delete",
            dismissState = { deleting = null },
            cancelText = "Cancel"
        )
    }
}

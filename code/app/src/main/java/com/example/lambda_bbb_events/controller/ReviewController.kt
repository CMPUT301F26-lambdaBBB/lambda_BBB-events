package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.repository.ReviewRepository
import com.example.lambda_bbb_events.model.review.Review
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.model.user.UserRole

/**
 * Controller for reading, writing, and deleting campsite reviews.
 *
 * @property reviewRepository The repository for [Review].
 */
class ReviewController(
    /** The repository used to load and persist reviews. */
    val reviewRepository: ReviewRepository
) {
    /**
     * Gets the reviews of a campsite, newest first.
     *
     * @param campsiteId The ID of the campsite.
     * @return The reviews of the campsite.
     */
    fun getReviewsForCampsite(campsiteId: String): List<Review> {
        return reviewRepository.getReviewsForCampsite(campsiteId)
            .sortedByDescending { it.reviewedAt }
    }

    /**
     * Gets a user's review of a campsite.
     *
     * @param campsiteId The ID of the campsite.
     * @param reviewerId The device ID of the user.
     * @return The review, or `null` if the user has not reviewed the campsite.
     */
    fun getReview(campsiteId: String, reviewerId: String): Review? {
        return reviewRepository.getReview(campsiteId, reviewerId)
    }

    /**
     * Checks if a user can review a campsite. Anyone who is signed in can, except the
     * campsite's owner. Archived campsites cannot be reviewed.
     *
     * Having stayed at the campsite is not required, since reservations are not made by the
     * app yet. Check for a past reservation here once they are.
     *
     * @param campsite The campsite to review.
     * @param user The user, or `null` if nobody is signed in.
     * @return `true` if the user can write or edit a review, `false` otherwise.
     */
    fun canReview(campsite: Campsite, user: User?): Boolean {
        return user != null && campsite.active && campsite.ownerId != user.deviceId
    }

    /**
     * Checks if a user can delete a review. Users can delete their own reviews, and admins can
     * delete anyone's.
     *
     * @param review The review to delete.
     * @param user The user, or `null` if nobody is signed in.
     * @return `true` if the user can delete the review, `false` otherwise.
     */
    fun canDelete(review: Review, user: User?): Boolean {
        return user != null &&
                (review.reviewerId == user.deviceId || user.role == UserRole.ADMIN)
    }

    /**
     * Saves a user's review of a campsite, replacing their earlier review if they have one.
     * Spaces around the comment are removed.
     *
     * @param campsite The campsite to review.
     * @param user The user writing the review, or `null` if nobody is signed in.
     * @param rating How many stars the user gave.
     * @param comment What the user wrote. May be empty.
     * @return `null` if the review was saved. An error message otherwise.
     */
    suspend fun submitReview(
        campsite: Campsite,
        user: User?,
        rating: Int,
        comment: String
    ): String? {
        if (user == null || !canReview(campsite, user)) {
            return "You cannot review this campsite."
        }
        if (rating !in Review.MIN_RATING..Review.MAX_RATING) {
            return "Please choose a star rating."
        }
        val trimmedComment = comment.trim()
        if (trimmedComment.length > Review.MAX_COMMENT_LENGTH) {
            return "Your review must be at most ${Review.MAX_COMMENT_LENGTH} characters."
        }

        try {
            reviewRepository.add(
                Review(
                    reviewId = Review.idFor(campsite.campsiteId, user.deviceId),
                    campsiteId = campsite.campsiteId,
                    reviewerId = user.deviceId,
                    rating = rating,
                    comment = trimmedComment
                )
            )
        } catch (_: Exception) {
            return "Could not save your review. Please try again."
        }
        return null
    }

    /**
     * Deletes a review.
     *
     * @param review The review to delete.
     * @param user The user deleting it, or `null` if nobody is signed in.
     * @return `true` if the review was deleted, `false` if the user cannot delete it or
     * deleting it failed.
     */
    suspend fun deleteReview(review: Review, user: User?): Boolean {
        if (!canDelete(review, user)) {
            return false
        }
        return try {
            reviewRepository.delete(review.reviewId)
            true
        } catch (_: Exception) {
            false
        }
    }
}

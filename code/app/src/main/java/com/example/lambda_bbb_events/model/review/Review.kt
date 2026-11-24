package com.example.lambda_bbb_events.model.review

import com.google.firebase.firestore.Exclude
import java.util.Date

/*
 * Defines the reviews users write about campsites.
 */

/**
 * A user's review of a campsite. Each user has at most one review of a campsite.
 *
 * @property reviewId The unique ID of the review, from [Review.idFor].
 * @property campsiteId The ID of the campsite being reviewed.
 * @property reviewerId The device ID of the user who wrote the review.
 * @property rating How many stars the user gave, from [MIN_RATING] to [MAX_RATING].
 * @property comment What the user wrote. May be empty.
 * @property reviewedAt When the review was written or last edited.
 */
data class Review(
    val reviewId: String = "",
    val campsiteId: String = "",
    val reviewerId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val reviewedAt: Date = Date()
) {
    /**
     * Whether the review has the right ID for its campsite and reviewer, a rating in range,
     * and a comment that is not too long.
     */
    @Exclude
    fun isValid(): Boolean {
        return campsiteId.isNotBlank() && reviewerId.isNotBlank() &&
                reviewId == idFor(campsiteId, reviewerId) &&
                rating in MIN_RATING..MAX_RATING &&
                comment.length <= MAX_COMMENT_LENGTH
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5

        /** The longest comment a review can have, in characters. */
        const val MAX_COMMENT_LENGTH = 500

        /**
         * Gets the ID of a user's review of a campsite. It is always the same for the same
         * user and campsite, so reviewing again edits the first review.
         *
         * @param campsiteId The ID of the campsite.
         * @param reviewerId The device ID of the user.
         */
        fun idFor(campsiteId: String, reviewerId: String): String {
            return campsiteId + "_" + reviewerId
        }
    }
}

package com.example.lambda_bbb_events.model.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewUnitTest {
    private val review = Review(
        reviewId = "site-1_user-1",
        campsiteId = "site-1",
        reviewerId = "user-1",
        rating = 4,
        comment = "Quiet and clean"
    )

    @Test
    fun idIsTheSameForTheSameUserAndCampsite() {
        assertEquals("site-1_user-1", Review.idFor("site-1", "user-1"))
    }

    @Test
    fun completeReviewIsValid() {
        assertTrue(review.isValid())
        assertTrue(review.copy(comment = "").isValid())
    }

    @Test
    fun ratingMustBeOneToFiveStars() {
        for (rating in 1..5) {
            assertTrue(review.copy(rating = rating).isValid())
        }
        assertFalse(review.copy(rating = 0).isValid())
        assertFalse(review.copy(rating = 6).isValid())
        assertFalse(review.copy(rating = -1).isValid())
    }

    @Test
    fun commentCannotBeTooLong() {
        assertTrue(review.copy(comment = "a".repeat(Review.MAX_COMMENT_LENGTH)).isValid())
        assertFalse(review.copy(comment = "a".repeat(Review.MAX_COMMENT_LENGTH + 1)).isValid())
    }

    @Test
    fun idMustMatchCampsiteAndReviewer() {
        assertFalse(review.copy(reviewId = "other").isValid())
        assertFalse(review.copy(reviewerId = "user-2").isValid())
        assertFalse(Review().isValid())
    }
}

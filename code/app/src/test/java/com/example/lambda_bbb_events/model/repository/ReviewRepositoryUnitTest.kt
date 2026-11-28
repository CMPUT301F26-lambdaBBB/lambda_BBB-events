package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.review.Review
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Verifies review repository behavior with mocked persistence. */
class ReviewRepositoryUnitTest {
    private val db = mockk<FirebaseFirestore>()
    private val transaction = mockk<Transaction>()
    private lateinit var reviews: MockCollection
    private lateinit var campsites: MockCollection
    private lateinit var repository: ReviewRepository

    private val review = Review("site-1_user-1", "site-1", "user-1", rating = 5, comment = "Great")

    @Before
    fun setup() {
        reviews = MockCollection(db, "reviews")
        campsites = MockCollection(db, "campsites")
        every { transaction.set(any(), any()) } returns transaction
        every { transaction.delete(any()) } returns transaction
        every { transaction.update(any(), any<String>(), any(), *anyVararg()) } returns transaction
        every { db.runTransaction(any<Transaction.Function<Any?>>()) } answers {
            try {
                completedTask(firstArg<Transaction.Function<Any?>>().apply(transaction))
            } catch (e: Exception) {
                failedTask(e)
            }
        }
        repository = ReviewRepository(db)
    }

    /** Stores a campsite with the given rating, or no campsite at all if [count] is `null`. */
    private fun storedCampsite(average: Double, count: Long?): DocumentReference {
        val document = mockk<DocumentSnapshot>()
        every { document.exists() } returns (count != null)
        every { document.getDouble("rating") } returns average
        every { document.getLong("ratingCount") } returns count
        every { transaction.get(campsites.document("site-1")) } returns document
        return campsites.document("site-1")
    }

    /** Stores the user's review with [rating] stars, or no review if it is `null`. */
    private fun storedReview(rating: Long?): DocumentReference {
        val document = mockk<DocumentSnapshot>()
        every { document.getLong("rating") } returns rating
        every { document.getString("campsiteId") } returns if (rating == null) null else "site-1"
        every { transaction.get(reviews.document("site-1_user-1")) } returns document
        return reviews.document("site-1_user-1")
    }

    private fun assertCampsiteRating(campsite: DocumentReference, average: Double, count: Int) {
        verify(exactly = 1) {
            transaction.update(campsite, "rating", average, "ratingCount", count)
        }
    }

    @Test
    fun firstReviewSetsTheCampsiteRating() = runTest {
        val campsite = storedCampsite(average = 0.0, count = 0)
        val reviewRef = storedReview(null)

        repository.add(review)

        verify(exactly = 1) { transaction.set(reviewRef, review) }
        assertCampsiteRating(campsite, average = 5.0, count = 1)
        assertSame(review, repository.getReview("site-1", "user-1"))
    }

    @Test
    fun anotherReviewIsAveragedIn() = runTest {
        val campsite = storedCampsite(average = 3.5, count = 2) // 7 stars
        storedReview(null)

        repository.add(review)

        assertCampsiteRating(campsite, average = 4.0, count = 3)
    }

    @Test
    fun storedAverageThatWasRoundedStillGivesTheExactTotal() = runTest {
        val campsite = storedCampsite(average = 4.333333492279053, count = 3) // 13 stars, as a float
        storedReview(null)

        repository.add(review)

        assertCampsiteRating(campsite, average = 4.5, count = 4)
    }

    @Test
    fun editingAReviewReplacesItsOldRating() = runTest {
        val campsite = storedCampsite(average = 3.0, count = 2) // 6 stars, 1 of them from this review
        storedReview(1)

        repository.update(review)

        assertCampsiteRating(campsite, average = 5.0, count = 2)
    }

    @Test
    fun deletingAReviewTakesItOutOfTheRating() = runTest {
        val campsite = storedCampsite(average = 4.0, count = 2)
        val reviewRef = storedReview(5)

        repository.delete(review)

        verify(exactly = 1) { transaction.delete(reviewRef) }
        assertCampsiteRating(campsite, average = 3.0, count = 1)
    }

    @Test
    fun deletingTheLastReviewResetsTheRating() = runTest {
        val campsite = storedCampsite(average = 5.0, count = 1)
        storedReview(5)

        repository.delete(review)

        assertCampsiteRating(campsite, average = 0.0, count = 0)
        assertNull(repository.getReview("site-1", "user-1"))
    }

    @Test
    fun reviewOfADeletedCampsiteCanStillBeDeleted() = runTest {
        storedCampsite(average = 0.0, count = null)
        val reviewRef = storedReview(5)

        repository.delete(review)

        verify(exactly = 1) { transaction.delete(reviewRef) }
        verify(exactly = 0) { transaction.update(any(), any<String>(), any(), *anyVararg()) }
    }

    @Test
    fun deletingAMissingReviewLeavesTheRatingAlone() = runTest {
        storedReview(null)

        repository.delete(review)

        verify(exactly = 0) { transaction.update(any(), any<String>(), any(), *anyVararg()) }
    }

    @Test
    fun missingCampsiteCannotBeReviewed() = runTest {
        storedCampsite(average = 0.0, count = null)
        storedReview(null)

        assertTrue(runCatching { repository.add(review) }.exceptionOrNull() is IllegalStateException)

        verify(exactly = 0) { transaction.set(any(), any()) }
        assertNull(repository.getReview("site-1", "user-1"))
    }

    @Test
    fun invalidReviewIsNotSaved() = runTest {
        val error = runCatching { repository.add(review.copy(rating = 6)) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        verify(exactly = 0) { db.runTransaction(any<Transaction.Function<Any?>>()) }
    }

    @Test
    fun listeningKeepsValidReviewsOfEachCampsite() {
        val other = Review("site-2_user-1", "site-2", "user-1", rating = 1)
        val invalid = review.copy(reviewId = "site-1_user-2", rating = 99)
        reviews.emit(
            documentOf("a", Review::class.java, review),
            documentOf("b", Review::class.java, other),
            documentOf("c", Review::class.java, invalid)
        )

        assertEquals(listOf(review), repository.getReviewsForCampsite("site-1"))
        assertEquals(listOf(other), repository.getReviewsForCampsite("site-2"))
        assertNull(repository.getReview("site-2", "user-2"))
    }
}

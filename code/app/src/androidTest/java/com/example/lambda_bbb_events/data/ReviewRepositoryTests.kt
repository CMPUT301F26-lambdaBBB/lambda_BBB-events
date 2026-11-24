package com.example.lambda_bbb_events.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.model.repository.ReviewRepository
import com.example.lambda_bbb_events.model.review.Review
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests [ReviewRepository] against the real Firestore database.
 *
 * Every campsite and review uses a unique ID and is deleted after each test in [cleanUp].
 */
@RunWith(AndroidJUnit4::class)
class ReviewRepositoryTests {
    private val testData = FirestoreTestData()
    private val campsites = CampsiteRepository()
    private lateinit var reviews: ReviewRepository
    private lateinit var campsiteId: String

    @Before
    fun setUp() = runTest {
        reviews = ReviewRepository()
        campsiteId = testData.newId("campsites", prefix = "test-campsite")
        campsites.addCampsite(Campsite(campsiteId = campsiteId, campsiteName = "Test Campsite"))
    }

    @After
    fun cleanUp() {
        reviews.stopListening()
        testData.cleanUp()
    }

    private fun newReview(user: String, rating: Int, comment: String = "Nice"): Review {
        val review = Review(Review.idFor(campsiteId, user), campsiteId, user, rating, comment)
        testData.add("reviews/" + review.reviewId)
        return review
    }

    private suspend fun assertRating(average: Float, count: Int) {
        val campsite = campsites.get(campsiteId)!!
        assertEquals(average, campsite.rating, 0.001f)
        assertEquals(count, campsite.ratingCount)
    }

    @Test
    fun reviewCanBeReadBack() = runTest {
        val review = newReview("test-user", 4, "Quiet and clean")

        reviews.add(review)

        assertEquals(review, reviews.get(review.reviewId))
        assertEquals(review, reviews.getReview(campsiteId, "test-user"))
    }

    @Test
    fun ratingFollowsAddedEditedAndDeletedReviews() = runTest {
        val first = newReview("test-user-1", 5)
        val second = newReview("test-user-2", 2)

        reviews.add(first)
        assertRating(5f, 1)
        reviews.add(second)
        assertRating(3.5f, 2)
        reviews.update(second.copy(rating = 4))
        assertRating(4.5f, 2)
        reviews.delete(first)
        assertRating(4f, 1)
        assertNull(reviews.get(first.reviewId))
        reviews.delete(second)
        assertRating(0f, 0)
    }

    @Test
    fun reviewsPostedAtTheSameTimeAreAllCounted() = runTest {
        val posted = listOf(1, 2, 3, 4, 5).map { newReview("test-user-$it", it) }

        posted.map { async { reviews.add(it) } }.awaitAll()

        assertRating(3f, 5)
    }

    @Test
    fun editingACampsiteKeepsItsRating() = runTest {
        val loadedBeforeTheReview = campsites.get(campsiteId)!!
        reviews.add(newReview("test-user", 4))

        campsites.updateCampsite(campsiteId, loadedBeforeTheReview.copy(description = "Edited"))

        assertEquals("Edited", campsites.get(campsiteId)?.description)
        assertRating(4f, 1)
    }

    @Test
    fun missingCampsiteCannotBeReviewed() = runTest {
        val review = newReview("test-user", 4)
        campsites.deleteCampsite(campsiteId)

        assertTrue(runCatching { reviews.add(review) }.isFailure)

        assertNull(reviews.get(review.reviewId))
        assertNull(campsites.get(campsiteId))
    }
}

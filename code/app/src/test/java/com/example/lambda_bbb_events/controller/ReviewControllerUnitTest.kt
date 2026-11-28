package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.repository.ReviewRepository
import com.example.lambda_bbb_events.model.review.Review
import com.example.lambda_bbb_events.model.user.Admin
import com.example.lambda_bbb_events.model.user.CampsiteOwner
import com.example.lambda_bbb_events.model.user.Entrant
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/** Verifies review controller behavior in isolation. */
class ReviewControllerUnitTest {
    private val repository = mockk<ReviewRepository>()
    private val controller = ReviewController(repository)
    private val saved = slot<Review>()

    private val campsite = Campsite(campsiteId = "site-1", ownerId = "owner-1")
    private val entrant = Entrant(deviceId = "user-1")
    private val owner = CampsiteOwner(deviceId = "owner-1")
    private val admin = Admin(deviceId = "admin-1")
    private val review = Review("site-1_user-1", "site-1", "user-1", rating = 4)

    @Before
    fun setup() {
        coEvery { repository.add(capture(saved)) } just runs
        coEvery { repository.delete(any<String>()) } just runs
    }

    @Test
    fun reviewsAreNewestFirst() {
        val older = review.copy(reviewedAt = Date(1_000))
        val newer = Review("site-1_user-2", "site-1", "user-2", rating = 2, reviewedAt = Date(2_000))
        every { repository.getReviewsForCampsite("site-1") } returns listOf(older, newer)

        assertEquals(listOf(newer, older), controller.getReviewsForCampsite("site-1"))
    }

    @Test
    fun getReviewFindsTheUsersReview() {
        every { repository.getReview("site-1", "user-1") } returns review
        every { repository.getReview("site-1", "user-2") } returns null

        assertSame(review, controller.getReview("site-1", "user-1"))
        assertNull(controller.getReview("site-1", "user-2"))
    }

    @Test
    fun everyoneButTheOwnerCanReview() {
        assertTrue(controller.canReview(campsite, entrant))
        assertTrue(controller.canReview(campsite, CampsiteOwner(deviceId = "owner-2")))
        assertTrue(controller.canReview(campsite, admin))
        assertFalse(controller.canReview(campsite, owner))
        assertFalse(controller.canReview(campsite, null))
    }

    @Test
    fun archivedCampsitesCannotBeReviewed() {
        assertFalse(controller.canReview(campsite.copy(active = false), entrant))
    }

    @Test
    fun submitReviewSavesTheTrimmedReview() = runTest {
        assertNull(controller.submitReview(campsite, entrant, 5, "  Great spot \n"))

        assertEquals("site-1_user-1", saved.captured.reviewId)
        assertEquals("site-1", saved.captured.campsiteId)
        assertEquals("user-1", saved.captured.reviewerId)
        assertEquals(5, saved.captured.rating)
        assertEquals("Great spot", saved.captured.comment)
        assertTrue(saved.captured.isValid())
    }

    @Test
    fun commentIsOptional() = runTest {
        assertNull(controller.submitReview(campsite, entrant, 3, "   "))

        assertEquals("", saved.captured.comment)
    }

    @Test
    fun submitReviewRejectsInvalidInput() = runTest {
        assertEquals("Please choose a star rating.", controller.submitReview(campsite, entrant, 0, "Hi"))
        assertEquals("Please choose a star rating.", controller.submitReview(campsite, entrant, 6, "Hi"))
        assertNotNull(
            controller.submitReview(campsite, entrant, 5, "a".repeat(Review.MAX_COMMENT_LENGTH + 1))
        )

        coVerify(exactly = 0) { repository.add(any()) }
    }

    @Test
    fun submitReviewRejectsUsersWhoCannotReview() = runTest {
        assertEquals("You cannot review this campsite.", controller.submitReview(campsite, owner, 5, ""))
        assertEquals("You cannot review this campsite.", controller.submitReview(campsite, null, 5, ""))

        coVerify(exactly = 0) { repository.add(any()) }
    }

    @Test
    fun submitReviewReportsAFailedSave() = runTest {
        coEvery { repository.add(any()) } throws RuntimeException("offline")

        assertEquals(
            "Could not save your review. Please try again.",
            controller.submitReview(campsite, entrant, 5, "")
        )
    }

    @Test
    fun usersDeleteTheirOwnReviewsAndAdminsDeleteAny() = runTest {
        assertTrue(controller.deleteReview(review, entrant))
        assertTrue(controller.deleteReview(review, admin))

        coVerify(exactly = 2) { repository.delete("site-1_user-1") }
    }

    @Test
    fun othersCannotDeleteAReview() = runTest {
        assertFalse(controller.deleteReview(review, Entrant(deviceId = "user-2")))
        assertFalse(controller.deleteReview(review, owner)) // nor the owner of the campsite
        assertFalse(controller.deleteReview(review, null))

        coVerify(exactly = 0) { repository.delete(any<String>()) }
    }

    @Test
    fun deleteReviewReportsAFailure() = runTest {
        coEvery { repository.delete(any<String>()) } throws RuntimeException("offline")

        assertFalse(controller.deleteReview(review, entrant))
    }
}

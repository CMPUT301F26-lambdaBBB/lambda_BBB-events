package com.example.lambda_bbb_events.ui.review

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.ReviewController
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.repository.ReviewRepository
import com.example.lambda_bbb_events.model.review.Review
import com.example.lambda_bbb_events.model.user.Admin
import com.example.lambda_bbb_events.model.user.CampsiteOwner
import com.example.lambda_bbb_events.model.user.Entrant
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.view.ui.components.review.ReviewSection
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Tests reading, writing, editing, and deleting reviews on a campsite's page. The repository is
 * replaced with one that keeps reviews in memory, so nothing is written to the database.
 */
@RunWith(AndroidJUnit4::class)
class ReviewSectionUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val controllers = mockk<AppControllers>(relaxed = true)
    private val repository = mockk<ReviewRepository>()

    /** The stored reviews. Compose state, like the repository's cache. */
    private var stored by mutableStateOf<List<Review>>(emptyList())

    private val campsite = Campsite(campsiteId = "site-1", ownerId = "owner-1", campsiteName = "Lakeside")
    private val ed = Entrant(deviceId = "user-1", name = "Ed")

    private fun review(userId: String, rating: Int, comment: String = "", day: Long = 1) =
        Review("site-1_$userId", "site-1", userId, rating, comment, Date(day * 86_400_000))

    private fun showSection(signedIn: User?, vararg reviews: Review) {
        stored = reviews.toList()
        every { repository.getReviewsForCampsite("site-1") } answers { stored }
        every { repository.getReview("site-1", any()) } answers {
            stored.find { it.reviewerId == secondArg() }
        }
        coEvery { repository.add(any()) } answers {
            val saved = firstArg<Review>()
            stored = stored.filter { it.reviewId != saved.reviewId } + saved
        }
        coEvery { repository.delete(any<String>()) } answers {
            stored = stored.filter { it.reviewId != firstArg() }
        }
        every { controllers.reviews } returns ReviewController(repository)
        every { controllers.users.currentUser } returns signedIn
        every { controllers.users.getCachedUser(any()) } returns null
        every { controllers.users.getCachedUser("user-1") } returns ed

        composeTestRule.setContent {
            ReviewSection(
                campsite = campsite,
                controllers = controllers,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    }

    private fun click(tag: String) {
        composeTestRule.onNodeWithTag(tag).performClick()
        composeTestRule.waitForIdle()
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun campsiteWithoutReviewsSaysSo() {
        showSection(ed)

        composeTestRule.onNodeWithText("No reviews yet.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reviewCard").assertDoesNotExist()
    }

    @Test
    fun reviewShowsWhoWroteItAndTheirComment() {
        showSection(null, review("user-1", 4, "Quiet and clean"))

        composeTestRule.onNodeWithText("Reviews (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quiet and clean").assertIsDisplayed()
    }

    @Test
    fun reviewOfADeletedAccountIsKept() {
        showSection(ed, review("gone", 2, "Too windy"))

        composeTestRule.onNodeWithText("Deleted user").assertIsDisplayed()
        composeTestRule.onNodeWithText("Too windy").assertIsDisplayed()
    }

    @Test
    fun writingAReviewAddsIt() {
        showSection(ed)

        composeTestRule.onNodeWithTag("writeReview").assertTextContains("Write a Review")
        click("writeReview")
        composeTestRule.onNodeWithTag("reviewSubmit").assertIsNotEnabled() // no stars yet
        click("ratingStar4")
        composeTestRule.onNodeWithTag("reviewSubmit").assertIsEnabled()
        composeTestRule.onNodeWithTag("reviewCommentInput").performTextInput("Great views")
        click("reviewSubmit")

        composeTestRule.onNodeWithTag("reviewSubmit").assertDoesNotExist()
        composeTestRule.onNodeWithText("Great views").assertIsDisplayed()
        assertEquals(4, stored.single().rating)
        assertEquals("user-1", stored.single().reviewerId)
    }

    @Test
    fun editingStartsFromTheOldReviewAndReplacesIt() {
        showSection(ed, review("user-1", 2, "Muddy"))

        composeTestRule.onNodeWithTag("writeReview").assertTextContains("Edit Your Review")
        click("writeReview")
        composeTestRule.onNodeWithTag("ratingStar2").assertIsSelected()
        composeTestRule.onNodeWithTag("ratingStar3").assertIsNotSelected()
        composeTestRule.onNodeWithTag("reviewCommentInput").assertTextContains("Muddy")
        click("ratingStar5")
        composeTestRule.onNodeWithTag("reviewCommentInput").performTextReplacement("Much better now")
        click("reviewSubmit")

        composeTestRule.onNodeWithText("Much better now").assertIsDisplayed()
        composeTestRule.onNodeWithText("Muddy").assertDoesNotExist()
        assertEquals(5, stored.single().rating)
    }

    @Test
    fun cancelledReviewIsNotSaved() {
        showSection(ed)

        click("writeReview")
        click("ratingStar1")
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("No reviews yet.").assertIsDisplayed()
        coVerify(exactly = 0) { repository.add(any()) }
    }

    @Test
    fun failedSaveKeepsTheReviewAndCanBeRetried() {
        showSection(ed)
        coEvery { repository.add(any()) } throws RuntimeException("offline") andThenAnswer {
            stored = listOf(firstArg())
        }

        click("writeReview")
        click("ratingStar3")
        composeTestRule.onNodeWithTag("reviewCommentInput").performTextInput("Nice")
        click("reviewSubmit")
        composeTestRule.onNodeWithTag("reviewError").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reviewCommentInput").assertTextContains("Nice")

        click("reviewSubmit")
        composeTestRule.onNodeWithTag("reviewSubmit").assertDoesNotExist()
        composeTestRule.onNodeWithText("Nice").assertIsDisplayed()
    }

    @Test
    fun commentStopsAtTheLimit() {
        showSection(ed)

        click("writeReview")
        composeTestRule.onNodeWithTag("reviewCommentInput")
            .performTextReplacement("a".repeat(Review.MAX_COMMENT_LENGTH + 20))

        composeTestRule.onNodeWithText("500/500").assertExists()
    }

    @Test
    fun ownerCannotReviewOrDeleteReviewsOfTheirCampsite() {
        showSection(CampsiteOwner(deviceId = "owner-1"), review("user-1", 1, "Bad"))

        composeTestRule.onNodeWithText("Bad").assertIsDisplayed()
        composeTestRule.onNodeWithTag("writeReview").assertDoesNotExist()
        composeTestRule.onNodeWithTag("deleteReview").assertDoesNotExist()
    }

    @Test
    fun usersOnlyDeleteTheirOwnReviews() {
        showSection(ed, review("user-1", 4, "Mine"), review("user-2", 3, "Theirs"))

        composeTestRule.onAllNodesWithTag("deleteReview").assertCountEquals(1)
        click("deleteReview")
        composeTestRule.onNodeWithText("Delete Review?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").performClick()

        composeTestRule.onNodeWithText("Mine").assertDoesNotExist()
        composeTestRule.onNodeWithText("Theirs").assertIsDisplayed()
    }

    @Test
    fun adminsDeleteAnyReview() {
        showSection(Admin(deviceId = "admin-1"), review("user-1", 4), review("user-2", 3))

        composeTestRule.onAllNodesWithTag("deleteReview").assertCountEquals(2)
    }

    @Test
    fun cancelledDeleteKeepsTheReview() {
        showSection(ed, review("user-1", 4, "Mine"))

        click("deleteReview")
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("Mine").assertIsDisplayed()
        coVerify(exactly = 0) { repository.delete(any<String>()) }
    }

    @Test
    fun failedDeleteShowsAnError() {
        showSection(ed, review("user-1", 4, "Mine"))
        coEvery { repository.delete(any<String>()) } throws RuntimeException("offline")

        click("deleteReview")
        composeTestRule.onNodeWithText("Delete").performClick()

        composeTestRule.onNodeWithText("Could not delete the review. Please try again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mine").assertIsDisplayed()
    }

    @Test
    fun onlyTheNewestReviewsShowUntilAskedForMore() {
        showSection(
            null,
            review("a", 5, "first", day = 1),
            review("b", 5, "second", day = 2),
            review("c", 5, "third", day = 3),
            review("d", 5, "fourth", day = 4)
        )

        composeTestRule.onAllNodesWithTag("reviewCard").assertCountEquals(3)
        composeTestRule.onNodeWithText("first").assertDoesNotExist()

        composeTestRule.onNodeWithTag("toggleAllReviews").performScrollTo().performClick()
        composeTestRule.onAllNodesWithTag("reviewCard").assertCountEquals(4)
        composeTestRule.onNodeWithText("first").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("toggleAllReviews").performScrollTo().performClick()
        composeTestRule.onAllNodesWithTag("reviewCard").assertCountEquals(3)
    }

    @Test
    fun reviewsFromOthersAppearByThemselves() {
        showSection(ed)

        composeTestRule.runOnIdle { stored = listOf(review("user-2", 5, "Just posted")) }

        composeTestRule.onNodeWithText("Just posted").assertIsDisplayed()
    }
}

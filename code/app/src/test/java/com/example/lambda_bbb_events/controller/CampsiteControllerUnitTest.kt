package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.local.InMemoryRecentlyViewedStore
import com.example.lambda_bbb_events.store.CampsiteStore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CampsiteControllerUnitTest {
    private val store = mockk<CampsiteStore>()
    private val campsites = mutableMapOf<String, Campsite>()
    private val savedViews = InMemoryRecentlyViewedStore()
    private val scope = TestScope()
    private lateinit var controller: CampsiteController

    private fun add(
        id: String,
        rating: Float = 0f,
        ratingCount: Int = 0,
        active: Boolean = true,
        createdAt: Long = 0L
    ) {
        campsites[id] = Campsite(
            campsiteId = id,
            rating = rating,
            ratingCount = ratingCount,
            active = active,
            createdAt = createdAt
        )
    }

    private fun List<Campsite>.ids() = map { it.campsiteId }

    @Before
    fun setup() {
        every { store.campsites } returns campsites
        controller = CampsiteController(store, savedViews, scope)
        scope.advanceUntilIdle()
    }

    @Test
    fun recentlyViewedInitiallyEmpty() {
        add("a")

        assertTrue(controller.recentlyViewed.isEmpty())
    }

    @Test
    fun recentlyViewedIsMostRecentFirstWithoutDuplicates() {
        add("a"); add("b"); add("c")

        controller.markViewed("a")
        controller.markViewed("b")
        controller.markViewed("c")
        controller.markViewed("a")

        assertEquals(listOf("a", "c", "b"), controller.recentlyViewed.ids())
    }

    @Test
    fun recentlyViewedIsCapped() {
        repeat(CampsiteController.MAX_RECENTLY_VIEWED + 2) { i ->
            add("site-$i")
            controller.markViewed("site-$i")
        }

        val viewed = controller.recentlyViewed.ids()
        assertEquals(CampsiteController.MAX_RECENTLY_VIEWED, viewed.size)
        assertEquals("site-${CampsiteController.MAX_RECENTLY_VIEWED + 1}", viewed.first())
        assertTrue("site-0" !in viewed)
    }

    @Test
    fun recentlyViewedSkipsDeletedCampsites() {
        add("a"); add("b")
        controller.markViewed("a")
        controller.markViewed("b")

        campsites.remove("a")

        assertEquals(listOf("b"), controller.recentlyViewed.ids())
    }

    @Test
    fun viewsAreSavedToStore() {
        add("a"); add("b")

        controller.markViewed("a")
        controller.markViewed("b")
        scope.advanceUntilIdle()

        assertEquals(listOf("b", "a"), savedViews.load(CampsiteController.MAX_RECENTLY_VIEWED))
    }

    @Test
    fun savedViewsAreLoadedOnStartup() {
        add("a"); add("b")
        val saved = InMemoryRecentlyViewedStore()
        saved.record("a", viewedAt = 1L, limit = 10)
        saved.record("b", viewedAt = 2L, limit = 10)

        val restarted = CampsiteController(store, saved, scope)
        scope.advanceUntilIdle()

        assertEquals(listOf("b", "a"), restarted.recentlyViewed.ids())
    }

    @Test
    fun viewsMadeWhileLoadingStayInFront() {
        add("a"); add("b"); add("c")
        val saved = InMemoryRecentlyViewedStore()
        saved.record("a", viewedAt = 1L, limit = 10)
        saved.record("b", viewedAt = 2L, limit = 10)

        val restarted = CampsiteController(store, saved, scope)
        restarted.markViewed("c")
        restarted.markViewed("a")
        scope.advanceUntilIdle()

        assertEquals(listOf("a", "c", "b"), restarted.recentlyViewed.ids())
    }

    @Test
    fun featuredCampsitesAreActiveAndSortedByTotalPoints() {
        add("great-few", rating = 4.5f, ratingCount = 2)    // 9 points
        add("great-many", rating = 4.5f, ratingCount = 30)  // 135 points
        add("good", rating = 4f, ratingCount = 10)          // 40 points
        add("inactive", rating = 5f, ratingCount = 100, active = false)

        assertEquals(listOf("great-many", "good", "great-few"), controller.featuredCampsites.ids())
    }

    @Test
    fun campsitesWithPositiveReviewsComeFirst() {
        add("popular-but-poor", rating = 2f, ratingCount = 500)  // 1000 points, not positive
        add("just-under", rating = 3.9f, ratingCount = 40)       // 156 points, not positive
        add("just-positive", rating = 4f, ratingCount = 1)       // 4 points, positive

        assertEquals(
            listOf("just-positive", "popular-but-poor", "just-under"),
            controller.featuredCampsites.ids()
        )
    }

    @Test
    fun featuredCampsitesWithEqualPointsPreferHigherRating() {
        add("many-good", rating = 4f, ratingCount = 10)     // 40 points
        add("few-great", rating = 5f, ratingCount = 8)      // 40 points

        assertEquals(listOf("few-great", "many-good"), controller.featuredCampsites.ids())
    }

    @Test
    fun unreviewedCampsitesComeAfterReviewedOnesNewestFirst() {
        add("old-unreviewed", createdAt = 1_000L)
        add("new-unreviewed", createdAt = 2_000L)
        add("reviewed", rating = 4.5f, ratingCount = 1)

        assertEquals(
            listOf("reviewed", "new-unreviewed", "old-unreviewed"),
            controller.featuredCampsites.ids()
        )
    }

    @Test
    fun withoutAnyReviewsFeaturedMatchesRecentlyAdded() {
        add("old", createdAt = 1_000L)
        add("newest", createdAt = 3_000L)
        add("middle", createdAt = 2_000L)

        assertEquals(controller.recentlyAddedCampsites.ids(), controller.featuredCampsites.ids())
    }

    @Test
    fun featuredCampsitesAreCapped() {
        repeat(CampsiteController.MAX_FEATURED + 3) { i -> add("site-$i", rating = 4.5f, ratingCount = i + 1) }

        assertEquals(CampsiteController.MAX_FEATURED, controller.featuredCampsites.size)
    }

    @Test
    fun recentlyAddedCampsitesAreActiveAndNewestFirst() {
        add("old", createdAt = 1_000L)
        add("newest", createdAt = 3_000L)
        add("middle", createdAt = 2_000L)
        add("hidden", createdAt = 4_000L, active = false)

        assertEquals(listOf("newest", "middle", "old"), controller.recentlyAddedCampsites.ids())
    }

    @Test
    fun campsitesWithoutCreationTimeComeLast() {
        add("from-before")
        add("new", createdAt = 5_000L)

        assertEquals(listOf("new", "from-before"), controller.recentlyAddedCampsites.ids())
    }

    @Test
    fun recentlyAddedCampsitesAreCapped() {
        repeat(CampsiteController.MAX_RECENTLY_ADDED + 3) { i -> add("site-$i", createdAt = i.toLong()) }

        assertEquals(CampsiteController.MAX_RECENTLY_ADDED, controller.recentlyAddedCampsites.size)
    }
}

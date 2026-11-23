package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.controller.CampsiteController
import com.example.lambda_bbb_events.model.Campsite
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

    private fun add(id: String, rating: Float = 0f, ratingCount: Int = 0, active: Boolean = true) {
        campsites[id] = Campsite(campsiteId = id, rating = rating, ratingCount = ratingCount, active = active)
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
    fun featuredCampsitesAreActiveAndSortedByRatingThenReviewCount() {
        add("low", rating = 2f, ratingCount = 50)
        add("high-few", rating = 4.5f, ratingCount = 2)
        add("high-many", rating = 4.5f, ratingCount = 30)
        add("inactive", rating = 5f, ratingCount = 100, active = false)

        assertEquals(listOf("high-many", "high-few", "low"), controller.featuredCampsites.ids())
    }

    @Test
    fun featuredCampsitesAreCapped() {
        repeat(CampsiteController.MAX_FEATURED + 3) { i -> add("site-$i", rating = i.toFloat()) }

        assertEquals(CampsiteController.MAX_FEATURED, controller.featuredCampsites.size)
    }
}

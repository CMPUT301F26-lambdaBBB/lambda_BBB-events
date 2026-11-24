package com.example.lambda_bbb_events.model.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryRecentlyViewedStoreUnitTest {
    private val store = InMemoryRecentlyViewedStore()

    @Test
    fun emptyStoreLoadsNothing() {
        assertTrue(store.load(10).isEmpty())
    }

    @Test
    fun loadsMostRecentFirst() {
        store.record("a", viewedAt = 1L, limit = 10)
        store.record("b", viewedAt = 3L, limit = 10)
        store.record("c", viewedAt = 2L, limit = 10)

        assertEquals(listOf("b", "c", "a"), store.load(10))
        assertEquals(listOf("b", "c"), store.load(2))
    }

    @Test
    fun recordingAgainUpdatesViewTime() {
        store.record("a", viewedAt = 1L, limit = 10)
        store.record("b", viewedAt = 2L, limit = 10)
        store.record("a", viewedAt = 3L, limit = 10)

        assertEquals(listOf("a", "b"), store.load(10))
    }

    @Test
    fun recordingDropsOldestViewsPastLimit() {
        store.record("a", viewedAt = 1L, limit = 2)
        store.record("b", viewedAt = 2L, limit = 2)
        store.record("c", viewedAt = 3L, limit = 2)

        assertEquals(listOf("c", "b"), store.load(10))
    }
}

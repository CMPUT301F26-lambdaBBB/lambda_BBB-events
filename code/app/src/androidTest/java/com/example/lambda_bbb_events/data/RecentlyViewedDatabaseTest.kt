package com.example.lambda_bbb_events.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.local.LocalDatabase
import com.example.lambda_bbb_events.model.local.SqliteRecentlyViewedStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Tests [SqliteRecentlyViewedStore] against a real SQLite database held in memory.
 */
@RunWith(AndroidJUnit4::class)
class RecentlyViewedDatabaseTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var database: LocalDatabase
    private lateinit var store: SqliteRecentlyViewedStore

    /** On-disk database files created by a test, deleted in [cleanUp]. */
    private val databaseFiles = mutableListOf<String>()

    @Before
    fun setUp() {
        database = LocalDatabase(context, name = null)
        store = SqliteRecentlyViewedStore(database)
    }

    @After
    fun cleanUp() {
        database.close()
        databaseFiles.forEach { context.deleteDatabase(it) }
    }

    @Test
    fun emptyDatabaseLoadsNothing() {
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
    fun recordingAgainUpdatesViewTimeWithoutDuplicating() {
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

    @Test
    fun viewsSurviveReopeningTheDatabase() {
        val name = "recently_viewed_test-${UUID.randomUUID()}.db"
        databaseFiles += name

        val firstOpen = LocalDatabase(context, name)
        SqliteRecentlyViewedStore(firstOpen).record("a", 1L, 10)
        firstOpen.close()

        val secondOpen = LocalDatabase(context, name)
        val loaded = SqliteRecentlyViewedStore(secondOpen).load(10)
        secondOpen.close()

        assertEquals(listOf("a"), loaded)
    }
}

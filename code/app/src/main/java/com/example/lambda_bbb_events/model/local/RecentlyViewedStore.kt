package com.example.lambda_bbb_events.model.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/*
 * Stores the campsites the user has viewed on this device.
 */

/**
 * Saves which campsites were viewed and when, so they survive app restarts.
 */
interface RecentlyViewedStore {
    /**
     * Loads the most recently viewed campsite IDs.
     *
     * @param limit The maximum number of IDs to load.
     * @return The campsite IDs, most recently viewed first.
     */
    fun load(limit: Int): List<String>

    /**
     * Records that a campsite was viewed, keeping only the [limit] most recent views.
     *
     * @param campsiteId The ID of the viewed campsite.
     * @param viewedAt When the campsite was viewed, in milliseconds since the epoch.
     * @param limit The maximum number of views to keep.
     */
    fun record(campsiteId: String, viewedAt: Long, limit: Int)
}

/**
 * Keeps recently viewed campsites in memory only. Used where no database is available.
 */
class InMemoryRecentlyViewedStore : RecentlyViewedStore {
    private val views = mutableMapOf<String, Long>()

    @Synchronized
    override fun load(limit: Int): List<String> =
        views.entries.sortedByDescending { it.value }.take(limit).map { it.key }

    @Synchronized
    override fun record(campsiteId: String, viewedAt: Long, limit: Int) {
        views[campsiteId] = viewedAt
        val kept = load(limit).toSet()
        views.keys.retainAll(kept)
    }
}

/**
 * The app's local SQLite database on this device.
 *
 * @param context The application context.
 * @param name The database file name, or `null` for a database held in memory.
 */
class LocalDatabase(
    context: Context,
    name: String? = DATABASE_NAME
) : SQLiteOpenHelper(context, name, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "lambda_local.db"
        const val DATABASE_VERSION = 1

        const val RECENTLY_VIEWED_TABLE = "recently_viewed"
        const val COLUMN_CAMPSITE_ID = "campsite_id"
        const val COLUMN_VIEWED_AT = "viewed_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $RECENTLY_VIEWED_TABLE (
                $COLUMN_CAMPSITE_ID TEXT PRIMARY KEY NOT NULL,
                $COLUMN_VIEWED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $RECENTLY_VIEWED_TABLE")
        onCreate(db)
    }
}

/**
 * Stores recently viewed campsites in the [LocalDatabase].
 *
 * Calls read and write the disk, so they should be made off the main thread.
 *
 * @param database The local database to store views in.
 */
class SqliteRecentlyViewedStore(
    private val database: LocalDatabase
) : RecentlyViewedStore {

    override fun load(limit: Int): List<String> {
        val ids = mutableListOf<String>()
        database.readableDatabase.query(
            LocalDatabase.RECENTLY_VIEWED_TABLE,
            arrayOf(LocalDatabase.COLUMN_CAMPSITE_ID),
            null,
            null,
            null,
            null,
            "${LocalDatabase.COLUMN_VIEWED_AT} DESC",
            limit.toString()
        ).use { cursor ->
            while (cursor.moveToNext()) {
                ids.add(cursor.getString(0))
            }
        }
        return ids
    }

    override fun record(campsiteId: String, viewedAt: Long, limit: Int) {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put(LocalDatabase.COLUMN_CAMPSITE_ID, campsiteId)
                put(LocalDatabase.COLUMN_VIEWED_AT, viewedAt)
            }
            db.insertWithOnConflict(
                LocalDatabase.RECENTLY_VIEWED_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )

            // Keep only the most recent views
            db.execSQL(
                """
                DELETE FROM ${LocalDatabase.RECENTLY_VIEWED_TABLE}
                WHERE ${LocalDatabase.COLUMN_CAMPSITE_ID} NOT IN (
                    SELECT ${LocalDatabase.COLUMN_CAMPSITE_ID}
                    FROM ${LocalDatabase.RECENTLY_VIEWED_TABLE}
                    ORDER BY ${LocalDatabase.COLUMN_VIEWED_AT} DESC
                    LIMIT ?
                )
                """.trimIndent(),
                arrayOf(limit)
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}

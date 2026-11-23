package com.example.lambda_bbb_events.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests that [FirestoreTestData] removes test data from the real Firestore database.
 */
class FirestoreTestDataTest {
    private val db = Firebase.firestore
    private lateinit var testData: FirestoreTestData

    /** Tracks documents the helper under test must leave alone, so they are still removed. */
    private lateinit var otherData: FirestoreTestData

    private fun exists(collection: String, id: String) = runBlocking {
        db.collection(collection).document(id).get().await().exists()
    }

    private fun write(collection: String, id: String) = runBlocking {
        db.collection(collection).document(id).set(mapOf("content" to "x")).await()
    }

    @Before
    fun setUp() {
        testData = FirestoreTestData(db)
        otherData = FirestoreTestData(db)
    }

    @After
    fun cleanUp() {
        try {
            testData.cleanUp()
        } finally {
            otherData.cleanUp()
        }
    }

    @Test
    fun cleanUpDeletesTrackedDocuments() {
        val first = testData.newId("messages")
        val second = testData.newId("messages")
        write("messages", first)
        write("messages", second)

        testData.cleanUp()

        assertFalse(exists("messages", first))
        assertFalse(exists("messages", second))
    }

    @Test
    fun cleanUpLeavesUntrackedDocuments() {
        val untracked = otherData.newId("messages", prefix = "test-untracked")
        write("messages", untracked)

        testData.cleanUp()

        assertTrue(exists("messages", untracked))
    }

    @Test
    fun cleanUpIgnoresIdsThatWereNeverWritten() {
        testData.newId("messages")

        testData.cleanUp()
    }
}

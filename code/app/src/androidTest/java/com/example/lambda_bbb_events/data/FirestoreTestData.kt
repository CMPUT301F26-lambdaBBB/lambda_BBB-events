package com.example.lambda_bbb_events.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Keeps track of what a test writes to the real Firestore database so it can be removed.
 *
 * Tests that use it follow the same order: set up in `@Before`, run and assert in the test,
 * then call [cleanUp] in `@After`. JUnit runs `@After` even when an assertion fails, so the
 * data is removed whether the test passed or not.
 *
 * @param db The database the tests write to.
 */
class FirestoreTestData(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    /** Paths of the documents to delete, such as "lotteries/test-lottery-1234". */
    private val documentPaths = mutableListOf<String>()

    /**
     * Creates a unique document ID and marks that document in [collection] for [cleanUp].
     *
     * @param collection The Firestore collection the document is written to.
     * @param prefix Start of the ID, to make leftover test data easy to recognize.
     * @return The new document ID.
     */
    fun newId(collection: String, prefix: String = "test"): String {
        val id = prefix + "-" + UUID.randomUUID()
        documentPaths.add("$collection/$id")
        return id
    }

    /**
     * Marks a document for [cleanUp] when its ID cannot be made by [newId]. The path must be
     * unique to the test, so nobody else's data is deleted.
     *
     * @param path The path of the document, such as "reviews/test-campsite-1234_test-user".
     */
    fun add(path: String) {
        documentPaths.add(path)
    }

    /**
     * Deletes every document created with [newId] or marked with [add].
     *
     * Documents are deleted directly, without the repositories' own delete functions, so
     * cleanup still works when related documents are missing. Throws if any delete fails,
     * naming the documents that were left behind.
     */
    fun cleanUp() {
        val failures = mutableListOf<String>()

        for (path in documentPaths) {
            try {
                runBlocking { db.document(path).delete().await() }
            } catch (e: Exception) {
                failures.add(path + ": " + e.message)
            }
        }
        documentPaths.clear()

        if (failures.isNotEmpty()) {
            throw AssertionError("Could not delete test data:\n" + failures.joinToString("\n"))
        }
    }
}

package com.example.lambda_bbb_events.model.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

/*
 * Shared helpers for unit testing repositories against a mocked Firestore.
 */

/**
 * Creates a [Task] that has already completed successfully with [value], so that
 * `await()` returns it immediately.
 */
fun <T> completedTask(value: T): Task<T> {
    val task = mockk<Task<T>>()
    every { task.isComplete } returns true
    every { task.isSuccessful } returns true
    every { task.isCanceled } returns false
    every { task.exception } returns null
    every { task.result } returns value
    return task
}

/**
 * Creates a [Task] that has already failed with [error], so that `await()` throws it.
 */
fun <T> failedTask(error: Exception): Task<T> {
    val task = mockk<Task<T>>()
    every { task.isComplete } returns true
    every { task.isSuccessful } returns false
    every { task.isCanceled } returns false
    every { task.exception } returns error
    return task
}

/**
 * Creates a [QuerySnapshot] containing [documents].
 */
fun querySnapshotOf(vararg documents: DocumentSnapshot): QuerySnapshot {
    val snapshot = mockk<QuerySnapshot>()
    every { snapshot.documents } returns documents.toList()
    return snapshot
}

/**
 * A mocked Firestore collection, with every document reference created on demand by
 * [document]. Writes and deletes succeed by default.
 */
class MockCollection(val db: FirebaseFirestore, name: String) {
    val ref = mockk<CollectionReference>()
    val listener: CapturingSlot<EventListener<QuerySnapshot>> = slot()
    val registration = mockk<ListenerRegistration>(relaxed = true)
    private val documents = mutableMapOf<String, DocumentReference>()

    init {
        every { db.collection(name) } returns ref
        every { ref.addSnapshotListener(capture(listener)) } returns registration
        every { ref.document(any<String>()) } answers { document(firstArg()) }
    }

    /**
     * Gets the mocked reference to the document with [id].
     */
    fun document(id: String): DocumentReference {
        val existing = documents[id]
        if (existing != null) {
            return existing
        }
        val doc = mockk<DocumentReference>()
        every { doc.id } returns id
        every { doc.set(any()) } returns completedTask(null)
        every { doc.delete() } returns completedTask(null)
        documents[id] = doc
        return doc
    }

    /**
     * Makes the snapshot listener report [documents].
     */
    fun emit(vararg documents: DocumentSnapshot) {
        listener.captured.onEvent(querySnapshotOf(*documents), null)
    }

    /**
     * Makes a read of the whole collection return [documents].
     */
    fun returnsOnGet(vararg documents: DocumentSnapshot) {
        every { ref.get() } returns completedTask(querySnapshotOf(*documents))
    }
}

/**
 * Creates a [DocumentSnapshot] with [id] that deserializes into [value] as [type].
 */
fun <T : Any> documentOf(id: String, type: Class<T>, value: T?): DocumentSnapshot {
    val document = mockk<DocumentSnapshot>(relaxed = true)
    every { document.id } returns id
    every { document.toObject(type) } returns value
    return document
}

package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Base class for every repository backed by a single Firestore collection.
 *
 * Each item of type [T] is stored as one document whose ID is [idOf] the item. Subclasses
 * describe how to identify an item and how to convert it to and from a Firestore document;
 * this class handles the reads, writes, and a local cache of the collection.
 *
 * The cache is updated by every write made through this repository, by [refresh], and by
 * the snapshot listener started with [startListening].
 *
 * @param T The model class stored in the collection.
 * @param collectionName The name of the Firestore collection.
 * @param db The Firestore instance to use.
 */
abstract class Repository<T : Any>(
    collectionName: String,
    protected val db: FirebaseFirestore = Firebase.firestore
) {
    protected val collectionRef: CollectionReference = db.collection(collectionName)

    private val _cache = mutableStateListOf<T>()
    private var listenerRegistration: ListenerRegistration? = null

    /** Cached items, synchronized with Firestore. */
    val cached: List<T>
        get() = synchronized(_cache) { _cache.toList() }

    /**
     * Gets the document ID of an item.
     *
     * @param item The item to identify.
     * @return The ID of the document the item is stored in.
     */
    protected abstract fun idOf(item: T): String

    /**
     * Converts a Firestore document into an item.
     *
     * @param document The document to convert.
     * @return The item, or `null` if the document does not describe a valid item.
     */
    protected abstract fun fromDocument(document: DocumentSnapshot): T?

    /**
     * Converts an item into the object written to Firestore. Defaults to the item itself.
     *
     * @param item The item to convert.
     * @return The object to store in the item's document.
     */
    protected open fun toDocument(item: T): Any = item

    /**
     * Called when the snapshot listener reports an error. The cache is left unchanged.
     *
     * @param error The error reported by Firestore.
     */
    protected open fun onListenError(error: FirebaseFirestoreException) {}

    /**
     * Adds an item to Firestore, replacing any existing document with the same ID.
     *
     * @param item The item to add.
     * @throws IllegalArgumentException if the item's ID is blank.
     */
    open suspend fun add(item: T) {
        val id = idOf(item)
        if (id.isBlank()) {
            throw IllegalArgumentException("Cannot store an item with a blank ID")
        }
        collectionRef.document(id).set(toDocument(item)).await()
        putInCache(item)
    }

    /**
     * Saves an item to Firestore, creating it if absent.
     *
     * @param item The item data to save.
     * @throws IllegalArgumentException if the item's ID is blank.
     */
    open suspend fun update(item: T) = add(item)

    /**
     * Deletes the document with the given ID from Firestore.
     *
     * @param id The ID of the item to delete.
     */
    open suspend fun delete(id: String) {
        collectionRef.document(id).delete().await()
        removeFromCache(id)
    }

    /**
     * Deletes an item from Firestore.
     *
     * @param item The item to delete.
     */
    open suspend fun delete(item: T) = delete(idOf(item))

    /**
     * Reads an item from Firestore.
     *
     * @param id The ID of the item to get.
     * @return The item if it exists and is valid. `null` otherwise, or if [id] is blank.
     */
    open suspend fun get(id: String): T? {
        if (id.isBlank()) {
            return null
        }
        return convert(collectionRef.document(id).get().await())
    }

    /**
     * Reads every item in the collection from Firestore. Documents that cannot be
     * converted are skipped.
     *
     * @return The list of items in the collection.
     */
    open suspend fun getAll(): List<T> {
        val snapshot = collectionRef.get().await()
        return convertAll(snapshot.documents)
    }

    /**
     * Gets an item from the cache without reading Firestore.
     *
     * @param id The ID of the item to get.
     * @return The cached item, or `null` if it is not cached.
     */
    fun getCached(id: String): T? = cached.find { idOf(it) == id }

    /**
     * Replaces the cache with every item currently in Firestore.
     */
    open suspend fun refresh() {
        replaceCache(getAll())
    }

    /**
     * Starts keeping the cache synchronized with Firestore, including changes made on other
     * devices or directly in the database. Does nothing if already listening.
     *
     * @param onChange Called with every item each time the cache is synchronized.
     */
    fun startListening(onChange: (List<T>) -> Unit = {}) {
        if (listenerRegistration != null) {
            return
        }
        listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onListenError(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = convertAll(snapshot.documents)
                replaceCache(items)
                onChange(items)
            }
        }
    }

    /**
     * Stops the snapshot listener started by [startListening].
     */
    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    /**
     * Inserts an item into the cache, replacing any cached item with the same ID.
     */
    protected fun putInCache(item: T) {
        val id = idOf(item)
        synchronized(_cache) {
            val index = _cache.indexOfFirst { idOf(it) == id }
            if (index >= 0) {
                _cache[index] = item
            } else {
                _cache.add(item)
            }
        }
    }

    /**
     * Removes the item with the given ID from the cache.
     */
    protected fun removeFromCache(id: String) {
        synchronized(_cache) {
            _cache.removeAll { idOf(it) == id }
        }
    }

    private fun replaceCache(items: List<T>) {
        synchronized(_cache) {
            _cache.clear()
            _cache.addAll(items)
        }
    }

    /**
     * Converts a document into an item, or returns `null` if it is not a valid item.
     */
    private fun convert(document: DocumentSnapshot): T? {
        try {
            return fromDocument(document)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Converts every document into an item, skipping documents that are not valid items.
     */
    private fun convertAll(documents: List<DocumentSnapshot>): List<T> {
        val items = mutableListOf<T>()
        for (document in documents) {
            val item = convert(document)
            if (item != null) {
                items.add(item)
            }
        }
        return items
    }
}

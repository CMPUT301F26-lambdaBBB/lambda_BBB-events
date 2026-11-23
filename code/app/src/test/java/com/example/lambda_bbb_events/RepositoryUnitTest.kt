package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.repository.Repository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests the shared behaviour of [Repository] through a minimal subclass.
 */
class RepositoryUnitTest {

    data class Item(val id: String = "", val name: String = "")

    /** Stores [Item]s as `{name}` documents. Documents named "bad" fail to convert. */
    class ItemRepository(db: FirebaseFirestore) : Repository<Item>("items", db) {
        var lastListenError: FirebaseFirestoreException? = null

        override fun idOf(item: Item) = item.id

        override fun fromDocument(document: DocumentSnapshot): Item? {
            val name = document.getString("name") ?: return null
            check(name != "bad") { "Malformed item" }
            return Item(document.id, name)
        }

        override fun toDocument(item: Item): Any = mapOf("name" to item.name)

        override fun onListenError(error: FirebaseFirestoreException) {
            lastListenError = error
        }
    }

    private val db = mockk<FirebaseFirestore>()
    private lateinit var items: MockCollection
    private lateinit var repository: ItemRepository

    private fun itemDocument(id: String, name: String?): DocumentSnapshot {
        val document = mockk<DocumentSnapshot>(relaxed = true)
        every { document.id } returns id
        every { document.getString("name") } returns name
        return document
    }

    @Before
    fun setup() {
        items = MockCollection(db, "items")
        repository = ItemRepository(db)
    }

    @Test
    fun cacheInitiallyEmpty() {
        assertTrue(repository.cached.isEmpty())
    }

    @Test
    fun addWritesDocumentAtItemIdAndCachesIt() = runTest {
        repository.add(Item("a", "Alpha"))

        verify(exactly = 1) { items.document("a").set(mapOf("name" to "Alpha")) }
        assertEquals(listOf(Item("a", "Alpha")), repository.cached)
    }

    @Test
    fun addWithBlankIdThrowsWithoutWriting() = runTest {
        val error = runCatching { repository.add(Item(" ", "Blank")) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)

        verify(exactly = 0) { items.ref.document(any<String>()) }
        assertTrue(repository.cached.isEmpty())
    }

    @Test
    fun failedWriteDoesNotChangeCache() = runTest {
        every { items.document("a").set(any()) } returns failedTask(IllegalStateException("offline"))

        val error = runCatching { repository.add(Item("a", "Alpha")) }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(repository.cached.isEmpty())
    }

    @Test
    fun updateReplacesCachedItemWithSameId() = runTest {
        repository.add(Item("a", "Alpha"))
        repository.add(Item("b", "Beta"))

        repository.update(Item("a", "Alpha 2"))

        verify(exactly = 1) { items.document("a").set(mapOf("name" to "Alpha 2")) }
        assertEquals(listOf(Item("a", "Alpha 2"), Item("b", "Beta")), repository.cached)
    }

    @Test
    fun deleteByIdRemovesDocumentAndCachedItem() = runTest {
        repository.add(Item("a", "Alpha"))
        repository.add(Item("b", "Beta"))

        repository.delete("a")

        verify(exactly = 1) { items.document("a").delete() }
        assertEquals(listOf(Item("b", "Beta")), repository.cached)
    }

    @Test
    fun deleteByItemUsesItsId() = runTest {
        repository.add(Item("a", "Alpha"))

        repository.delete(Item("a", "Alpha"))

        verify(exactly = 1) { items.document("a").delete() }
        assertTrue(repository.cached.isEmpty())
    }

    @Test
    fun getReturnsConvertedDocument() = runTest {
        val document = itemDocument("a", "Alpha")
        every { items.document("a").get() } returns completedTask(document)

        assertEquals(Item("a", "Alpha"), repository.get("a"))
    }

    @Test
    fun getWithBlankIdReturnsNullWithoutReading() = runTest {
        assertNull(repository.get(""))

        verify(exactly = 0) { items.ref.document(any<String>()) }
    }

    @Test
    fun getReturnsNullForMissingDocument() = runTest {
        val document = itemDocument("missing", null)
        every { items.document("missing").get() } returns completedTask(document)

        assertNull(repository.get("missing"))
    }

    @Test
    fun getReturnsNullWhenConversionFails() = runTest {
        val document = itemDocument("a", "bad")
        every { items.document("a").get() } returns completedTask(document)

        assertNull(repository.get("a"))
    }

    @Test
    fun getAllSkipsDocumentsThatCannotBeConverted() = runTest {
        items.returnsOnGet(
            itemDocument("a", "Alpha"),
            itemDocument("b", "bad"),
            itemDocument("c", null),
            itemDocument("d", "Delta")
        )

        assertEquals(listOf(Item("a", "Alpha"), Item("d", "Delta")), repository.getAll())
    }

    @Test
    fun refreshReplacesCache() = runTest {
        repository.add(Item("stale", "Stale"))
        items.returnsOnGet(itemDocument("a", "Alpha"))

        repository.refresh()

        assertEquals(listOf(Item("a", "Alpha")), repository.cached)
    }

    @Test
    fun getCachedFindsItemById() = runTest {
        repository.add(Item("a", "Alpha"))

        assertEquals(Item("a", "Alpha"), repository.getCached("a"))
        assertNull(repository.getCached("b"))
    }

    @Test
    fun listenerReplacesCacheOnEverySnapshot() {
        repository.startListening()

        items.emit(itemDocument("a", "Alpha"), itemDocument("b", "Beta"))
        assertEquals(listOf(Item("a", "Alpha"), Item("b", "Beta")), repository.cached)

        items.emit(itemDocument("b", "Beta"), itemDocument("x", "bad"))
        assertEquals(listOf(Item("b", "Beta")), repository.cached)
    }

    @Test
    fun listenerErrorKeepsCacheAndIsReported() {
        repository.startListening()
        items.emit(itemDocument("a", "Alpha"))
        val error = mockk<FirebaseFirestoreException>()

        items.listener.captured.onEvent(null, error)

        assertSame(error, repository.lastListenError)
        assertEquals(listOf(Item("a", "Alpha")), repository.cached)
    }

    @Test
    fun startListeningOnlyRegistersOnce() {
        repository.startListening()
        repository.startListening()

        verify(exactly = 1) { items.ref.addSnapshotListener(any()) }
    }

    @Test
    fun stopListeningRemovesRegistrationAndAllowsRestart() {
        repository.startListening()
        repository.stopListening()

        verify(exactly = 1) { items.registration.remove() }

        repository.startListening()
        verify(exactly = 2) { items.ref.addSnapshotListener(any()) }
    }

    @Test
    fun observeAllEmitsConvertedItemsAndRemovesListenerWhenDone() = runTest {
        every { items.ref.addSnapshotListener(capture(items.listener)) } answers {
            items.emit(itemDocument("a", "Alpha"), itemDocument("b", "bad"))
            items.registration
        }

        assertEquals(listOf(Item("a", "Alpha")), repository.observeAll().first())
        verify(exactly = 1) { items.registration.remove() }
    }
}

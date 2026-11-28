package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Verifies waitlist entry repository behavior with mocked persistence. */
class WaitlistEntryRepositoryUnitTest {
    private val db = mockk<FirebaseFirestore>()
    private lateinit var entries: MockCollection
    private lateinit var lotteries: MockCollection
    private lateinit var repository: WaitlistEntryRepository
    private val batch = mockk<WriteBatch>(relaxed = true)

    private val entryA = WaitlistEntry(entryId = "a", entrantId = "ed", lotteryId = "lottery-1")
    private val entryB = WaitlistEntry(entryId = "b", entrantId = "ed", lotteryId = "lottery-2")
    private val entryC = WaitlistEntry(entryId = "c", entrantId = "flo", lotteryId = "lottery-1")

    @Before
    fun setup() {
        entries = MockCollection(db, "waitlistEntries")
        lotteries = MockCollection(db, "lotteries")
        every { db.batch() } returns batch
        every { batch.commit() } returns completedTask(null)
        repository = WaitlistEntryRepository(db)
    }

    private fun emitAll() = entries.emit(
        documentOf("a", WaitlistEntry::class.java, entryA),
        documentOf("b", WaitlistEntry::class.java, entryB),
        documentOf("c", WaitlistEntry::class.java, entryC)
    )

    @Test
    fun entriesAreFilteredByLotteryAndEntrant() {
        emitAll()

        assertEquals(listOf(entryA, entryB, entryC), repository.waitlistEntries)
        assertEquals(listOf(entryA, entryC), repository.getEntries("lottery-1"))
        assertEquals(listOf(entryA, entryB), repository.getEntriesForEntrant("ed"))
        assertTrue(repository.getEntries("lottery-3").isEmpty())
    }

    @Test
    fun deleteWaitlistEntryAlsoRemovesItFromLottery() = runTest {
        emitAll()

        repository.deleteWaitlistEntry(entryA)

        verifyOrder {
            batch.delete(entries.document("a"))
            batch.update(lotteries.document("lottery-1"), "entryIds", any())
            batch.commit()
        }
        assertEquals(listOf(entryB, entryC), repository.waitlistEntries)
    }

    @Test
    fun deletingEntryWithoutLotteryOnlyDeletesEntry() = runTest {
        repository.deleteWaitlistEntry(WaitlistEntry(entryId = "orphan"))

        verifyOrder {
            batch.delete(entries.document("orphan"))
            batch.commit()
        }
        verify(exactly = 0) { batch.update(any<com.google.firebase.firestore.DocumentReference>(), any<String>(), any()) }
    }

    @Test
    fun deleteByIdLooksUpEntryToRemoveItFromLottery() = runTest {
        emitAll()

        repository.delete("c")

        verify(exactly = 1) {
            batch.update(lotteries.document("lottery-1"), "entryIds", any())
        }
    }

    @Test
    fun deleteByIdOfUnknownEntryDeletesDocumentOnly() = runTest {
        every { entries.document("ghost").get() } returns
            completedTask(documentOf("ghost", WaitlistEntry::class.java, null))

        repository.delete("ghost")

        verify(exactly = 1) { entries.document("ghost").delete() }
        verify(exactly = 0) { batch.commit() }
    }

    @Test
    fun getWaitlistEntryWithBlankIdReturnsNull() = runTest {
        assertEquals(null, repository.getWaitlistEntry(""))
    }

    @Test
    fun updateEntryStatusWritesStatusName() = runTest {
        every { entries.document("a").update("status", "WON") } returns completedTask(null)

        repository.updateEntryStatus(entryA, WaitlistStatus.WON)

        verify(exactly = 1) { entries.document("a").update("status", "WON") }
    }
}

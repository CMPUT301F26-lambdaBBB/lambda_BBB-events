package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.model.message.Message
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LotteryRepositoryUnitTest {
    private val db = mockk<FirebaseFirestore>()
    private lateinit var lotteries: MockCollection
    private lateinit var repository: LotteryRepository

    private val lottery = Lottery(
        lotteryId = "lottery-1",
        campsiteIds = listOf("site-1"),
        waitlistCapacity = 2,
        entryIds = listOf("entry-1")
    )

    @Before
    fun setup() {
        lotteries = MockCollection(db, "lotteries")
        repository = LotteryRepository(db)
    }

    @Test
    fun repositoryListensOnCreation() {
        verify(exactly = 1) { lotteries.ref.addSnapshotListener(any()) }
    }

    @Test
    fun getLotteriesForCampsiteFiltersCache() {
        val other = Lottery(lotteryId = "lottery-2", campsiteIds = listOf("site-2"))
        lotteries.emit(
            documentOf("lottery-1", Lottery::class.java, lottery),
            documentOf("lottery-2", Lottery::class.java, other)
        )

        assertEquals(listOf(lottery, other), repository.lotteries)
        assertEquals(listOf(lottery), repository.getLotteriesForCampsite("site-1"))
        assertTrue(repository.getLotteriesForCampsite("site-3").isEmpty())
    }

    @Test
    fun addWaitlistToFullLotteryFails() = runTest {
        val full = lottery.copy(entryIds = listOf("entry-1", "entry-2"))

        assertFalse(repository.addWaitlistToLottery(full, WaitlistEntry(entryId = "entry-3")))
        verify(exactly = 0) { lotteries.document("lottery-1").update(any<String>(), any()) }
    }

    @Test
    fun addDuplicateWaitlistEntryFails() = runTest {
        assertFalse(repository.addWaitlistToLottery(lottery, WaitlistEntry(entryId = "entry-1")))
        verify(exactly = 0) { lotteries.document("lottery-1").update(any<String>(), any()) }
    }

    @Test
    fun addWaitlistEntryUpdatesEntryIds() = runTest {
        every { lotteries.document("lottery-1").update("entryIds", any()) } returns completedTask(null)

        assertTrue(repository.addWaitlistToLottery(lottery, WaitlistEntry(entryId = "entry-2")))
        verify(exactly = 1) { lotteries.document("lottery-1").update("entryIds", any()) }
    }

    @Test
    fun deleteLotteryRemovesEntriesAndUserReferencesInOneBatch() = runTest {
        val entryRef = mockk<DocumentReference>()
        val entryDoc = mockk<DocumentSnapshot>()
        every { entryDoc.id } returns "entry-1"
        every { entryDoc.reference } returns entryRef

        val userRef = mockk<DocumentReference>()
        val userDoc = mockk<DocumentSnapshot>()
        every { userDoc.reference } returns userRef

        val entriesRef = mockk<CollectionReference>()
        val entriesQuery = mockk<Query>()
        every { db.collection("waitlistEntries") } returns entriesRef
        every { entriesRef.whereEqualTo("lotteryId", "lottery-1") } returns entriesQuery
        every { entriesQuery.get() } returns completedTask(querySnapshotOf(entryDoc))

        val usersRef = mockk<CollectionReference>()
        val usersQuery = mockk<Query>()
        every { db.collection("users") } returns usersRef
        every { usersRef.whereArrayContains("waitlistEntryIds", "entry-1") } returns usersQuery
        every { usersQuery.get() } returns completedTask(querySnapshotOf(userDoc))

        val batch = mockk<WriteBatch>(relaxed = true)
        every { db.batch() } returns batch
        every { batch.commit() } returns completedTask(null)

        repository.addLottery(lottery)
        repository.deleteLottery(lottery)

        verifyOrder {
            batch.update(userRef, "waitlistEntryIds", any())
            batch.delete(entryRef)
            batch.delete(lotteries.document("lottery-1"))
            batch.commit()
        }
        assertTrue(repository.lotteries.isEmpty())
    }

    @Test
    fun deleteAndNotifyWinnersMessagesOnlyWinnersInTheSameBatch() = runTest {
        val entries = listOf(
            WaitlistEntry(entryId = "entry-won", entrantId = "won", lotteryId = "lottery-1", status = WaitlistStatus.WON),
            WaitlistEntry(entryId = "entry-accepted", entrantId = "accepted", lotteryId = "lottery-1", status = WaitlistStatus.ACCEPTED),
            WaitlistEntry(entryId = "entry-lost", entrantId = "lost", lotteryId = "lottery-1", status = WaitlistStatus.LOST),
            WaitlistEntry(entryId = "entry-waiting", entrantId = "waiting", lotteryId = "lottery-1")
        )
        val entryDocs = entries.map { entry ->
            documentOf(entry.entryId, WaitlistEntry::class.java, entry)
        }

        val entriesRef = mockk<CollectionReference>()
        val entriesQuery = mockk<Query>()
        every { db.collection("waitlistEntries") } returns entriesRef
        every { entriesRef.whereEqualTo("lotteryId", "lottery-1") } returns entriesQuery
        every { entriesQuery.get() } returns completedTask(querySnapshotOf(*entryDocs.toTypedArray()))

        val usersRef = mockk<CollectionReference>()
        val usersQuery = mockk<Query>()
        every { db.collection("users") } returns usersRef
        every { usersRef.whereArrayContains("waitlistEntryIds", any()) } returns usersQuery
        every { usersQuery.get() } returns completedTask(querySnapshotOf())

        val messages = MockCollection(db, "messages")
        val batch = mockk<WriteBatch>(relaxed = true)
        every { db.batch() } returns batch
        every { batch.commit() } returns completedTask(null)

        val sent = mutableListOf<Message>()
        val notified = repository.deleteAndNotifyWinners("lottery-1") { entry ->
            Message(messageId = "msg-${entry.entrantId}", recipientId = entry.entrantId)
                .also { sent.add(it) }
        }

        assertEquals(2, notified)
        assertEquals(listOf("won", "accepted"), sent.map { it.recipientId })
        verifyOrder {
            batch.set(messages.document("msg-won"), sent[0])
            batch.set(messages.document("msg-accepted"), sent[1])
            batch.delete(lotteries.document("lottery-1"))
            batch.commit()
        }
    }
}

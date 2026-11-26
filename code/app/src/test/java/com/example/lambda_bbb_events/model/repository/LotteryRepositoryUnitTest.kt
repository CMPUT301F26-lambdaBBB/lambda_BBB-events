package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.model.Message
import com.example.lambda_bbb_events.model.reservation.Reservation
import com.example.lambda_bbb_events.model.reservation.ReservationStatus
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
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
import java.time.LocalDate

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

    /**
     * Makes the server read of the lottery being deleted find nothing, so no messaging
     * access needs revoking.
     */
    private fun lotteryIsGoneOnServer() {
        every { lotteries.document("lottery-1").get(Source.SERVER) } returns
                completedTask(documentOf("lottery-1", Lottery::class.java, null))
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
        every { entriesQuery.get(Source.SERVER) } returns completedTask(querySnapshotOf(entryDoc))
        lotteryIsGoneOnServer()

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
        every { entriesQuery.get(Source.SERVER) } returns completedTask(querySnapshotOf(*entryDocs.toTypedArray()))
        lotteryIsGoneOnServer()

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

    /**
     * Sets up deleting lottery-1 at site-1 with one waitlist entry from "ed", whose
     * reservations are [reservations], and returns ed's messaging access document for site-1.
     */
    private fun deletingLotteryWithEntrant(reservations: List<Reservation>): DocumentReference {
        val entryDoc = documentOf("entry-1", WaitlistEntry::class.java, null)
        every { entryDoc.getString("entrantId") } returns "ed"

        val entriesRef = mockk<CollectionReference>()
        val byLottery = mockk<Query>()
        val byEntrant = mockk<Query>()
        every { db.collection("waitlistEntries") } returns entriesRef
        every { entriesRef.whereEqualTo("lotteryId", "lottery-1") } returns byLottery
        every { byLottery.get(Source.SERVER) } returns completedTask(querySnapshotOf(entryDoc))
        every { entriesRef.whereEqualTo("entrantId", "ed") } returns byEntrant
        every { byEntrant.get(Source.SERVER) } returns completedTask(objectsSnapshot(WaitlistEntry::class.java, emptyList()))

        val usersRef = mockk<CollectionReference>()
        val usersQuery = mockk<Query>()
        every { db.collection("users") } returns usersRef
        every { usersRef.whereArrayContains("waitlistEntryIds", any()) } returns usersQuery
        every { usersQuery.get() } returns completedTask(querySnapshotOf())

        val reservationsRef = mockk<CollectionReference>()
        val reservationsQuery = mockk<Query>()
        every { db.collection("reservations") } returns reservationsRef
        every { reservationsRef.whereEqualTo("entrantId", "ed") } returns reservationsQuery
        every { reservationsQuery.get(Source.SERVER) } returns completedTask(objectsSnapshot(Reservation::class.java, reservations))

        every { lotteries.document("lottery-1").get(Source.SERVER) } returns
                completedTask(documentOf("lottery-1", Lottery::class.java, lottery))

        val accessRef = mockk<DocumentReference>()
        val accessDoc = mockk<DocumentSnapshot>()
        every { accessDoc.exists() } returns true
        every { accessRef.get(Source.SERVER) } returns completedTask(accessDoc)
        val accessRoot = mockk<CollectionReference>()
        val edAccess = mockk<DocumentReference>()
        val edCampsites = mockk<CollectionReference>()
        every { db.collection("messagingAccess") } returns accessRoot
        every { accessRoot.document("ed") } returns edAccess
        every { edAccess.collection("campsites") } returns edCampsites
        every { edCampsites.document("site-1") } returns accessRef

        val batch = mockk<WriteBatch>(relaxed = true)
        every { db.batch() } returns batch
        every { batch.commit() } returns completedTask(null)
        batchInUse = batch

        return accessRef
    }

    /** The batch the deletion set up by [deletingLotteryWithEntrant] writes to. */
    private lateinit var batchInUse: WriteBatch

    /** Creates a query snapshot that converts into [items] of [type]. */
    private fun <T : Any> objectsSnapshot(type: Class<T>, items: List<T>): QuerySnapshot {
        val snapshot = querySnapshotOf()
        every { snapshot.toObjects(type) } returns items
        return snapshot
    }

    @Test
    fun deletingTheOnlyEligibleLotteryRevokesOwnerMessagingAccess() = runTest {
        val accessRef = deletingLotteryWithEntrant(reservations = emptyList())

        repository.deleteAndNotifyWinners("lottery-1", makeNotice = null)

        verifyOrder {
            batchInUse.update(accessRef, "active", false)
            batchInUse.delete(lotteries.document("lottery-1"))
            batchInUse.commit()
        }
    }

    @Test
    fun deletingALotteryKeepsAccessWhenAReservationIsStillActive() = runTest {
        val upcoming = Reservation(
            campsiteId = "site-1",
            entrantId = "ed",
            endDate = LocalDate.now().plusDays(3).toString(),
            status = ReservationStatus.CONFIRMED
        )
        val accessRef = deletingLotteryWithEntrant(reservations = listOf(upcoming))

        repository.deleteAndNotifyWinners("lottery-1", makeNotice = null)

        verify(exactly = 0) { batchInUse.update(accessRef, "active", false) }
        verify(exactly = 1) { batchInUse.commit() }
    }
}

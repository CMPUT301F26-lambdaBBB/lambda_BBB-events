package com.example.lambda_bbb_events.data

import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.model.repository.WaitlistEntryRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Tests [WaitlistEntryRepository] against the real Firestore database.
 *
 * Every entry uses a unique ID and is deleted after each test in [cleanUp].
 */
class WaitlistEntryRepositoryTests {
    private val testData = FirestoreTestData()
    private lateinit var repository: WaitlistEntryRepository

    @Before
    fun setUp() {
        repository = WaitlistEntryRepository()
    }

    @After
    fun cleanUp() {
        repository.stopListening()
        testData.cleanUp()
    }

    private fun newEntryId() = testData.newId("waitlistEntries", prefix = "test-entry")

    @Test
    fun addingEntryAndGettingTheEntry() = runTest {
        val entry = WaitlistEntry(entryId = newEntryId(), numberOfGuests = 2)

        repository.addWaitlistEntry(entry)

        assertEquals(entry, repository.getWaitlistEntry(entry.entryId))
    }

    @Test
    fun gettingAnEntryThatDoesNotExistReturnsNull() = runTest {
        assertNull(repository.getWaitlistEntry("thisEntryDoesntExist!!"))
    }

    @Test
    fun deletingEntry() = runTest {
        val entry = WaitlistEntry(entryId = newEntryId())

        repository.addWaitlistEntry(entry)
        repository.deleteWaitlistEntry(entry)

        assertNull(repository.getWaitlistEntry(entry.entryId))
    }

    @Test
    fun updatingEntry() = runTest {
        val entry = WaitlistEntry(entryId = newEntryId(), numberOfGuests = 1)
        val updatedEntry = entry.copy(numberOfGuests = 4)

        repository.addWaitlistEntry(entry)
        repository.updateWaitlistEntry(updatedEntry)

        assertEquals(4, repository.getWaitlistEntry(entry.entryId)?.numberOfGuests)
    }

    @Test
    fun updatingEntryStatus() = runTest {
        val entry = WaitlistEntry(entryId = newEntryId(), status = WaitlistStatus.IN_WAITLIST)
        val newStatus = WaitlistStatus.WON

        repository.addWaitlistEntry(entry)
        repository.updateEntryStatus(entry, newStatus)

        assertEquals(newStatus, repository.getWaitlistEntry(entry.entryId)?.status)
    }

    @Test
    fun gettingEntriesOnlyReturnsEntriesForThatLottery() = runTest {
        val lotteryId = "test-lottery-${UUID.randomUUID()}"
        val entryInLottery = WaitlistEntry(entryId = newEntryId(), lotteryId = lotteryId)
        val entryInOtherLottery = WaitlistEntry(entryId = newEntryId(), lotteryId = "otherLottery")

        repository.addWaitlistEntry(entryInLottery)
        repository.addWaitlistEntry(entryInOtherLottery)

        val resultIds = repository.getEntries(lotteryId).map { it.entryId }

        assertTrue(entryInLottery.entryId in resultIds)
        assertFalse(entryInOtherLottery.entryId in resultIds)
    }

    @Test
    fun gettingEntriesForEntrantOnlyReturnsEntriesForThatEntrant() = runTest {
        val entrantId = "test-entrant-${UUID.randomUUID()}"
        val entryForEntrant = WaitlistEntry(entryId = newEntryId(), entrantId = entrantId)
        val entryForSomeoneElse = WaitlistEntry(entryId = newEntryId(), entrantId = "otherEntrant")

        repository.addWaitlistEntry(entryForEntrant)
        repository.addWaitlistEntry(entryForSomeoneElse)

        val resultIds = repository.getEntriesForEntrant(entrantId).map { it.entryId }

        assertTrue(entryForEntrant.entryId in resultIds)
        assertFalse(entryForSomeoneElse.entryId in resultIds)
    }
}

package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.model.WaitlistStatus
import com.example.lambda_bbb_events.model.repository.WaitlistEntryRepository
import com.example.lambda_bbb_events.model.roles.Entrant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WaitlistEntryRepositoryTests {
    @Test
    fun addingEntryAndGettingTheEntry() = runTest {
        val entry = WaitlistEntry(entryId = "testEntry", numberOfGuests = 2)
        val repository = WaitlistEntryRepository()

        repository.addWaitlistEntry(entry)

        assertEquals(entry, repository.getWaitlistEntry("testEntry"))
    }

    @Test
    fun gettingAnEntryThatDoesNotExistReturnsNull() = runTest {
        val repository = WaitlistEntryRepository()

        assertNull(repository.getWaitlistEntry("thisEntryDoesntExist!!"))
    }

    @Test
    fun deletingEntry() = runTest {
        val entry = WaitlistEntry(entryId = "testEntry")
        val repository = WaitlistEntryRepository()

        repository.addWaitlistEntry(entry)
        repository.deleteWaitlistEntry(entry)

        assertNull(repository.getWaitlistEntry("testEntry"))
    }

    @Test
    fun updatingEntry() = runTest {
        val entry = WaitlistEntry(entryId = "testEntry", numberOfGuests = 1)
        val updatedEntry = entry.copy(numberOfGuests = 4)
        val repository = WaitlistEntryRepository()

        repository.addWaitlistEntry(entry)
        repository.updateWaitlistEntry(updatedEntry)

        assertEquals(4, repository.getWaitlistEntry("testEntry")?.numberOfGuests)
    }

    @Test
    fun updatingEntryStatus() = runTest {
        val entry = WaitlistEntry(entryId = "testEntry", status = WaitlistStatus.IN_WAITLIST)
        val newStatus = WaitlistStatus.WON
        val repository = WaitlistEntryRepository()

        repository.addWaitlistEntry(entry)
        repository.updateEntryStatus(entry, newStatus)

        assertEquals(newStatus, repository.getWaitlistEntry("testEntry")?.status)
    }

    @Test
    fun gettingEntriesOnlyReturnsEntriesForThatLottery() = runTest {
        val entryInLottery = WaitlistEntry(entryId = "testEntry", lotteryId = "testLottery")
        val entryInOtherLottery = WaitlistEntry(entryId = "testEntry2", lotteryId = "otherLottery")
        val repository = WaitlistEntryRepository()

        repository.addWaitlistEntry(entryInLottery)
        repository.addWaitlistEntry(entryInOtherLottery)

        val result = repository.getEntries(Lottery(lotteryId = "testLottery"))
        val resultIds = mutableListOf<String>()
        for (entry in result) {
            resultIds.add(entry.entryId)
        }

        assertTrue("testEntry" in resultIds)
        assertFalse("testEntry2" in resultIds)
    }

    @Test
    fun gettingEntriesForEntrantOnlyReturnsEntriesForThatEntrant() = runTest {
        val entryForEntrant = WaitlistEntry(entryId = "testEntry", entrantId = "testEntrant")
        val entryForSomeoneElse = WaitlistEntry(entryId = "testEntry2", entrantId = "otherEntrant")
        val repository = WaitlistEntryRepository()

        repository.addWaitlistEntry(entryForEntrant)
        repository.addWaitlistEntry(entryForSomeoneElse)

        val result = repository.getEntriesForEntrant(Entrant(deviceId = "testEntrant"))
        val resultIds = mutableListOf<String>()
        for (entry in result) {
            resultIds.add(entry.entryId)
        }

        assertTrue("testEntry" in resultIds)
        assertFalse("testEntry2" in resultIds)
    }
}
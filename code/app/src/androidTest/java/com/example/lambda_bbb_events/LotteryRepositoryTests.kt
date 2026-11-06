package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LotteryRepositoryTests {
    @Test
    fun addingLotteryAndGettingTheLottery() = runTest {
        val lottery = Lottery(lotteryId = "testLottery")
        val repository = LotteryRepository()

        repository.addLottery(lottery)
        assertEquals(lottery, repository.getLottery("testLottery"))
        repository.deleteLottery(lottery)
    }

    @Test
    fun gettingALotteryThatDoesNotExistReturnsNull() = runTest {
        val repository = LotteryRepository()
        assertNull(repository.getLottery("thisLotteryDoesntExist!!"))
    }

    @Test
    fun deletingLottery() = runTest {
        val lottery = Lottery(lotteryId = "testLottery")
        val repository = LotteryRepository()

        repository.addLottery(lottery)
        repository.deleteLottery(lottery)

        assertNull(repository.getLottery("testLottery"))
    }

    @Test
    fun updatingLottery() = runTest {
        val lottery = Lottery(lotteryId = "testLottery", ownerId = "oldOwner")
        val updatedLottery = lottery.copy(ownerId = "newOwner")
        val repository = LotteryRepository()

        repository.addLottery(lottery)
        repository.updateLottery(updatedLottery)

        assertEquals("newOwner", repository.getLottery("testLottery")?.ownerId)

        repository.deleteLottery(updatedLottery)
    }

    @Test
    fun gettingLotteriesForSpecificCampsiteOnlyReturnsLotteriesWithThatCampsite() = runTest {
        val lotteryWithCampsite = Lottery(
            lotteryId = "testLottery",
            campsiteIds = listOf("myCampsite")
        )
        val lotteryWithoutCampsite = Lottery(
            lotteryId = "testLottery2",
            campsiteIds = listOf("someOtherCampsite")
        )
        val repository = LotteryRepository()

        repository.addLottery(lotteryWithCampsite)
        repository.addLottery(lotteryWithoutCampsite)

        val result = repository.getLotteriesForCampsite("myCampsite")

        assertTrue(result.contains(lotteryWithCampsite))
        assertFalse(result.contains(lotteryWithoutCampsite))

        // cleaning up the stuffs
        repository.deleteLottery(lotteryWithCampsite)
        repository.deleteLottery(lotteryWithoutCampsite)
    }

    @Test
    fun addingToWaitlist() = runTest {
        val lottery = Lottery(lotteryId = "testLottery", waitlistCapacity = 5)
        val entry = WaitlistEntry(entryId = "entry")
        val repository = LotteryRepository()

        repository.addLottery(lottery)

        val wasAdded = repository.addWaitlistToLottery(lottery, entry)

        assertTrue(wasAdded)
        assertTrue(repository.getLottery("testLottery")?.entryIds?.contains("entry") == true)

        repository.deleteLottery(lottery)
    }

    @Test
    fun addingToFullWaitlistFails() = runTest {
        val lottery = Lottery(
            lotteryId = "testLottery",
            waitlistCapacity = 1,
            entryIds = listOf("entry")
        )
        val newEntry = WaitlistEntry(entryId = "entry2")
        val repository = LotteryRepository()

        val wasAdded = repository.addWaitlistToLottery(lottery, newEntry)

        assertFalse(wasAdded)

        repository.deleteLottery(lottery)
    }

    @Test
    fun addingSameEntryTwiceFails() = runTest {
        val lottery = Lottery(
            lotteryId = "testLottery",
            waitlistCapacity = 5,
            entryIds = listOf("entry")
        )
        val sameEntry = WaitlistEntry(entryId = "entry")
        val repository = LotteryRepository()

        val wasAdded = repository.addWaitlistToLottery(lottery, sameEntry)

        assertFalse(wasAdded)

        repository.deleteLottery(lottery)
    }

    @Test
    fun removingFromWaitlistWorks() = runTest {
        val lottery = Lottery(
            lotteryId = "testLottery",
            waitlistCapacity = 5,
            entryIds = listOf("entry")
        )
        val entry = WaitlistEntry(entryId = "entry")
        val repository = LotteryRepository()

        repository.addLottery(lottery)
        assertEquals(listOf("entry"), repository.getLottery("testLottery")?.entryIds)
        repository.removeWaitlistFromLottery(lottery, entry)
        assertEquals(emptyList<String>(), repository.getLottery("testLottery")?.entryIds)

        repository.deleteLottery(lottery)
    }
}
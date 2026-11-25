package com.example.lambda_bbb_events.data

import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.repository.LotteryRepository
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
 * Tests [LotteryRepository] against the real Firestore database.
 *
 * Every lottery uses a unique ID and is deleted after each test in [cleanUp].
 */
class LotteryRepositoryTests {
    private val testData = FirestoreTestData()
    private lateinit var repository: LotteryRepository

    @Before
    fun setUp() {
        repository = LotteryRepository()
    }

    @After
    fun cleanUp() {
        repository.stopListening()
        testData.cleanUp()
    }

    private fun newLotteryId() = testData.newId("lotteries", prefix = "test-lottery")

    @Test
    fun addingLotteryAndGettingTheLottery() = runTest {
        val lottery = Lottery(lotteryId = newLotteryId())

        repository.addLottery(lottery)

        assertEquals(lottery, repository.getLottery(lottery.lotteryId))
    }

    @Test
    fun gettingALotteryThatDoesNotExistReturnsNull() = runTest {
        assertNull(repository.getLottery("thisLotteryDoesntExist!!"))
    }

    @Test
    fun deletingLottery() = runTest {
        val lottery = Lottery(lotteryId = newLotteryId())

        repository.addLottery(lottery)
        repository.deleteLottery(lottery)

        assertNull(repository.getLottery(lottery.lotteryId))
    }

    @Test
    fun updatingLottery() = runTest {
        val lottery = Lottery(lotteryId = newLotteryId(), ownerId = "oldOwner")
        val updatedLottery = lottery.copy(ownerId = "newOwner")

        repository.addLottery(lottery)
        repository.updateLottery(updatedLottery)

        assertEquals("newOwner", repository.getLottery(lottery.lotteryId)?.ownerId)
    }

    @Test
    fun gettingLotteriesForSpecificCampsiteOnlyReturnsLotteriesWithThatCampsite() = runTest {
        val campsiteId = "test-campsite-${UUID.randomUUID()}"
        val lotteryWithCampsite = Lottery(
            lotteryId = newLotteryId(),
            campsiteIds = listOf(campsiteId)
        )
        val lotteryWithoutCampsite = Lottery(
            lotteryId = newLotteryId(),
            campsiteIds = listOf("someOtherCampsite")
        )

        repository.addLottery(lotteryWithCampsite)
        repository.addLottery(lotteryWithoutCampsite)

        val result = repository.getLotteriesForCampsite(campsiteId)

        assertTrue(result.contains(lotteryWithCampsite))
        assertFalse(result.contains(lotteryWithoutCampsite))
    }

    @Test
    fun addingToWaitlist() = runTest {
        val lottery = Lottery(lotteryId = newLotteryId(), waitlistCapacity = 5)
        val entry = WaitlistEntry(entryId = "entry")

        repository.addLottery(lottery)

        val wasAdded = repository.addWaitlistToLottery(lottery, entry)

        assertTrue(wasAdded)
        assertTrue(repository.getLottery(lottery.lotteryId)?.entryIds?.contains("entry") == true)
    }

    @Test
    fun addingToFullWaitlistFails() = runTest {
        val lottery = Lottery(
            lotteryId = newLotteryId(),
            waitlistCapacity = 1,
            entryIds = listOf("entry")
        )
        val newEntry = WaitlistEntry(entryId = "entry2")

        val wasAdded = repository.addWaitlistToLottery(lottery, newEntry)

        assertFalse(wasAdded)
    }

    @Test
    fun addingSameEntryTwiceFails() = runTest {
        val lottery = Lottery(
            lotteryId = newLotteryId(),
            waitlistCapacity = 5,
            entryIds = listOf("entry")
        )
        val sameEntry = WaitlistEntry(entryId = "entry")

        val wasAdded = repository.addWaitlistToLottery(lottery, sameEntry)

        assertFalse(wasAdded)
    }

    @Test
    fun removingFromWaitlistWorks() = runTest {
        val lottery = Lottery(
            lotteryId = newLotteryId(),
            waitlistCapacity = 5,
            entryIds = listOf("entry")
        )
        val entry = WaitlistEntry(entryId = "entry")

        repository.addLottery(lottery)
        assertEquals(listOf("entry"), repository.getLottery(lottery.lotteryId)?.entryIds)
        repository.removeWaitlistFromLottery(lottery, entry)
        assertEquals(emptyList<String>(), repository.getLottery(lottery.lotteryId)?.entryIds)
    }
}

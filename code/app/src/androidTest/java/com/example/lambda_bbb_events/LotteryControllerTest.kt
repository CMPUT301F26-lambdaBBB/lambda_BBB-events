package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.controller.LotteryController
import com.example.lambda_bbb_events.controller.LotteryFilter
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.LotteryStatus
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class LotteryControllerTest {

    private lateinit var lotteryRepository: LotteryRepository
    private lateinit var lotteryController: LotteryController

    private val campsiteId = "testCampsite"
    private val lotteryId = "testLottery"

    @Before
    fun setup() {
        lotteryRepository = mockk(relaxed = true)
        lotteryController = LotteryController(lotteryRepository)
    }

    @Test
    fun addLotteryDelegatesToRepository() = runTest {
        val lottery = Lottery(lotteryId = lotteryId, campsiteIds = listOf(campsiteId))

        lotteryController.addLottery(lottery)

        coVerify(exactly = 1) {
            lotteryRepository.addLottery(lottery)
        }
    }

    @Test
    fun getLotteriesForCampsiteDefaultActiveFilter() {
        val activeLottery = Lottery(
            lotteryId = "active",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            bookingEnd = Date(System.currentTimeMillis() + 86400000L)
        )
        val cancelledLottery = Lottery(
            lotteryId = "cancelled",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.CANCELLED,
            bookingEnd = Date(System.currentTimeMillis() + 86400000L)
        )
        val pastLottery = Lottery(
            lotteryId = "past",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            bookingEnd = Date(System.currentTimeMillis() - 86400000L)
        )

        every {
            lotteryRepository.getLotteriesForCampsite(campsiteId)
        } returns listOf(activeLottery, cancelledLottery, pastLottery)

        val result = lotteryController.getLotteriesForCampsite(campsiteId)

        assertEquals(1, result.size)
        assertEquals("active", result[0].lotteryId)
    }

    @Test
    fun getLotteriesForCampsiteFilterAll() {
        val activeLottery = Lottery(
            lotteryId = "active",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            bookingEnd = Date(System.currentTimeMillis() + 86400000L)
        )
        val cancelledLottery = Lottery(
            lotteryId = "cancelled",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.CANCELLED,
            bookingEnd = Date(System.currentTimeMillis() + 86400000L)
        )
        val pastLottery = Lottery(
            lotteryId = "past",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            bookingEnd = Date(System.currentTimeMillis() - 86400000L)
        )

        every {
            lotteryRepository.getLotteriesForCampsite(campsiteId)
        } returns listOf(activeLottery, cancelledLottery, pastLottery)

        val result = lotteryController.getLotteriesForCampsite(campsiteId, LotteryFilter.ALL)

        assertEquals(3, result.size)
    }

    @Test
    fun getLotteriesForCampsiteFilterCancelled() {
        val activeLottery = Lottery(
            lotteryId = "active",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            bookingEnd = Date(System.currentTimeMillis() + 86400000L)
        )
        val cancelledLottery = Lottery(
            lotteryId = "cancelled",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.CANCELLED,
            bookingEnd = Date(System.currentTimeMillis() + 86400000L)
        )

        every {
            lotteryRepository.getLotteriesForCampsite(campsiteId)
        } returns listOf(activeLottery, cancelledLottery)

        val result = lotteryController.getLotteriesForCampsite(campsiteId, LotteryFilter.CANCELLED)

        assertEquals(1, result.size)
        assertEquals("cancelled", result[0].lotteryId)
    }

    @Test
    fun getLotteriesForCampsiteFilterPast() {
        val activeLottery = Lottery(
            lotteryId = "active",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            bookingEnd = Date(System.currentTimeMillis() + 86400000L)
        )
        val pastLottery = Lottery(
            lotteryId = "past",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            bookingEnd = Date(System.currentTimeMillis() - 86400000L)
        )

        every {
            lotteryRepository.getLotteriesForCampsite(campsiteId)
        } returns listOf(activeLottery, pastLottery)

        val result = lotteryController.getLotteriesForCampsite(campsiteId, LotteryFilter.PAST)

        assertEquals(1, result.size)
        assertEquals("past", result[0].lotteryId)
    }

    @Test
    fun getOpenLotteryForCampsiteReturnsFirstOpenLottery() {
        val closedLottery = Lottery(
            lotteryId = "closed",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            registrationStart = Date(System.currentTimeMillis() - 7200000L),
            registrationEnd = Date(System.currentTimeMillis() - 3600000L)
        )
        val openLottery = Lottery(
            lotteryId = "open",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            registrationStart = Date(System.currentTimeMillis() - 3600000L),
            registrationEnd = Date(System.currentTimeMillis() + 3600000L)
        )

        every {
            lotteryRepository.getLotteriesForCampsite(campsiteId)
        } returns listOf(closedLottery, openLottery)

        val result = lotteryController.getOpenLotteryForCampsite(campsiteId)

        assertEquals("open", result?.lotteryId)
    }

    @Test
    fun getOpenLotteryForCampsiteReturnsNullWhenNoneOpen() {
        val closedLottery = Lottery(
            lotteryId = "closed",
            campsiteIds = listOf(campsiteId),
            status = LotteryStatus.OPEN,
            registrationStart = Date(System.currentTimeMillis() - 7200000L),
            registrationEnd = Date(System.currentTimeMillis() - 3600000L)
        )

        every {
            lotteryRepository.getLotteriesForCampsite(campsiteId)
        } returns listOf(closedLottery)

        val result = lotteryController.getOpenLotteryForCampsite(campsiteId)

        assertNull(result)
    }

    @Test
    fun joinLotteryEntryDelegatesToRepositoryAndTriggersCallback() {
        var callbackSuccess = false
        var callbackMsg = ""

        every {
            lotteryRepository.joinLottery(eq(lotteryId), eq("entrant-1"), eq(2), any())
        } answers {
            val callback = arg<(Boolean, String) -> Unit>(3)
            callback(true, "Successfully joined lottery")
        }

        lotteryController.joinLotteryEntry(lotteryId, "entrant-1", 2) { success, msg ->
            callbackSuccess = success
            callbackMsg = msg
        }

        verify(exactly = 1) {
            lotteryRepository.joinLottery(eq(lotteryId), eq("entrant-1"), eq(2), any())
        }
        assertTrue(callbackSuccess)
        assertEquals("Successfully joined lottery", callbackMsg)
    }
}
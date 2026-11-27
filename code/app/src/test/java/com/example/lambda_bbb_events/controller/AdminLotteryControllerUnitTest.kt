package com.example.lambda_bbb_events.controller

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [AdminLotteryController].
 */
class AdminLotteryControllerUnitTest {

    @Test
    fun runAllLotteriesNow_successTrue_succeeds() = runTest {
        val sentIds = mutableListOf<String>()
        val controller = AdminLotteryController(runAllLotteries = { id ->
            sentIds += id
            mapOf("success" to true)
        })

        val result = controller.runAllLotteriesNow("admin-device")

        assertTrue(result.isSuccess)
        assertEquals(listOf("admin-device"), sentIds)
    }

    @Test
    fun runAllLotteriesNow_successFalse_fails() = runTest {
        val controller = AdminLotteryController(runAllLotteries = {
            mapOf("success" to false, "message" to "nothing to draw")
        })

        val result = controller.runAllLotteriesNow("admin-device")

        assertTrue(result.isFailure)
        assertEquals("nothing to draw", result.exceptionOrNull()?.message)
    }

    @Test
    fun runAllLotteriesNow_missingResponse_fails() = runTest {
        val controller = AdminLotteryController(runAllLotteries = { null })

        assertTrue(controller.runAllLotteriesNow("admin-device").isFailure)
    }

    @Test
    fun runAllLotteriesNow_wrapsThrownError() = runTest {
        val controller = AdminLotteryController(
            runAllLotteries = { throw IllegalStateException("permission-denied") }
        )

        val result = controller.runAllLotteriesNow("admin-device")

        assertTrue(result.isFailure)
        assertEquals("permission-denied", result.exceptionOrNull()?.message)
    }
}

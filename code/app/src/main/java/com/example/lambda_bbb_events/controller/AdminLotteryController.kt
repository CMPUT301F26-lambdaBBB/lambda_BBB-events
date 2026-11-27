package com.example.lambda_bbb_events.controller

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await

/**
 * Handles admin-only lottery actions backed by Firebase Cloud Functions.
 *
 * @property runAllLotteries Triggers the server-side draw for the given admin device ID
 * and returns the callable's response data.
 */
class AdminLotteryController(
    private val runAllLotteries: suspend (String) -> Any? = ::callManuallyRunLotteries
) {
    /**
     * Runs all lotteries immediately instead of waiting for the scheduled job.
     *
     * @param deviceId The device ID of the admin requesting the run, checked by the function.
     * @return [Result.success] when the function responded with `success: true`, otherwise a failure.
     */
    suspend fun runAllLotteriesNow(deviceId: String): Result<Unit> = runCatching {
        val response = runAllLotteries(deviceId) as? Map<*, *>
        check(response?.get("success") == true) {
            response?.get("message")?.toString() ?: "Server did not report success."
        }
    }
}

/**
 * Calls the admin-only `manuallyRunLottery` callable Cloud Function.
 *
 * The app does not use Firebase Auth, so the caller's device ID is sent as `deviceId`
 * for the function to verify the admin role.
 *
 * @param deviceId The device ID of the requesting admin.
 * @return The data returned by the function.
 * @throws IllegalStateException prefixed with the Firebase error code when the call fails.
 */
private suspend fun callManuallyRunLotteries(deviceId: String): Any? =
    try {
        FirebaseFunctions.getInstance()
            .getHttpsCallable("manuallyRunLottery")
            .call(mapOf("deviceId" to deviceId))
            .await()
            .getData()
    } catch (e: FirebaseFunctionsException) {
        throw IllegalStateException("[${e.code}] ${e.message}", e)
    }

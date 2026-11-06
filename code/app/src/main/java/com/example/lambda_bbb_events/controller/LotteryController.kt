package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.LotteryStatus
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import java.util.Date

/**
 * The filters for a list of lotteries.
 */
enum class LotteryFilter {
    ALL,        // all lotteries
    ACTIVE,     // the registration period still active
    PAST,       // the registration period has ended
    CANCELLED   // cancelled by the owner
}

/**
 * Controller for [Lottery]-related operations.
 *
 * @property lotteryRepository The repository for [Lottery].
 */
class LotteryController(
    val lotteryRepository: LotteryRepository
) {
    /**
     * Adds a new lottery to the repository and Firestore database.
     */
    suspend fun addLottery(lottery: Lottery) {
        lotteryRepository.addLottery(lottery)
    }
    /**
     * Gets the lotteries of a campsite that match a filter.
     *
     * @param campsiteId The `campsiteId` to search for.
     * @param filter Which lotteries to return. The default is only the active ones.
     * @return The list of matching lotteries.
     */
    fun getLotteriesForCampsite(
        campsiteId: String,
        filter: LotteryFilter = LotteryFilter.ACTIVE
    ): List<Lottery> {
        val result = mutableListOf<Lottery>()

        for (lottery in lotteryRepository.getLotteriesForCampsite(campsiteId)) {
            if (matchesFilter(lottery, filter)) {
                result.add(lottery)
            }
        }

        return result
    }

    /**
     * Checks if a lottery matches a filter.
     *
     * @param lottery The lottery to check.
     * @param filter The filter to check against.
     * @return `true` if the lottery matches the filter, `false` otherwise.
     */
    private fun matchesFilter(lottery: Lottery, filter: LotteryFilter): Boolean {
        val isCancelled = lottery.status == LotteryStatus.CANCELLED
        val isOver = lottery.bookingEnd.before(Date())

        if (filter == LotteryFilter.ALL) {
            return true
        }

        if (filter == LotteryFilter.CANCELLED) {
            return isCancelled
        }

        if (filter == LotteryFilter.ACTIVE) {
            return !isCancelled && !isOver
        }

        return !isCancelled && isOver
    }

    /**
     * Gets the first lottery for a campsite whose registration period is currently open.
     *
     * @param campsiteId The `campsiteId` to search for.
     * @return The first lottery with open registration, or `null` if none exists.
     */
    fun getOpenLotteryForCampsite(campsiteId: String): Lottery? {
        return lotteryRepository
            .getLotteriesForCampsite(campsiteId)
            .firstOrNull { it.isRegistrationOpen() }
    }

    /**
     * Registers an entrant for a lottery.
     *
     * @param lotteryId The ID of the lottery to join.
     * @param entrantId The ID of the entrant joining the lottery.
     * @param numberOfGuests The number of guests included in the entry.
     * @param onResult Callback that returns whether registration was successful
     * and a message describing the result.
     */
    fun joinLotteryEntry(
        lotteryId: String,
        entrantId: String,
        numberOfGuests: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        lotteryRepository.joinLottery(
            lotteryId = lotteryId,
            entrantId = entrantId,
            numberOfGuests = numberOfGuests,
            onResult = onResult
        )
    }
}
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
     * Adds a lottery to the repository.
     *
     * @param lottery The [Lottery] to add.
     */
    fun addLottery(lottery: Lottery) {
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

        // past lottery
        return !isCancelled && isOver
    }


    /**
     * Retrieves the first lottery accepting registrations
     * for the specified campsite.
     *
     * @param campsiteId The ID of the campsite.
     * @return An open [Lottery], or null if none is available.
     */
    fun getOpenLotteryForCampsite(campsiteId: String): Lottery? {
        return lotteryRepository
            .getLotteriesForCampsite(campsiteId)
            .firstOrNull { it.isRegistrationOpen() }
    }


    /**
     * Submits a user's entry to a campsite lottery.
     *
     * @param lotteryId The ID of the lottery to join.
     * @param entrantId The ID of the participating user.
     * @param numberOfGuests The number of guests in the entry.
     * @param onResult Callback returning the success status and a message.
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
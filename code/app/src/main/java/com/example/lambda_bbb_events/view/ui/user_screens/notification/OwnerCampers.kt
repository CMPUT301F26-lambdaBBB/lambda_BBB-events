package com.example.lambda_bbb_events.view.ui.user_screens.notification

import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.LotteryFilter
import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.LotteryStatus
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus

/*
 * Finds the campers a campsite owner can start a conversation with (US 02.12.01). Everyone
 * who reserves a campsite entered its lottery first, so the owner's lottery entrants include
 * everyone reserving their campsites.
 */

/** Entry statuses whose campers lost their messaging access to the owner. */
private val CLOSED_ENTRY_STATUSES = setOf(WaitlistStatus.LOST, WaitlistStatus.DECLINED)

/**
 * Gets the campers who entered a lottery for one of an owner's campsites and can still
 * message the owner, with the owner's campsites each one entered for.
 *
 * @param ownerId Device ID of the campsite owner.
 * @param controllers Reads the owner's campsites, their lotteries, and the lotteries' entries.
 * @return The owner's campsite IDs for each camper, by the camper's device ID.
 */
fun lotteryCampersOf(ownerId: String, controllers: AppControllers): Map<String, Set<String>> {
    val ownedCampsiteIds = controllers.campsites.getCampsitesByOwner(ownerId)
        .map { it.campsiteId }
        .toSet()

    val campers = mutableMapOf<String, MutableSet<String>>()
    for (lottery in lotteriesOf(ownedCampsiteIds, controllers)) {
        val campsiteIds = lottery.campsiteIds.filter { it in ownedCampsiteIds }
        for (camperId in openEntrantIds(lottery, controllers)) {
            campers.getOrPut(camperId) { mutableSetOf() }.addAll(campsiteIds)
        }
    }
    campers.remove(ownerId)
    return campers
}

/** Every lottery, not cancelled, that includes one of [campsiteIds]. */
private fun lotteriesOf(campsiteIds: Set<String>, controllers: AppControllers): List<Lottery> =
    campsiteIds
        .flatMap { controllers.lotteries.getLotteriesForCampsite(it, LotteryFilter.ALL) }
        .distinctBy { it.lotteryId }
        .filter { it.status != LotteryStatus.CANCELLED }

/** The device IDs of the lottery's entrants who have not lost or declined. */
private fun openEntrantIds(lottery: Lottery, controllers: AppControllers): List<String> =
    controllers.waitlistEntries.getEntriesForLottery(lottery.lotteryId)
        .filter { it.status !in CLOSED_ENTRY_STATUSES }
        .map { it.entrantId }

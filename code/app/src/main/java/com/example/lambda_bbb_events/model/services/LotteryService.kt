package com.example.lambda_bbb_events.model.services

import com.example.lambda_bbb_events.model.lottery.Lottery


/**
 * Draws lottery winners and losers
 *
 * @param lottery Lottery to be drawn
 */

fun draw_winners(lottery: Lottery): MutableList< MutableList<String> >{

    val winners: MutableList<String> = mutableListOf()
    val losers: MutableList<String> = lottery.entryIds.toMutableList()
    val numWinners = minOf(lottery.numberOfWinners, losers.size) // cannot draw more winners than entries

    for (i in 0..<numWinners){
        val value = (losers).random()

        losers.remove(value)
        winners.add(value)

    }


    return mutableListOf(winners, losers)
}
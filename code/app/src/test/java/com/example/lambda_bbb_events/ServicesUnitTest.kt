package com.example.lambda_bbb_events

import androidx.navigation.NavController
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.goToPage
import com.example.lambda_bbb_events.model.services.draw_winners
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServicesUnitTest {

    // draw_winners

    @Test
    fun drawSplitsEveryEntryIntoWinnersOrLosers() {
        val entries = listOf("a", "b", "c", "d", "e")

        repeat(20) {
            val (winners, losers) = draw_winners(Lottery(numberOfWinners = 2, entryIds = entries))

            assertEquals(2, winners.size)
            assertEquals(3, losers.size)
            assertEquals(entries.toSet(), (winners + losers).toSet())
            assertTrue(winners.intersect(losers.toSet()).isEmpty())
        }
    }

    @Test
    fun drawWithMoreWinnersThanEntriesMakesEveryoneAWinner() {
        val (winners, losers) = draw_winners(Lottery(numberOfWinners = 5, entryIds = listOf("a", "b")))

        assertEquals(setOf("a", "b"), winners.toSet())
        assertTrue(losers.isEmpty())
    }

    @Test
    fun drawWithNoEntriesHasNoWinners() {
        val (winners, losers) = draw_winners(Lottery(numberOfWinners = 3))

        assertTrue(winners.isEmpty())
        assertTrue(losers.isEmpty())
    }

    @Test
    fun drawDoesNotChangeTheLottery() {
        val lottery = Lottery(numberOfWinners = 1, entryIds = listOf("a", "b"))

        draw_winners(lottery)

        assertEquals(listOf("a", "b"), lottery.entryIds)
    }

    // goToPage

    private val navController = mockk<NavController>().also {
        every { it.navigate(any<String>()) } just runs
    }

    @Test
    fun plainCampsiteIdOpensDetails() {
        goToPage(navController, "  site-1 ")

        verify(exactly = 1) { navController.navigate("campsite_details/site-1") }
    }

    @Test
    fun urlOpensDetailsForLastPathSegment() {
        goToPage(navController, "https://lambda.example.com/campsites/site-2")

        verify(exactly = 1) { navController.navigate("campsite_details/site-2") }
    }
}

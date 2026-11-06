package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.model.WaitlistStatus
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.Date

class WaitlistEntryTesting {
    @Test
    fun defaultWaitlistEntry() {
        val entry = WaitlistEntry()

        assertEquals("", entry.entryId)
        assertEquals("", entry.entrantId)
        assertEquals("", entry.lotteryId)
        assertEquals(1, entry.numberOfGuests)
        assertEquals(WaitlistStatus.IN_WAITLIST, entry.status)
    }

    @Test
    fun createWaitlistEntry() {
        val date = Date(0)

        val entry = WaitlistEntry(
            entryId = "entry1",
            entrantId = "user1",
            lotteryId = "lottery1",
            joinedAt = date,
            numberOfGuests = 3,
            status = WaitlistStatus.WON
        )

        assertEquals("entry1", entry.entryId)
        assertEquals("user1", entry.entrantId)
        assertEquals("lottery1", entry.lotteryId)
        assertEquals(date, entry.joinedAt)
        assertEquals(3, entry.numberOfGuests)
        assertEquals(WaitlistStatus.WON, entry.status)
    }

    @Test
    fun numberOfGuestsIsZero() {
        assertThrows(IllegalArgumentException::class.java) {
            WaitlistEntry(numberOfGuests = 0)
        }
    }

    @Test
    fun numberOfGuestsIsNegative() {
        assertThrows(IllegalArgumentException::class.java) {
            WaitlistEntry(numberOfGuests = -1)
        }
    }

    @Test
    fun declineWaitlistEntry() {
        val entry = WaitlistEntry(
            status = WaitlistStatus.WON
        )

        entry.declineEntry()

        assertEquals(WaitlistStatus.DECLINED, entry.status)
    }

    @Test
    fun declineAlreadyDeclinedEntry() {
        val entry = WaitlistEntry(
            status = WaitlistStatus.DECLINED
        )

        entry.declineEntry()
        entry.declineEntry()
        assertEquals(WaitlistStatus.DECLINED, entry.status)
    }
}
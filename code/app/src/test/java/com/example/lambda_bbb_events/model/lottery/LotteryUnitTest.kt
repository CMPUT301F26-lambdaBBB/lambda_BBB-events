package com.example.lambda_bbb_events.model.lottery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LotteryUnitTest {
    @Test
    fun lotteryIsFull() {
        val lottery = Lottery(
            entryIds = listOf("Hello", "YutaTakenaka"),
            waitlistCapacity = 2
        )
        assertTrue(lottery.isFull())
    }

    @Test
    fun lotteryIsNotFull() {
        val lottery = Lottery(
            entryIds = listOf("YutaTakenaka"),
            waitlistCapacity = 2
        )
        assertFalse(lottery.isFull())
    }

    @Test
    fun checkRegistrationIsNotOpen() {
        val lottery = Lottery(
            status = LotteryStatus.CLOSED,
            registrationStart = LocalDate.of(2026, 1, 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .let( Date::from ),
            registrationEnd = LocalDate.of(2026, 1, 5)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .let( Date::from )
        )
        assertFalse(lottery.isRegistrationOpen(LocalDate.of(2026, 1, 1)
                                                .atStartOfDay(ZoneId.systemDefault())
                                                .toInstant()
                                                .let(Date::from))
        )
    }

    @Test
    fun checkRegistrationIsOpen() {
        val lottery = Lottery(
            status = LotteryStatus.OPEN,
            registrationStart = LocalDate.of(2026, 1, 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .let( Date::from ),
            registrationEnd = LocalDate.of(2026, 1, 5)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .let( Date::from )
        )
        assertTrue(lottery.isRegistrationOpen(LocalDate.of(2026, 1, 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .let(Date::from))
        )
    }

    @Test
    fun registrationIsOpenIncludingBothEnds() {
        val lottery = Lottery(registrationStart = Date(1_000), registrationEnd = Date(2_000))

        assertFalse(lottery.isRegistrationOpen(Date(999)))
        assertTrue(lottery.isRegistrationOpen(Date(1_000)))
        assertTrue(lottery.isRegistrationOpen(Date(2_000)))
        assertFalse(lottery.isRegistrationOpen(Date(2_001)))
    }

    @Test
    fun registrationIsOnlyOpenForOpenLotteries() {
        for (status in LotteryStatus.entries) {
            val lottery = Lottery(registrationStart = Date(1_000), registrationEnd = Date(2_000), status = status)
            assertEquals(status == LotteryStatus.OPEN, lottery.isRegistrationOpen(Date(1_500)))
        }
    }

    @Test
    fun lotteryIsFullAtCapacity() {
        assertFalse(Lottery(waitlistCapacity = 2, entryIds = listOf("a")).isFull())
        assertTrue(Lottery(waitlistCapacity = 2, entryIds = listOf("a", "b")).isFull())
        assertTrue(Lottery(waitlistCapacity = 2, entryIds = listOf("a", "b", "c")).isFull())
    }

    @Test
    fun lotteryWithoutCapacityIsNeverFull() {
        assertNull(Lottery().waitlistCapacity)
        assertFalse(Lottery(entryIds = List(1_000) { "entry-$it" }).isFull())
    }
}

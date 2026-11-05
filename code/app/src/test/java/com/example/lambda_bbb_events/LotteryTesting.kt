package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.LotteryStatus
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LotteryTesting {
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
}
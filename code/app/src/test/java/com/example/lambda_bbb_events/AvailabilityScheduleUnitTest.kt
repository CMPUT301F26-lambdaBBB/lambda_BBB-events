package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.AvailabilitySchedule
import org.junit.Test
import org.junit.Assert.assertTrue
import java.time.LocalDate

class AvailabilityScheduleUnitTest {
    @Test
    fun testAvailabilityScheduleBasic() {
        val schedule = AvailabilitySchedule()
        val start = LocalDate.of(2026, 5, 1)
        val end = LocalDate.of(2026, 5, 3)
        schedule.addAvailableDates(start, end)
        assertTrue(schedule.isAvailable(start, end))
    }
}

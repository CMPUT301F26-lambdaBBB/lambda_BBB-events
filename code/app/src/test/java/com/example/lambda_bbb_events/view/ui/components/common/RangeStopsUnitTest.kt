package com.example.lambda_bbb_events.view.ui.components.common

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests matching ranges to the stops of range sliders.
 */
class RangeStopsUnitTest {
    private val stops = listOf(10, 2000, 9999)

    @Test
    fun rangesBetweenStopsAreKept() {
        assertEquals(10..2000, closestStops(10..2000, stops))
        assertEquals(2000..2000, closestStops(2000..2000, stops))
    }

    @Test
    fun endsThatAreNotStopsMoveToTheClosest() {
        assertEquals(10..2000, closestStops(4..1800, stops))
        assertEquals(9999..9999, closestStops(8000..20_000, stops))
    }
}

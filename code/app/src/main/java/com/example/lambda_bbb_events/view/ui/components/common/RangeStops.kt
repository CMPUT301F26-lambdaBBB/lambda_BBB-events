package com.example.lambda_bbb_events.view.ui.components.common

import kotlin.math.abs

/*
 * Matches ranges of values to the stops of a range slider.
 */

/**
 * Moves both ends of a range to their closest stops. The stops can change after a range was
 * chosen, so its ends may no longer be stops.
 *
 * @param range The chosen range.
 * @param stops The values the slider stops at, in order. Must not be empty.
 */
fun closestStops(range: IntRange, stops: List<Int>): IntRange =
    closestStop(range.first, stops)..closestStop(range.last, stops)

private fun closestStop(value: Int, stops: List<Int>): Int = stops.minBy { abs(it - value) }

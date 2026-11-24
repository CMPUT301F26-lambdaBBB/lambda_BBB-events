package com.example.lambda_bbb_events.model.campsite

import java.time.LocalDate

/**
 * The filters applied when browsing campsites. By default, nothing is filtered.
 *
 * @property startDate The first day the campsite must be available, or `null` for any dates.
 * @property endDate The last day the campsite must be available. `null` for only [startDate].
 * @property location Text the campsite's address or name must contain. Empty for anywhere.
 * @property starRating The lowest average rating, or `null` for any rating.
 * @property guestRange The numbers of guests the campsite can allow, or `null` for any number.
 */
data class CampsiteFilters(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val location: String = "",
    val starRating: Double? = null,
    val guestRange: IntRange? = null
)

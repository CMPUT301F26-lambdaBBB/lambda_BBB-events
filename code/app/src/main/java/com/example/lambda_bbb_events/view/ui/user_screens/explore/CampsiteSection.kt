package com.example.lambda_bbb_events.view.ui.user_screens.explore

/**
 * Categories of campsite lists that users can expand.
 *
 * @property title The display title of the campsite section.
 */
enum class CampsiteSection(val title: String) {
    RECENTLY_VIEWED("Recently Viewed"),
    FEATURED("Featured Campsites"),
    RECENTLY_ADDED("Recently Added"),
    NEARBY("Nearby Campsites")
}
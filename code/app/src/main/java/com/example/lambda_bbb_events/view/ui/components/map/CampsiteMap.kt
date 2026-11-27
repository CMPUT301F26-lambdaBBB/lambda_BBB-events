package com.example.lambda_bbb_events.view.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.services.MapFallback
import com.example.lambda_bbb_events.model.services.MapProvider

/*
 * Provides a map showing where campsites are.
 */

/** Zoom level used when showing a single campsite. */
internal const val SINGLE_CAMPSITE_ZOOM = 13

/** Space in dp kept between the outermost markers and the edge of the map. */
internal const val BOUNDS_PADDING = 48

/**
 * Closest zoom used when framing several campsites. Without it, campsites at the same spot
 * zoom the map in so far that only a blank patch of ground shows.
 */
internal const val MAX_FRAMING_ZOOM = 14

/**
 * Displays a map with a marker for each campsite that has a location.
 *
 * The map frames every marker, or zooms in on the campsite when there is only one.
 * Tapping a marker shows the campsite's name and address, with a link that opens it. The map is
 * Google's, or OpenStreetMap's when there is no Google API key.
 *
 * @param campsites The campsites to mark on the map.
 * @param modifier Sets the size and shape of the map.
 * @param onCampsiteClick Called when a campsite's link is tapped, or `null` to do nothing.
 * @param emptyMessage Shown in place of the map when no campsite has a location.
 */
@Composable
fun CampsiteMap(
    campsites: List<Campsite>,
    modifier: Modifier = Modifier,
    onCampsiteClick: ((Campsite) -> Unit)? = null,
    emptyMessage: String = "Location not available"
) {
    val located = campsites.filter { it.hasLocation() }

    if (located.isEmpty()) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val mapModifier = modifier.testTag("campsite_map")
    when (MapFallback.shared.mapProvider) {
        MapProvider.GOOGLE -> GoogleCampsiteMap(located, mapModifier, onCampsiteClick)
        MapProvider.OPEN_STREET_MAP -> OpenStreetCampsiteMap(located, mapModifier, onCampsiteClick)
    }
}

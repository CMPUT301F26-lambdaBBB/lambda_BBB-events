package com.example.lambda_bbb_events.view.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.services.MapFallback
import com.example.lambda_bbb_events.model.services.MapProvider
import org.json.JSONArray
import org.json.JSONObject

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
 * Lists the campsites for an OpenStreetMap page. They are passed as JSON, which keeps quotes in
 * names from breaking the page's script.
 */
internal fun campsiteMarkers(campsites: List<Campsite>): JSONArray {
    val markers = JSONArray()
    for (campsite in campsites) {
        val marker = JSONObject()
        marker.put("id", campsite.campsiteId)
        marker.put("name", campsite.campsiteName)
        marker.put("address", campsite.address)
        marker.put("lat", campsite.latitude)
        marker.put("lng", campsite.longitude)
        markers.put(marker)
    }
    return markers
}

/**
 * Displays a map with a marker for each campsite that has a location.
 *
 * The map frames every marker, or zooms in on the campsite when there is only one.
 * Tapping a marker shows the campsite's name and address, with a link that opens it. The map is
 * Google's, or OpenStreetMap's once Google has stopped working.
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
    when (MapFallback.shared.provider) {
        MapProvider.GOOGLE -> GoogleCampsiteMap(located, mapModifier, onCampsiteClick)
        MapProvider.OPEN_STREET_MAP -> OpenStreetCampsiteMap(located, mapModifier, onCampsiteClick)
    }
}

/**
 * The OpenStreetMap version of [GoogleCampsiteMap], drawn by a web page.
 *
 * @param campsites The campsites to mark, all with a location.
 * @param modifier Sets the size and shape of the map.
 * @param onCampsiteClick Called when a campsite's link is tapped, or `null` to do nothing.
 */
@Composable
private fun OpenStreetCampsiteMap(
    campsites: List<Campsite>,
    modifier: Modifier,
    onCampsiteClick: ((Campsite) -> Unit)?
) {
    val html = remember(campsites) { osmPageHtml(osmCampsiteMapScript(campsites)) }

    MapsJavaScriptView(
        html = html,
        modifier = modifier,
        onEvent = { name, data ->
            if (name == "campsiteClick" && onCampsiteClick != null) {
                val id = data.optString("id")
                val campsite = campsites.find { it.campsiteId == id }
                if (campsite != null) {
                    onCampsiteClick(campsite)
                }
            }
        }
    )
}

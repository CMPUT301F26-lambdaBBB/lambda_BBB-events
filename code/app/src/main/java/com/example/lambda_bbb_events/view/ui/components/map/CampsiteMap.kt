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

/** Space in pixels kept between the outermost markers and the edge of the map. */
internal const val BOUNDS_PADDING = 48

/**
 * Closest zoom used when framing several campsites. Without it, campsites at the same spot
 * zoom the map in so far that only a blank patch of ground shows.
 */
internal const val MAX_FRAMING_ZOOM = 14

/**
 * Lists the campsites for a map page. They are passed as JSON, which keeps quotes in names from
 * breaking the page's script.
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
 * Builds the JavaScript that draws a marker for each campsite on a Google Map and frames them.
 *
 * Tapping a marker opens an info window with the campsite's name and address, and tapping
 * "View campsite" sends a `campsiteClick` event with the campsite's ID.
 *
 * @param campsites The campsites to mark, all with a location.
 */
internal fun campsiteMapScript(campsites: List<Campsite>): String {
    return """
        const campsites = ${campsiteMarkers(campsites)};

        async function initMap() {
          const { Map, InfoWindow } = await google.maps.importLibrary('maps');
          const { AdvancedMarkerElement } = await google.maps.importLibrary('marker');
          const { LatLngBounds } = await google.maps.importLibrary('core');

          const map = new Map(document.getElementById('map'), {
            center: { lat: campsites[0].lat, lng: campsites[0].lng },
            zoom: $SINGLE_CAMPSITE_ZOOM,
            mapId: 'DEMO_MAP_ID',
            gestureHandling: 'greedy',
            mapTypeControl: false,
            streetViewControl: false,
            fullscreenControl: false,
            keyboardShortcuts: false,
            clickableIcons: false
          });
          const info = new InfoWindow();
          const bounds = new LatLngBounds();

          campsites.forEach(function (campsite) {
            const position = { lat: campsite.lat, lng: campsite.lng };
            const marker = new AdvancedMarkerElement({ map: map, position: position, title: campsite.name, gmpClickable: true });
            bounds.extend(position);

            marker.addEventListener('gmp-click', function () {
              const content = document.createElement('div');
              content.className = 'info';
              const name = document.createElement('b');
              name.textContent = campsite.name;
              const address = document.createElement('div');
              address.textContent = campsite.address;
              const link = document.createElement('a');
              link.href = '#';
              link.textContent = 'View campsite';
              link.onclick = function (event) {
                event.preventDefault();
                send('campsiteClick', { id: campsite.id });
              };
              content.append(name, address, link);
              info.setContent(content);
              info.open({ anchor: marker, map: map });
            });
          });

          if (campsites.length > 1) {
            map.fitBounds(bounds, $BOUNDS_PADDING);
            google.maps.event.addListenerOnce(map, 'idle', function () {
              if (map.getZoom() > $MAX_FRAMING_ZOOM) {
                map.setZoom($MAX_FRAMING_ZOOM);
              }
            });
          }
        }
    """.trimIndent()
}

/**
 * Displays a map with a marker for each campsite that has a location.
 *
 * The map frames every marker, or zooms in on the campsite when there is only one.
 * Tapping a marker shows the campsite's name and address, with a link that opens it.
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

    val provider = MapFallback.shared.provider
    val html = remember(located, provider) {
        when (provider) {
            MapProvider.GOOGLE -> mapPageHtml(campsiteMapScript(located))
            MapProvider.OPEN_STREET_MAP -> osmPageHtml(osmCampsiteMapScript(located))
        }
    }

    MapsJavaScriptView(
        html = html,
        provider = provider,
        modifier = modifier.testTag("campsite_map"),
        onEvent = { name, data ->
            if (name == "campsiteClick" && onCampsiteClick != null) {
                val id = data.optString("id")
                val campsite = located.find { it.campsiteId == id }
                if (campsite != null) {
                    onCampsiteClick(campsite)
                }
            }
        }
    )
}

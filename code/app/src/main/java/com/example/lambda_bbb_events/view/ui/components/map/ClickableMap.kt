package com.example.lambda_bbb_events.view.ui.components.map

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.services.MapFallback
import com.example.lambda_bbb_events.model.services.MapProvider
import com.google.android.gms.maps.model.LatLng
import java.util.Locale


/*
 * Provides an interactive map for selecting campsite locations
 */

/**
 * Builds the JavaScript for a Google Map with one draggable marker. Tapping the map or dragging
 * the marker moves it and sends a `locationSelected` event with its coordinates.
 *
 * Sends a `mapReady` event once the app can call `showLocation(lat, lng)`, which moves the
 * marker and map there without sending a `locationSelected` event.
 *
 * @param initialLocation Where the marker and map start.
 */
internal fun clickableMapScript(initialLocation: LatLng): String = """
    const start = { lat: ${initialLocation.latitude}, lng: ${initialLocation.longitude} };

    async function initMap() {
      const { Map } = await google.maps.importLibrary('maps');
      const { AdvancedMarkerElement } = await google.maps.importLibrary('marker');

      const map = new Map(document.getElementById('map'), {
        center: start,
        zoom: 13,
        mapId: 'DEMO_MAP_ID',
        gestureHandling: 'greedy',
        mapTypeControl: false,
        streetViewControl: false,
        fullscreenControl: false,
        keyboardShortcuts: false,
        clickableIcons: false
      });
      const marker = new AdvancedMarkerElement({ map: map, position: start, title: 'Selected Location', gmpDraggable: true });

      function select(latLng) {
        marker.position = latLng;
        send('locationSelected', { lat: latLng.lat(), lng: latLng.lng() });
      }

      map.addListener('click', function (event) { select(event.latLng); });
      marker.addListener('dragend', function () {
        const position = marker.position;
        send('locationSelected', { lat: position.lat, lng: position.lng });
      });

      window.showLocation = function (lat, lng) {
        const position = { lat: lat, lng: lng };
        marker.position = position;
        map.setCenter(position);
        map.setZoom(13);
      };
      send('mapReady');
    }
""".trimIndent()

/**
 * A request to move a [ClickableMap] to a location. Every instance moves the map once, so
 * choosing the same place again moves it back there.
 */
class MapMove(val location: LatLng)

/**
 * Composable for displaying a clickable map with a marker.
 *
 * @param modifier Modifier for styling and layout.
 * @param height Height of the map in dp.
 * @param initialLocation Initial location to center the map on.
 * @param move Moves the marker and map when it changes, without calling [onLocationSelected].
 * @param onLocationSelected Callback when a location is selected. Returns coordinates: LatLng, address: String, postalCode: String
 */
@Composable
fun ClickableMap(
    modifier: Modifier = Modifier,
    height: Int = 300,
    initialLocation: LatLng = LatLng(51.1869, -115.5323),
    move: MapMove? = null,
    onLocationSelected: (coordinates: LatLng, address: String, postalCode: String) -> Unit
) {
    val context = LocalContext.current
    val provider = MapFallback.shared.provider

    // Where the marker is, so that it stays there if the map is switched to another provider
    var markerLocation by remember(initialLocation) { mutableStateOf(initialLocation) }
    // A move the user has since replaced by tapping the map, which must not be made again
    var replacedMove by remember { mutableStateOf<MapMove?>(null) }
    LaunchedEffect(move) {
        if (move != null) {
            markerLocation = move.location
        }
    }

    val html = remember(initialLocation, provider) {
        when (provider) {
            MapProvider.GOOGLE -> mapPageHtml(clickableMapScript(markerLocation))
            MapProvider.OPEN_STREET_MAP -> osmPageHtml(osmClickableMapScript(markerLocation))
        }
    }

    // A move made before the map has loaded waits until it is ready
    var isReady by remember(html) { mutableStateOf(false) }
    val command = remember(move, replacedMove, isReady) {
        if (move != null && move !== replacedMove && isReady) {
            MapCommand("showLocation(${move.location.latitude}, ${move.location.longitude});")
        } else {
            null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        MapsJavaScriptView(
            html = html,
            provider = provider,
            modifier = Modifier.fillMaxSize(),
            command = command,
            onEvent = { name, data ->
                if (name == "mapReady") {
                    isReady = true
                } else if (name == "authFailure") {
                    isReady = false // the map was replaced by a message, so it cannot be moved
                } else if (name == "locationSelected") {
                    val latLng = LatLng(data.optDouble("lat"), data.optDouble("lng"))
                    markerLocation = latLng
                    replacedMove = move

                    // Reverse geocode coordinates to retrieve address and postal code
                    fetchAddressAndPostalCodeFromLatLng(context, latLng) { fetchedAddress, fetchedPostalCode ->
                        onLocationSelected(latLng, fetchedAddress, fetchedPostalCode)
                    }
                }
            }
        )
    }
}

/**
 * Performs reverse geocoding to retrieve the address and postal code for the given [LatLng] coordinates.
 *
 * @param context The current Android [Context].
 * @param location The [LatLng] coordinates to reverse-geocode.
 * @param callback Callback returning the retrieved full address and postal code.
 */
fun fetchAddressAndPostalCodeFromLatLng(
    context: Context,
    location: LatLng,
    callback: (address: String, postalCode: String) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                val addrObj = addresses.firstOrNull()
                val fullAddress = addrObj?.getAddressLine(0) ?: ""
                val postalCode = addrObj?.postalCode ?: ""
                callback(fullAddress, postalCode)
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val addrObj = addresses?.firstOrNull()
            val fullAddress = addrObj?.getAddressLine(0) ?: ""
            val postalCode = addrObj?.postalCode ?: ""
            callback(fullAddress, postalCode)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        callback("", "")
    }
}

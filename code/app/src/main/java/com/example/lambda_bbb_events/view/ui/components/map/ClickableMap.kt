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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import java.util.Locale


/*
 * Provides an interactive Google Map for selecting campsite locations
 */

/**
 * Builds the JavaScript for a map with one draggable marker. Tapping the map or dragging
 * the marker moves it and sends a `locationSelected` event with its coordinates.
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
    }
""".trimIndent()

/**
 * Composable for displaying a clickable map with a marker.
 *
 * @param modifier Modifier for styling and layout.
 * @param height Height of the map in dp.
 * @param initialLocation Initial location to center the map on.
 * @param onLocationSelected Callback when a location is selected. Returns coordinates: LatLng, address: String, postalCode: String
 */
@Composable
fun ClickableMap(
    modifier: Modifier = Modifier,
    height: Int = 300,
    initialLocation: LatLng = LatLng(51.1869, -115.5323),
    onLocationSelected: (coordinates: LatLng, address: String, postalCode: String) -> Unit
) {
    val context = LocalContext.current
    val html = remember(initialLocation) { mapPageHtml(clickableMapScript(initialLocation)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        MapsJavaScriptView(
            html = html,
            modifier = Modifier.fillMaxSize(),
            onEvent = { name, data ->
                if (name == "locationSelected") {
                    val latLng = LatLng(data.optDouble("lat"), data.optDouble("lng"))

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

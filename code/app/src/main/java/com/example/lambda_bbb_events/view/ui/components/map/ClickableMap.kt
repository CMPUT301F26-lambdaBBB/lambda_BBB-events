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

    var markerLocation by remember(initialLocation) { mutableStateOf(initialLocation) }
    // A move the user has since replaced by tapping the map, which must not be made again
    var replacedMove by remember { mutableStateOf<MapMove?>(null) }
    LaunchedEffect(move) {
        if (move != null) {
            markerLocation = move.location
        }
    }
    val pendingMove = move?.takeIf { it !== replacedMove }

    val select = { latLng: LatLng ->
        markerLocation = latLng
        replacedMove = move
        fetchAddressAndPostalCodeFromLatLng(context, latLng) { fetchedAddress, fetchedPostalCode ->
            onLocationSelected(latLng, fetchedAddress, fetchedPostalCode)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        when (MapFallback.shared.mapProvider) {
            MapProvider.GOOGLE ->
                GoogleClickableMap(markerLocation, pendingMove, Modifier.fillMaxSize(), select)
            MapProvider.OPEN_STREET_MAP ->
                OpenStreetClickableMap(markerLocation, pendingMove, Modifier.fillMaxSize(), select)
        }
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

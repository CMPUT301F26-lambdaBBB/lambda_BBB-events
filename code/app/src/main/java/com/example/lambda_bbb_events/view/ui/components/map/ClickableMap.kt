package com.example.lambda_bbb_events.view.ui.components.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.services.MapFallback
import com.example.lambda_bbb_events.model.services.MapProvider
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume


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

    val scope = rememberCoroutineScope()
    var lookup by remember { mutableStateOf<Job?>(null) }

    val select = { latLng: LatLng ->
        markerLocation = latLng
        replacedMove = move
        lookup?.cancel()
        lookup = scope.launch {
            val (fetchedAddress, fetchedPostalCode) = fetchAddressAndPostalCodeFromLatLng(context, latLng)
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

/** The first address Android's geocoder finds at [location], or `null` if it finds none. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private suspend fun geocodeWithListener(geocoder: Geocoder, location: LatLng): Address? =
    suspendCancellableCoroutine { continuation ->
        geocoder.getFromLocation(location.latitude, location.longitude, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) = continuation.resume(addresses.firstOrNull())
            override fun onError(errorMessage: String?) = continuation.resume(null)
        })
    }

/**
 * The first address Android's geocoder finds at [location], or `null` if it finds none. Before
 * Android 13 the geocoder can only wait for its answer, so it is asked on a background thread.
 */
private suspend fun geocodeOnBackgroundThread(geocoder: Geocoder, location: LatLng): Address? =
    withContext(Dispatchers.IO) {
        @Suppress("DEPRECATION")
        geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
    }

/**
 * Looks up the address and postal code at [location] without blocking the main thread.
 *
 * @param context The current Android [Context].
 * @param location The [LatLng] coordinates to reverse-geocode.
 * @return The full address and postal code, or empty strings if none is found or the lookup fails.
 */
suspend fun fetchAddressAndPostalCodeFromLatLng(context: Context, location: LatLng): Pair<String, String> {
    val geocoder = Geocoder(context, Locale.getDefault())
    val address = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocodeWithListener(geocoder, location)
        } else {
            geocodeOnBackgroundThread(geocoder, location)
        }
    } catch (e: IOException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
    return Pair(address?.getAddressLine(0) ?: "", address?.postalCode ?: "")
}

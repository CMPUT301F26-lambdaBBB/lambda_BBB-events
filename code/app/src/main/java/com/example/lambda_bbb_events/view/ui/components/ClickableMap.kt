package com.example.lambda_bbb_events.view.ui.components

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.util.Locale

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
    var selectedPosition by remember { mutableStateOf(initialLocation) }
    val markerState = rememberMarkerState(position = selectedPosition)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 13f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selectedPosition = latLng
                markerState.position = latLng
                
                // Reverse geocode coordinates to retrieve address and postal code
                fetchAddressAndPostalCodeFromLatLng(context, latLng) { fetchedAddress, fetchedPostalCode ->
                    onLocationSelected(latLng, fetchedAddress, fetchedPostalCode)
                }
            }
        ) {
            Marker(
                state = markerState,
                title = "Selected Location",
                snippet = "Lat: %.4f, Lng: %.4f".format(
                    selectedPosition.latitude,
                    selectedPosition.longitude
                ),
                onClick = {
                    fetchAddressAndPostalCodeFromLatLng(context, selectedPosition) { fetchedAddress, fetchedPostalCode ->
                        onLocationSelected(selectedPosition, fetchedAddress, fetchedPostalCode)
                    }
                    false
                }
            )
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

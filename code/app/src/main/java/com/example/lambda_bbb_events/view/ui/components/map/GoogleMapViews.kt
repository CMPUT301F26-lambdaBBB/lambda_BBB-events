package com.example.lambda_bbb_events.view.ui.components.map

import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter

/*
 * Shows Google Maps with the native Maps SDK for Android. The OpenStreetMap versions are in
 * OpenStreetMapPage.kt.
 *
 * No map ID is set and no advanced markers are used, so loading a map stays on Google's free
 * "Maps SDK" SKU instead of being billed as Dynamic Maps.
 *
 * Reference:
 * https://developers.google.com/maps/documentation/android-sdk/maps-compose
 */

/** Zoom used by the location picker, the same as the OpenStreetMap picker. */
private const val PICKER_ZOOM = 13f

/** Colour of the "View campsite" link, the same as on the OpenStreetMap pages. */
private val LINK_COLOR = Color(0xFF2C5C7C)

/** Text size in a campsite's bubble, the same as on the OpenStreetMap pages. */
private val INFO_TEXT_SIZE = 14.sp

/**
 * The map's buttons and gestures. The toolbar would open the Google Maps app, and tilting and
 * rotating only make a flat map of campsites harder to read.
 */
private val MAP_UI = MapUiSettings(
    compassEnabled = false,
    indoorLevelPickerEnabled = false,
    mapToolbarEnabled = false,
    myLocationButtonEnabled = false,
    rotationGesturesEnabled = false,
    tiltGesturesEnabled = false
)

/**
 * A [MapView] that keeps scrolling screens it is in from taking over drags meant for the map.
 */
private class ScrollGuardMapView(context: Context, options: GoogleMapOptions) : MapView(context, options) {
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(event)
    }
}

/**
 * A Google map with the app's settings.
 *
 * @param cameraPositionState Controls where the map looks.
 * @param modifier Sets the size and shape of the map.
 * @param onMapClick Called with the location of each tap on the map.
 * @param onMapLoaded Called once the map has been drawn.
 * @param content The markers on the map.
 */
@Composable
private fun AppGoogleMap(
    cameraPositionState: CameraPositionState,
    modifier: Modifier,
    onMapClick: ((LatLng) -> Unit)? = null,
    onMapLoaded: (() -> Unit)? = null,
    content: @Composable @GoogleMapComposable () -> Unit
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MAP_UI,
        onMapClick = onMapClick,
        onMapLoaded = onMapLoaded,
        mapViewFactory = ::ScrollGuardMapView,
        content = content
    )
}

/** Where the campsite is on the map. */
private fun Campsite.position(): LatLng = LatLng(latitude, longitude)

/**
 * Moves [map] to show every campsite, or to zoom in on the campsite when there is only one.
 *
 * @param paddingPx Space kept between the outermost campsites and the edge of the map.
 */
private fun frameCampsites(map: GoogleMap, campsites: List<Campsite>, paddingPx: Int) {
    if (campsites.size == 1) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(campsites[0].position(), SINGLE_CAMPSITE_ZOOM.toFloat()))
        return
    }
    val bounds = LatLngBounds.builder()
    campsites.forEach { bounds.include(it.position()) }
    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), paddingPx))
    if (map.cameraPosition.zoom > MAX_FRAMING_ZOOM) {
        map.moveCamera(CameraUpdateFactory.zoomTo(MAX_FRAMING_ZOOM.toFloat()))
    }
}

/**
 * The bubble shown above a tapped campsite marker.
 *
 * @param showLink Whether to show "View campsite", for maps where tapping the bubble opens it.
 */
@Composable
private fun CampsiteInfo(campsite: Campsite, showLink: Boolean) {
    Column(modifier = Modifier.widthIn(max = 220.dp).padding(4.dp)) {
        Text(text = campsite.campsiteName, fontSize = INFO_TEXT_SIZE, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = campsite.address, fontSize = INFO_TEXT_SIZE, color = Color.DarkGray)
        if (showLink) {
            Text(
                text = "View campsite",
                fontSize = INFO_TEXT_SIZE,
                fontWeight = FontWeight.SemiBold,
                color = LINK_COLOR,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

/**
 * The Google version of [CampsiteMap]: a marker for each campsite, framed to show them all.
 *
 * The map is framed again whenever the campsites change.
 *
 * @param campsites The campsites to mark, all with a location.
 * @param modifier Sets the size and shape of the map.
 * @param onCampsiteClick Called when a campsite's bubble is tapped, or `null` to do nothing.
 */
@OptIn(MapsComposeExperimentalApi::class)
@Composable
internal fun GoogleCampsiteMap(
    campsites: List<Campsite>,
    modifier: Modifier,
    onCampsiteClick: ((Campsite) -> Unit)?
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(campsites[0].position(), SINGLE_CAMPSITE_ZOOM.toFloat())
    }
    val paddingPx = with(LocalDensity.current) { BOUNDS_PADDING.dp.roundToPx() }
    var isLoaded by remember { mutableStateOf(false) }

    AppGoogleMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier,
        onMapLoaded = { isLoaded = true }
    ) {
        MapEffect(isLoaded, campsites) { map ->
            if (isLoaded) {
                frameCampsites(map, campsites, paddingPx)
            }
        }
        for (campsite in campsites) {
            key(campsite.campsiteId) {
                MarkerInfoWindowContent(
                    state = rememberUpdatedMarkerState(campsite.position()),
                    title = campsite.campsiteName,
                    onInfoWindowClick = { onCampsiteClick?.invoke(campsite) }
                ) {
                    CampsiteInfo(campsite, showLink = onCampsiteClick != null)
                }
            }
        }
    }
}

/**
 * Calls [onSelect] with the marker's location each time the user finishes dragging it.
 */
@Composable
private fun ReportMarkerDrags(markerState: MarkerState, onSelect: (LatLng) -> Unit) {
    val currentOnSelect by rememberUpdatedState(onSelect)
    LaunchedEffect(markerState) {
        snapshotFlow { markerState.isDragging }
            .drop(1)
            .filter { isDragging -> !isDragging }
            .collect { currentOnSelect(markerState.position) }
    }
}

/**
 * The Google version of the map in [ClickableMap]: one draggable marker, which tapping the map
 * also moves.
 *
 * @param start Where the marker and map start.
 * @param move Moves the marker and map when it changes, without calling [onSelect].
 * @param modifier Sets the size and shape of the map.
 * @param onSelect Called with the marker's location when the user taps the map or drops the marker.
 */
@Composable
internal fun GoogleClickableMap(
    start: LatLng,
    move: MapMove?,
    modifier: Modifier,
    onSelect: (LatLng) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(start, PICKER_ZOOM)
    }
    val markerState = remember { MarkerState(start) }

    LaunchedEffect(move) {
        if (move != null) {
            markerState.position = move.location
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(move.location, PICKER_ZOOM))
        }
    }
    ReportMarkerDrags(markerState, onSelect)

    AppGoogleMap(
        cameraPositionState = cameraPositionState,
        modifier = modifier,
        onMapClick = { location ->
            markerState.position = location
            onSelect(location)
        }
    ) {
        Marker(state = markerState, title = "Selected Location", draggable = true)
    }
}

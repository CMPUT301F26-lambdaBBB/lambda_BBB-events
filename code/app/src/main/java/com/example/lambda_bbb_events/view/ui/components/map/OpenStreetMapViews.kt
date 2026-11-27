package com.example.lambda_bbb_events.view.ui.components.map

import android.content.Context
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.services.MAP_USER_AGENT
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import com.google.android.gms.maps.model.LatLng as GoogleLatLng

/*
 * Shows OpenStreetMap maps natively with MapLibre, for when there is no Google API key. The
 * Google versions are in GoogleMapViews.kt.
 *
 * The tile policy is at https://operations.osmfoundation.org/policies/tiles/.
 *
 * Reference:
 * https://maplibre.org/maplibre-native/android/api/
 */

/** OpenStreetMap's tile server. */
private const val OSM_TILE_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"

/** The closest zoom OpenStreetMap draws tiles for. */
private const val OSM_MAX_ZOOM = 19f

/** Size in pixels of OpenStreetMap's tiles. */
private const val OSM_TILE_SIZE = 256

/** Zoom used by the location picker, the same as the Google picker. */
private const val OSM_PICKER_ZOOM = 13.0

/** The page OpenStreetMap's credit links to. */
private const val OSM_COPYRIGHT_URL = "https://www.openstreetmap.org/copyright"

/** Colour of the "View campsite" link, the same as on the Google map. */
private const val OSM_LINK_COLOR = 0xFF2C5C7C.toInt()

/** Text size in sp in a campsite's bubble, the same as on the Google map. */
private const val OSM_INFO_TEXT_SIZE = 14f

/**
 * Sends tile requests with the app's own User-Agent. OpenStreetMap blocks MapLibre's default
 * one, because its tile policy requires each app to identify itself.
 */
private val osmHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header("User-Agent", MAP_USER_AGENT).build())
        }
        .build()
}

/** Starts MapLibre, which must happen before a map view is made. */
private fun startMapLibre(context: Context) {
    MapLibre.getInstance(context)
    HttpRequestUtil.setOkHttpClient(osmHttpClient)
}

/** Converts to the LatLng MapLibre uses. */
private fun GoogleLatLng.toMapLibre(): LatLng = LatLng(latitude, longitude)

/** Converts to the LatLng the rest of the app uses. */
private fun LatLng.toGoogle(): GoogleLatLng = GoogleLatLng(latitude, longitude)

/** Where the campsite is on the map. */
private fun Campsite.osmPosition(): LatLng = LatLng(latitude, longitude)

/** A map style with only OpenStreetMap's tiles in it. */
private fun osmStyle(): Style.Builder {
    val tiles = TileSet("2.2.0", OSM_TILE_URL)
    tiles.maxZoom = OSM_MAX_ZOOM
    return Style.Builder()
        .withSource(RasterSource("osm", tiles, OSM_TILE_SIZE))
        .withLayer(RasterLayer("osm", "osm"))
}

/**
 * The map's settings. MapLibre's own logo and credit button are hidden because [OsmMap] shows
 * OpenStreetMap's credit, and tilting and rotating only make a flat map harder to read.
 */
private fun osmMapOptions(context: Context, start: CameraPosition): MapLibreMapOptions =
    MapLibreMapOptions.createFromAttributes(context)
        .camera(start)
        .textureMode(true)
        .logoEnabled(false)
        .attributionEnabled(false)
        .compassEnabled(false)
        .rotateGesturesEnabled(false)
        .tiltGesturesEnabled(false)

/**
 * A [MapView] that keeps scrolling screens it is in from taking over drags meant for the map.
 */
private class ScrollGuardOsmMapView(context: Context, options: MapLibreMapOptions) : MapView(context, options) {
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(event)
    }
}

/** Passes a screen's lifecycle [event] on to [mapView], which needs it to draw and to clean up. */
private fun forwardLifecycle(mapView: MapView, event: Lifecycle.Event) {
    when (event) {
        Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
        Lifecycle.Event.ON_START -> mapView.onStart()
        Lifecycle.Event.ON_RESUME -> mapView.onResume()
        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
        Lifecycle.Event.ON_STOP -> mapView.onStop()
        else -> Unit
    }
}

/** Takes [mapView] down from [state] and frees it, once the map has left the screen. */
private fun closeMapView(mapView: MapView, state: Lifecycle.State) {
    if (state.isAtLeast(Lifecycle.State.RESUMED)) {
        mapView.onPause()
    }
    if (state.isAtLeast(Lifecycle.State.STARTED)) {
        mapView.onStop()
    }
    mapView.onDestroy()
}

/**
 * Makes a MapLibre map view that follows the screen's lifecycle and is freed when it leaves
 * the screen.
 *
 * @param start Where the map looks first.
 */
@Composable
private fun rememberOsmMapView(start: CameraPosition): MapView {
    val context = LocalContext.current
    val mapView = remember {
        startMapLibre(context)
        ScrollGuardOsmMapView(context, osmMapOptions(context, start))
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event -> forwardLifecycle(mapView, event) }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            closeMapView(mapView, lifecycle.currentState)
        }
    }
    return mapView
}

/**
 * OpenStreetMap's credit, which its licence requires to stay visible. Tapping it opens
 * OpenStreetMap's copyright page.
 */
@Composable
private fun OsmCredit(modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = "© OpenStreetMap contributors",
        fontSize = 11.sp,
        color = Color.DarkGray,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.8f))
            .clickable { uriHandler.openUri(OSM_COPYRIGHT_URL) }
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}

/**
 * An OpenStreetMap map with the app's settings. [onReady] is called once, when the tiles have
 * been added and markers can be placed.
 *
 * @param start Where the map looks first.
 * @param modifier Sets the size and shape of the map.
 * @param onReady Sets up the map's markers and listeners.
 */
@Composable
private fun OsmMap(start: CameraPosition, modifier: Modifier, onReady: (MapLibreMap) -> Unit) {
    val mapView = rememberOsmMapView(start)
    val currentOnReady by rememberUpdatedState(onReady)
    LaunchedEffect(mapView) {
        mapView.getMapAsync { map -> map.setStyle(osmStyle()) { currentOnReady(map) } }
    }
    Box(modifier = modifier) {
        AndroidView(factory = { mapView }, modifier = Modifier.matchParentSize())
        OsmCredit(Modifier.align(Alignment.BottomEnd))
    }
}

/** Makes a line of text for a campsite's bubble. */
private fun infoLine(context: Context, text: String, color: Int, style: Int): TextView {
    val line = TextView(context)
    line.text = text
    line.textSize = OSM_INFO_TEXT_SIZE
    line.setTextColor(color)
    line.setTypeface(null, style)
    return line
}

/**
 * The bubble shown above a tapped campsite marker, laid out like the Google map's.
 *
 * @param showLink Whether to show "View campsite", for maps where tapping the bubble opens it.
 */
private fun campsiteInfoView(context: Context, campsite: Campsite, showLink: Boolean): View {
    val padding = (8 * context.resources.displayMetrics.density).toInt()
    val bubble = LinearLayout(context)
    bubble.orientation = LinearLayout.VERTICAL
    bubble.setBackgroundColor(android.graphics.Color.WHITE)
    bubble.setPadding(padding, padding, padding, padding)
    bubble.addView(infoLine(context, campsite.campsiteName, android.graphics.Color.BLACK, Typeface.BOLD))
    bubble.addView(infoLine(context, campsite.address, android.graphics.Color.DKGRAY, Typeface.NORMAL))
    if (showLink) {
        val link = infoLine(context, "View campsite", OSM_LINK_COLOR, Typeface.BOLD)
        link.setPadding(0, padding, 0, 0)
        bubble.addView(link)
    }
    return bubble
}

/**
 * Moves [map] to show every campsite, or to zoom in on the campsite when there is only one.
 *
 * @param paddingPx Space kept between the outermost campsites and the edge of the map.
 */
private fun frameOsmCampsites(map: MapLibreMap, campsites: List<Campsite>, paddingPx: Int) {
    if (campsites.size == 1) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(campsites[0].osmPosition(), SINGLE_CAMPSITE_ZOOM.toDouble()))
        return
    }
    val bounds = LatLngBounds.Builder()
    campsites.forEach { bounds.include(it.osmPosition()) }
    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), paddingPx))
    if (map.cameraPosition.zoom > MAX_FRAMING_ZOOM) {
        map.moveCamera(CameraUpdateFactory.zoomTo(MAX_FRAMING_ZOOM.toDouble()))
    }
}

/**
 * Adds a marker for each campsite to [map], with a bubble that calls [onCampsiteClick] when
 * tapped.
 */
private fun addCampsiteMarkers(
    context: Context,
    map: MapLibreMap,
    campsites: List<Campsite>,
    onCampsiteClick: ((Campsite) -> Unit)?
) {
    val byMarker = mutableMapOf<Marker, Campsite>()
    for (campsite in campsites) {
        val marker = map.addMarker(MarkerOptions().position(campsite.osmPosition()).title(campsite.campsiteName))
        byMarker[marker] = campsite
    }
    map.setInfoWindowAdapter { marker ->
        byMarker[marker]?.let { campsiteInfoView(context, it, showLink = onCampsiteClick != null) }
    }
    map.setOnInfoWindowClickListener { marker ->
        byMarker[marker]?.let { onCampsiteClick?.invoke(it) }
        true
    }
}

/**
 * The OpenStreetMap version of [GoogleCampsiteMap]: a marker for each campsite, framed to show
 * them all.
 *
 * @param campsites The campsites to mark, all with a location.
 * @param modifier Sets the size and shape of the map.
 * @param onCampsiteClick Called when a campsite's bubble is tapped, or `null` to do nothing.
 */
@Composable
internal fun OpenStreetCampsiteMap(
    campsites: List<Campsite>,
    modifier: Modifier,
    onCampsiteClick: ((Campsite) -> Unit)?
) {
    val context = LocalContext.current
    val paddingPx = with(LocalDensity.current) { BOUNDS_PADDING.dp.roundToPx() }
    val currentOnCampsiteClick by rememberUpdatedState(onCampsiteClick)
    val start = CameraPosition.Builder()
        .target(campsites[0].osmPosition())
        .zoom(SINGLE_CAMPSITE_ZOOM.toDouble())
        .build()

    key(campsites) {
        OsmMap(start, modifier) { map ->
            addCampsiteMarkers(context, map, campsites) { currentOnCampsiteClick?.invoke(it) }
            frameOsmCampsites(map, campsites, paddingPx)
        }
    }
}

/**
 * The OpenStreetMap version of [GoogleClickableMap]: one marker, which tapping the map moves.
 *
 * @param start Where the marker and map start.
 * @param move Moves the marker and map when it changes, without calling [onSelect].
 * @param modifier Sets the size and shape of the map.
 * @param onSelect Called with the marker's location when the user taps the map.
 */
@Composable
internal fun OpenStreetClickableMap(
    start: GoogleLatLng,
    move: MapMove?,
    modifier: Modifier,
    onSelect: (GoogleLatLng) -> Unit
) {
    val currentOnSelect by rememberUpdatedState(onSelect)
    var ready by remember { mutableStateOf<Pair<MapLibreMap, Marker>?>(null) }
    val camera = CameraPosition.Builder().target(start.toMapLibre()).zoom(OSM_PICKER_ZOOM).build()

    LaunchedEffect(ready, move) {
        val (map, marker) = ready ?: return@LaunchedEffect
        if (move != null) {
            marker.position = move.location.toMapLibre()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(move.location.toMapLibre(), OSM_PICKER_ZOOM))
        }
    }

    OsmMap(camera, modifier) { map ->
        val marker = map.addMarker(MarkerOptions().position(start.toMapLibre()))
        map.addOnMapClickListener { location ->
            marker.position = location
            currentOnSelect(location.toGoogle())
            true
        }
        ready = map to marker
    }
}

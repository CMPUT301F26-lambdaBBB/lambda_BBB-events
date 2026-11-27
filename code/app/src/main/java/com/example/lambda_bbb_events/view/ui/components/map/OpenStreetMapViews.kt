package com.example.lambda_bbb_events.view.ui.components.map

import android.content.Context
import android.graphics.RectF
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.services.MAP_USER_AGENT
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import com.google.android.gms.maps.model.LatLng as GoogleLatLng

/*
 * Shows OpenStreetMap maps natively with MapLibre, for when there is no Google API key. The
 * Google versions are in GoogleMapViews.kt.
 *
 * Markers are drawn as a circle layer over the tiles, which is MapLibre's current way of
 * marking points.
 *
 * The tile policy is at https://operations.osmfoundation.org/policies/tiles/.
 */

/** Zoom used by the location picker, the same as the Google picker. */
private const val OSM_PICKER_ZOOM = 13.0

/** The ID of the source and layer that hold the markers. */
private const val MARKERS = "markers"

/** How far in pixels from a marker a tap still selects it. */
private const val TAP_SLOP_PX = 32f

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

/** A point on the map, as the marker source needs it. */
private fun markerPoint(latitude: Double, longitude: Double): Point = Point.fromLngLat(longitude, latitude)

/** Where the campsite is on the map. */
private fun Campsite.osmPosition(): LatLng = LatLng(latitude, longitude)

/** OpenStreetMap's tiles, with an empty marker layer drawn over them. */
private fun osmStyle(): Style.Builder {
    val tiles = TileSet("2.2.0", "https://tile.openstreetmap.org/{z}/{x}/{y}.png")
    tiles.maxZoom = 19f
    val markers = CircleLayer(MARKERS, MARKERS).withProperties(
        circleRadius(9f),
        circleColor("#2C5C7C"),
        circleStrokeColor("#FFFFFF"),
        circleStrokeWidth(2f)
    )
    return Style.Builder()
        .withSource(RasterSource("osm", tiles, 256))
        .withLayer(RasterLayer("osm", "osm"))
        .withSource(GeoJsonSource(MARKERS))
        .withLayer(markers)
}

/**
 * A [MapView] that keeps scrolling screens it is in from taking over drags meant for the map.
 * MapLibre's logo and credit button are hidden because [OsmMap] shows OpenStreetMap's credit,
 * and tilting and rotating only make a flat map harder to read.
 */
private class OsmMapView(context: Context, start: CameraPosition) : MapView(
    context,
    MapLibreMapOptions.createFromAttributes(context)
        .camera(start)
        .textureMode(true)
        .logoEnabled(false)
        .attributionEnabled(false)
        .compassEnabled(false)
        .rotateGesturesEnabled(false)
        .tiltGesturesEnabled(false)
) {
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(event)
    }
}

/**
 * An OpenStreetMap map with the app's settings and OpenStreetMap's credit, which its licence
 * requires to stay visible. [onReady] is called once the style has loaded, with the source that
 * holds the markers.
 *
 * @param start Where the map looks first.
 * @param modifier Sets the size and shape of the map.
 * @param overlay Drawn on top of the map.
 * @param onReady Sets up the map's markers and listeners.
 */
@Composable
private fun OsmMap(
    start: CameraPosition,
    modifier: Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    onReady: (MapLibreMap, GeoJsonSource) -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val currentOnReady by rememberUpdatedState(onReady)
    val mapView = remember {
        MapLibre.getInstance(context)
        HttpRequestUtil.setOkHttpClient(osmHttpClient)
        OsmMapView(context, start)
    }
    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.getMapAsync { map ->
            map.setStyle(osmStyle()) { style ->
                style.getSourceAs<GeoJsonSource>(MARKERS)?.let { currentOnReady(map, it) }
            }
        }
        onDispose {
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    Box(modifier = modifier) {
        AndroidView(factory = { mapView }, modifier = Modifier.matchParentSize())
        Text(
            text = "© OpenStreetMap contributors",
            fontSize = 11.sp,
            color = Color.DarkGray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.White.copy(alpha = 0.8f))
                .clickable { uriHandler.openUri("https://www.openstreetmap.org/copyright") }
                .padding(horizontal = 4.dp, vertical = 1.dp)
        )
        overlay()
    }
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

/** The markers for [campsites], each tagged with its campsite's ID. */
private fun campsiteFeatures(campsites: List<Campsite>): FeatureCollection =
    FeatureCollection.fromFeatures(
        campsites.map { campsite ->
            val feature = Feature.fromGeometry(markerPoint(campsite.latitude, campsite.longitude))
            feature.addStringProperty("id", campsite.campsiteId)
            feature
        }
    )

/** The campsite whose marker is at [location] on [map], or `null` if the tap missed them all. */
private fun campsiteAt(map: MapLibreMap, location: LatLng, campsites: List<Campsite>): Campsite? {
    val point = map.projection.toScreenLocation(location)
    val area = RectF(point.x - TAP_SLOP_PX, point.y - TAP_SLOP_PX, point.x + TAP_SLOP_PX, point.y + TAP_SLOP_PX)
    val id = map.queryRenderedFeatures(area, MARKERS).firstOrNull()?.getStringProperty("id")
    return campsites.find { it.campsiteId == id }
}

/**
 * The card shown over the map for a tapped campsite, with the same text as the Google map's
 * bubble.
 *
 * @param onClick Called when "View campsite" is tapped, or `null` to leave the link out.
 */
@Composable
private fun CampsiteCard(campsite: Campsite, onClick: ((Campsite) -> Unit)?, modifier: Modifier) {
    Card(modifier = modifier.widthIn(max = 240.dp).testTag("osm_campsite_card")) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = campsite.campsiteName, fontWeight = FontWeight.Bold)
            Text(text = campsite.address, style = MaterialTheme.typography.bodyMedium)
            if (onClick != null) {
                Text(
                    text = "View campsite",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable { onClick(campsite) }
                )
            }
        }
    }
}

/**
 * The OpenStreetMap version of [GoogleCampsiteMap]: a marker for each campsite, framed to show
 * them all. Tapping a marker shows the campsite's name and address over the map.
 *
 * @param campsites The campsites to mark, all with a location.
 * @param modifier Sets the size and shape of the map.
 * @param onCampsiteClick Called when "View campsite" is tapped, or `null` to leave it out.
 */
@Composable
internal fun OpenStreetCampsiteMap(
    campsites: List<Campsite>,
    modifier: Modifier,
    onCampsiteClick: ((Campsite) -> Unit)?
) {
    val paddingPx = with(LocalDensity.current) { BOUNDS_PADDING.dp.roundToPx() }
    var selected by remember(campsites) { mutableStateOf<Campsite?>(null) }
    val start = CameraPosition.Builder().target(campsites[0].osmPosition()).zoom(SINGLE_CAMPSITE_ZOOM.toDouble()).build()

    key(campsites) {
        OsmMap(
            start = start,
            modifier = modifier,
            overlay = {
                selected?.let { CampsiteCard(it, onCampsiteClick, Modifier.align(Alignment.TopCenter).padding(8.dp)) }
            }
        ) { map, markers ->
            markers.setGeoJson(campsiteFeatures(campsites))
            map.addOnMapClickListener { location ->
                selected = campsiteAt(map, location, campsites)
                true
            }
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
    var ready by remember { mutableStateOf<Pair<MapLibreMap, GeoJsonSource>?>(null) }

    LaunchedEffect(ready, move) {
        val (map, marker) = ready ?: return@LaunchedEffect
        val location = move?.location ?: return@LaunchedEffect
        marker.setGeoJson(markerPoint(location.latitude, location.longitude))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), OSM_PICKER_ZOOM))
    }

    val camera = CameraPosition.Builder().target(LatLng(start.latitude, start.longitude)).zoom(OSM_PICKER_ZOOM).build()
    OsmMap(camera, modifier) { map, marker ->
        marker.setGeoJson(markerPoint(start.latitude, start.longitude))
        map.addOnMapClickListener { location ->
            marker.setGeoJson(markerPoint(location.latitude, location.longitude))
            currentOnSelect(GoogleLatLng(location.latitude, location.longitude))
            true
        }
        ready = map to marker
    }
}

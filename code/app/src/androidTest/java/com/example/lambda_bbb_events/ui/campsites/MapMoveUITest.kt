package com.example.lambda_bbb_events.ui.campsites

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.view.ui.components.map.MapCommand
import com.example.lambda_bbb_events.view.ui.components.map.MapsJavaScriptView
import com.example.lambda_bbb_events.view.ui.components.map.clickableMapScript
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests moving the marker of the location map from the app. The page's script runs in a real
 * WebView with a stand-in for Google's library, so nothing is loaded from Google.
 */
@RunWith(AndroidJUnit4::class)
class MapMoveUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<Pair<String, JSONObject>>()
    private var command by mutableStateOf<MapCommand?>(null)

    private val page = """
        <html><body><div id="map"></div><script>
        function send(name, data) { Android.post(name, JSON.stringify(data || {})); }
        class Map {
          addListener() {}
          setCenter(position) { send('centered', position); }
          setZoom() {}
        }
        class AdvancedMarkerElement {
          addListener() {}
          set position(position) { send('markerMoved', position); }
        }
        const google = { maps: { importLibrary: async () => ({ Map, AdvancedMarkerElement }) } };
        ${clickableMapScript(LatLng(51.0, -115.0))}
        initMap();
        </script></body></html>
    """.trimIndent()

    private fun named(name: String) = events.filter { it.first == name }.map { it.second }

    private fun openMap() {
        composeTestRule.setContent {
            MapsJavaScriptView(
                html = page,
                modifier = Modifier.size(200.dp),
                command = command,
                onEvent = { name, data -> events.add(name to data) }
            )
        }
        composeTestRule.waitUntil(10_000) { named("mapReady").isNotEmpty() }
    }

    private fun move(count: Int) {
        composeTestRule.runOnIdle { command = MapCommand("showLocation(52.9, -118.1);") }
        composeTestRule.waitUntil(10_000) { named("markerMoved").size == count }
    }

    @Test
    fun moveShowsTheLocationWithoutSelectingIt() {
        openMap()
        move(count = 1)
        composeTestRule.waitUntil(10_000) { named("centered").isNotEmpty() }

        for (position in named("markerMoved") + named("centered")) {
            assertEquals(52.9, position.getDouble("lat"), 0.0)
            assertEquals(-118.1, position.getDouble("lng"), 0.0)
        }
        assertFalse(events.any { it.first == "locationSelected" })
    }

    @Test
    fun movingToTheSamePlaceAgainMovesTheMarkerAgain() {
        openMap()
        move(count = 1)
        move(count = 2)
    }
}

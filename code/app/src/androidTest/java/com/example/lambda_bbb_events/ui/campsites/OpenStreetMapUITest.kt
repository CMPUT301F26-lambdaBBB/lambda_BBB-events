package com.example.lambda_bbb_events.ui.campsites

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.services.MapProvider
import com.example.lambda_bbb_events.view.ui.components.map.MapCommand
import com.example.lambda_bbb_events.view.ui.components.map.MapsJavaScriptView
import com.example.lambda_bbb_events.view.ui.components.map.osmCampsiteMapScript
import com.example.lambda_bbb_events.view.ui.components.map.osmClickableMapScript
import com.example.lambda_bbb_events.view.ui.components.map.osmPageHtml
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the OpenStreetMap pages in a real WebView with the bundled Leaflet. The map images are
 * left out, so nothing is loaded from OpenStreetMap.
 */
@RunWith(AndroidJUnit4::class)
class OpenStreetMapUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<Pair<String, JSONObject>>()
    private var command by mutableStateOf<MapCommand?>(null)

    private fun named(name: String) = events.filter { it.first == name }.map { it.second }

    private fun show(script: String) {
        val page = osmPageHtml(script).replace("https://tile.openstreetmap.org/{z}/{x}/{y}.png", "data:,")
        composeTestRule.setContent {
            MapsJavaScriptView(
                html = page,
                modifier = Modifier.size(300.dp).testTag("map"),
                provider = MapProvider.OPEN_STREET_MAP,
                command = command,
                onEvent = { name, data -> events.add(name to data) }
            )
        }
    }

    private fun waitFor(name: String) {
        composeTestRule.waitUntil(15_000) { named(name).isNotEmpty() }
    }

    private fun run(script: String) {
        composeTestRule.runOnIdle { command = MapCommand(script) }
    }

    private fun showLocationMap() {
        show(osmClickableMapScript(LatLng(51.0, -115.0)))
        waitFor("mapReady")
    }

    @Test
    fun tappingTheMapSelectsThatLocation() {
        showLocationMap()
        composeTestRule.onNodeWithTag("map").performTouchInput { click(percentOffset(0.75f, 0.75f)) }
        waitFor("locationSelected")

        // Down and to the right of where the map starts
        val location = named("locationSelected").last()
        assertTrue("$location", location.getDouble("lat") in 50.9..50.9999)
        assertTrue("$location", location.getDouble("lng") in -114.9999..-114.9)
    }

    @Test
    fun movingTheMapDoesNotSelectTheLocation() {
        showLocationMap()
        run("showLocation(52.9, -118.1); send('moved', { html: document.querySelectorAll('.leaflet-marker-icon').length });")
        waitFor("moved")
        assertFalse(events.any { it.first == "locationSelected" })

        // The map is now centred on the new location, so tapping the middle area selects near it
        composeTestRule.onNodeWithTag("map").performTouchInput { click(percentOffset(0.6f, 0.6f)) }
        waitFor("locationSelected")
        val location = named("locationSelected").last()
        assertEquals(52.9, location.getDouble("lat"), 0.05)
        assertEquals(-118.1, location.getDouble("lng"), 0.05)
    }

    @Test
    fun openStreetMapIsCredited() {
        showLocationMap()
        run("send('credit', { text: document.querySelector('.leaflet-control-attribution').textContent });")
        waitFor("credit")

        assertTrue(named("credit").last().getString("text").contains("OpenStreetMap contributors"))
    }

    @Test
    fun campsiteMarkersOpenTheCampsite() {
        val sneaky = "Bob's \"Camp\" </script><script>send('hacked')</script>"
        show(
            osmCampsiteMapScript(
                listOf(
                    Campsite(campsiteId = "a", campsiteName = sneaky, address = "1 Lake Rd", latitude = 53.5, longitude = -113.5),
                    Campsite(campsiteId = "b", campsiteName = "Forest", address = "9 Pine St", latitude = 51.2, longitude = -115.6)
                )
            ) + "\nconst drawn = initMap; initMap = function () { drawn(); send('drawn'); };"
        )
        waitFor("drawn")
        run(
            "const pins = document.querySelectorAll('.leaflet-marker-icon'); pins[0].click();" +
                    "send('pins', { count: pins.length, name: document.querySelector('.info b').textContent });" +
                    "document.querySelector('.info a').click();"
        )
        waitFor("campsiteClick")

        assertEquals(2, named("pins").last().getInt("count"))
        assertEquals(sneaky, named("pins").last().getString("name"))
        assertEquals("a", named("campsiteClick").last().getString("id"))
        assertFalse(events.any { it.first == "hacked" })
    }
}

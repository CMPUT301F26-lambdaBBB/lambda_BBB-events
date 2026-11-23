package com.example.lambda_bbb_events

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.view.ui.components.map.campsiteMapScript
import com.example.lambda_bbb_events.view.ui.components.map.clickableMapScript
import com.example.lambda_bbb_events.view.ui.components.map.mapPageHtml
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the Maps JavaScript API pages shown in the app's map WebViews.
 */
@RunWith(AndroidJUnit4::class)
class MapPageTest {

    private fun campsitesIn(script: String): JSONArray =
        JSONArray(script.substringAfter("const campsites = ").substringBefore(";\n"))

    @Test
    fun pageLoadsJavaScriptApiWithKeyAndHandlesAuthFailure() {
        val html = mapPageHtml("async function initMap() {}")

        assertTrue(html.contains("https://maps.googleapis.com/maps/api/js?key=${BuildConfig.MAPS_API_KEY}"))
        assertTrue(html.contains("callback=initMap"))
        assertTrue(html.contains("window.gm_authFailure"))
        assertTrue(html.contains("async function initMap() {}"))
    }

    @Test
    fun campsiteScriptListsEveryCampsite() {
        val campsites = listOf(
            Campsite(campsiteId = "a", campsiteName = "Lakeside", address = "1 Lake Rd", latitude = 53.5, longitude = -113.5),
            Campsite(campsiteId = "b", campsiteName = "Forest", address = "9 Pine St", latitude = 51.2, longitude = -115.6)
        )

        val parsed = campsitesIn(campsiteMapScript(campsites))

        assertEquals(2, parsed.length())
        assertEquals("a", parsed.getJSONObject(0).getString("id"))
        assertEquals("Lakeside", parsed.getJSONObject(0).getString("name"))
        assertEquals(53.5, parsed.getJSONObject(0).getDouble("lat"), 0.0)
        assertEquals(-115.6, parsed.getJSONObject(1).getDouble("lng"), 0.0)
    }

    @Test
    fun campsiteNamesCannotBreakOutOfThePageScript() {
        val sneaky = Campsite(
            campsiteId = "x",
            campsiteName = "Bob's \"Camp\" </script><script>alert(1)</script>",
            address = "Line 1\nLine 2",
            latitude = 1.0,
            longitude = 2.0
        )

        val script = campsiteMapScript(listOf(sneaky))

        assertFalse(script.contains("</script>"))
        assertEquals(sneaky.campsiteName, campsitesIn(script).getJSONObject(0).getString("name"))
        assertEquals(sneaky.address, campsitesIn(script).getJSONObject(0).getString("address"))
    }

    @Test
    fun pickerScriptStartsAtGivenLocation() {
        val script = clickableMapScript(LatLng(53.5461, -113.4938))

        assertTrue(script.contains("const start = { lat: 53.5461, lng: -113.4938 };"))
        assertTrue(script.contains("send('locationSelected'"))
    }
}

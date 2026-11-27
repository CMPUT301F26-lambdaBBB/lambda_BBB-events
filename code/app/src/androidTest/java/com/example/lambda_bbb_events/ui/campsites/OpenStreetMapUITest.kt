package com.example.lambda_bbb_events.ui.campsites

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.view.ui.components.map.OpenStreetCampsiteMap
import com.example.lambda_bbb_events.view.ui.components.map.OpenStreetClickableMap
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the native OpenStreetMap maps drawn by MapLibre.
 */
@RunWith(AndroidJUnit4::class)
class OpenStreetMapUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val start = LatLng(51.1869, -115.5323)

    private val campsites = listOf(
        Campsite(campsiteId = "a", campsiteName = "Lake", address = "1 Lake Rd", latitude = 51.18, longitude = -115.53),
        Campsite(campsiteId = "b", campsiteName = "Ridge", address = "2 Ridge Rd", latitude = 51.25, longitude = -115.6)
    )

    /**
     * Taps the picker beside its marker until the map is ready and reports the tap, then returns
     * where it was reported.
     */
    private fun tapPickerUntilSelected(selected: () -> LatLng?): LatLng? {
        repeat(20) {
            composeTestRule.onNodeWithTag("osm_picker").performTouchInput { click(centerLeft + (center - centerLeft) / 2f) }
            composeTestRule.waitForIdle()
            selected()?.let { return it }
            Thread.sleep(500)
        }
        return null
    }

    @Test
    fun campsiteMapShowsOpenStreetMapCredit() {
        composeTestRule.setContent {
            OpenStreetCampsiteMap(campsites, Modifier.size(300.dp), onCampsiteClick = null)
        }

        composeTestRule.onNodeWithText("© OpenStreetMap contributors").assertIsDisplayed()
    }

    @Test
    fun tappingThePickerSelectsWhereItWasTapped() {
        var selected: LatLng? = null
        composeTestRule.setContent {
            OpenStreetClickableMap(start, null, Modifier.size(300.dp).testTag("osm_picker")) { selected = it }
        }

        val tapped = tapPickerUntilSelected { selected }

        assertNotNull(tapped)
        assertEquals(start.latitude, tapped!!.latitude, 0.01)
        assertTrue(tapped.longitude < start.longitude)
        assertTrue(tapped.longitude > start.longitude - 0.05)
    }
}

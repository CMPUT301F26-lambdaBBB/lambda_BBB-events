package com.example.lambda_bbb_events.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.services.PlaceDetails
import com.example.lambda_bbb_events.model.services.PlaceSuggestion
import com.example.lambda_bbb_events.model.services.parsePlace
import com.example.lambda_bbb_events.model.services.parseSuggestions
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Tests reading responses from the Places API. JSON is only available on a device, so these
 * are not unit tests. Nothing is sent to Google.
 */
@RunWith(AndroidJUnit4::class)
class PlaceSearchParsingTest {

    @Test
    fun suggestionsAreReadInOrderAndIncompleteOnesSkipped() {
        val response = JSONObject(
            """{"suggestions": [
                {"placePrediction": {"placeId": "a", "structuredFormat": {
                    "mainText": {"text": "Banff National Park"},
                    "secondaryText": {"text": "AB, Canada"}}}},
                {"queryPrediction": {"text": {"text": "banff food"}}},
                {"placePrediction": {"placeId": "no-name"}},
                {"placePrediction": {"placeId": "b", "structuredFormat": {
                    "mainText": {"text": "Canada"}}}}
            ]}"""
        )

        assertEquals(
            listOf(
                PlaceSuggestion("a", "Banff National Park", "AB, Canada"),
                PlaceSuggestion("b", "Canada", "")
            ),
            parseSuggestions(response)
        )
    }

    @Test
    fun noMatchesGiveAnEmptyList() {
        assertEquals(emptyList<PlaceSuggestion>(), parseSuggestions(JSONObject("{}")))
    }

    @Test
    fun placeIsReadWithItsPostalCode() {
        val response = JSONObject(
            """{"formattedAddress": "1 Lake Rd, AB T0L, Canada",
                "location": {"latitude": 51.5, "longitude": -115.9},
                "addressComponents": [
                    {"longText": "Canada", "types": ["country", "political"]},
                    {"longText": "T0L", "types": ["postal_code"]}]}"""
        )

        assertEquals(
            PlaceDetails(51.5, -115.9, "1 Lake Rd, AB T0L, Canada", "T0L"),
            parsePlace(response)
        )
    }

    @Test
    fun placeWithoutAddressIsStillRead() {
        val response = JSONObject("""{"location": {"latitude": 51.5, "longitude": -115.9}}""")

        assertEquals(PlaceDetails(51.5, -115.9, "", ""), parsePlace(response))
    }

    @Test
    fun placeWithoutLocationFails() {
        assertThrows(IOException::class.java) { parsePlace(JSONObject("{}")) }
    }
}

package com.example.lambda_bbb_events.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.services.PlaceDetails
import com.example.lambda_bbb_events.model.services.PlaceSuggestion
import com.example.lambda_bbb_events.model.services.parsePhotonPlaces
import com.example.lambda_bbb_events.model.services.parsePlace
import com.example.lambda_bbb_events.model.services.parseSuggestions
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Tests reading responses from the Places API and Photon. JSON is only available on a device, so
 * these are not unit tests. Nothing is sent to either service.
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

    private fun photonPlaces(vararg features: String) =
        parsePhotonPlaces(JSONObject("""{"features": [${features.joinToString()}]}"""))

    @Test
    fun photonPlacesHaveTheirDetails() {
        val place = photonPlaces(
            """{"properties": {"osm_type": "N", "osm_id": 7, "name": "Lake Lodge", "housenumber": "1",
                "street": "Lake Rd", "city": "Banff", "state": "Alberta", "postcode": "T1L 1A1",
                "country": "Canada"}, "geometry": {"coordinates": [-115.5, 51.2]}}"""
        ).single()

        assertEquals("N7", place.placeId)
        assertEquals("Lake Lodge", place.name)
        assertEquals("1 Lake Rd, Banff, Alberta, Canada", place.area)
        assertEquals(
            PlaceDetails(51.2, -115.5, "1 Lake Rd, Banff, Alberta T1L 1A1, Canada", "T1L 1A1"),
            place.details
        )
    }

    @Test
    fun photonPlacesWithoutAStreetUseTheirName() {
        val place = photonPlaces(
            """{"properties": {"name": "Banff", "city": "Banff", "state": "Alberta",
                "country": "Canada"}, "geometry": {"coordinates": [-115.5, 51.2]}}"""
        ).single()

        assertEquals("Alberta, Canada", place.area)
        assertEquals("Banff, Alberta, Canada", place.details?.address)
        assertEquals("", place.details?.postalCode)
    }

    @Test
    fun photonHousesAreNamedByTheirStreet() {
        val place = photonPlaces(
            """{"properties": {"housenumber": "9", "street": "Pine St", "city": "Jasper"},
                "geometry": {"coordinates": [-118.1, 52.9]}}"""
        ).single()

        assertEquals("9 Pine St", place.name)
        assertEquals("Jasper", place.area)
    }

    @Test
    fun repeatedAndIncompletePhotonPlacesAreSkipped() {
        val park = """{"properties": {"name": "Park", "country": "Canada"}, "geometry": {"coordinates": [-118.1, 52.9]}}"""
        val places = photonPlaces(
            park,
            park,
            """{"properties": {"name": "No location"}}""",
            """{"properties": {"country": "No name"}, "geometry": {"coordinates": [1, 2]}}"""
        )

        assertEquals(listOf("Park"), places.map { it.name })
        assertEquals(emptyList<PlaceSuggestion>(), parsePhotonPlaces(JSONObject("{}")))
    }

    @Test
    fun atMostFivePhotonPlacesAreRecommended() {
        val features = (1..8).map { """{"properties": {"name": "Park $it"}, "geometry": {"coordinates": [1, 2]}}""" }

        assertEquals(5, photonPlaces(*features.toTypedArray()).size)
    }

    @Test
    fun placeWithoutLocationFails() {
        assertThrows(IOException::class.java) { parsePlace(JSONObject("{}")) }
    }
}

package com.example.lambda_bbb_events.model.services

import com.example.lambda_bbb_events.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/*
 * Searches Google for places by name, using the Places API (New).
 *
 * Reference:
 * https://developers.google.com/maps/documentation/places/web-service/place-autocomplete
 */

private const val PLACES_URL = "https://places.googleapis.com/v1/places"
private const val TIMEOUT_MS = 10_000

/** How far around the map to prefer results from, in metres. 50 km is the most allowed. */
private const val NEARBY_RADIUS_METRES = 50_000.0

/** The status Google responds with when the key's usage limit is reached. */
private const val TOO_MANY_REQUESTS = 429

/**
 * Thrown when the Maps key has reached its usage limit. Demo keys work again the next day.
 */
class PlaceSearchLimitException : IOException("The place search limit was reached")

/**
 * A place recommended for what the user has typed.
 *
 * @property placeId Google's ID for the place, for [PlaceSearch.getPlace].
 * @property name The place's name, such as "Banff National Park".
 * @property area Where the place is, such as "AB, Canada". May be empty.
 */
data class PlaceSuggestion(
    val placeId: String,
    val name: String,
    val area: String
)

/**
 * A place's location and address.
 *
 * @property address The full address. May be empty.
 * @property postalCode The postal code, or empty if the place does not have one.
 */
data class PlaceDetails(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val postalCode: String
)

/**
 * Finds places by name, like the search bar in Google Maps.
 *
 * @param apiKey The Maps API key, which must allow the Places API (New).
 */
class PlaceSearch(
    private val apiKey: String = BuildConfig.MAPS_API_KEY
) {
    /**
     * Gets recommended places for what the user has typed so far. Places near the given
     * location are preferred, but places anywhere can be recommended.
     *
     * @param query What the user has typed.
     * @param nearLatitude Latitude to prefer places near.
     * @param nearLongitude Longitude to prefer places near.
     * @param sessionToken Identifies one search. Use the same token while the user types and
     * for the [getPlace] call after it, then a new one for the next search.
     * @return Up to five places, best match first.
     * @throws PlaceSearchLimitException if the key's usage limit is reached.
     * @throws IOException if the search fails for another reason, such as being offline.
     */
    suspend fun suggest(
        query: String,
        nearLatitude: Double,
        nearLongitude: Double,
        sessionToken: String
    ): List<PlaceSuggestion> {
        val center = JSONObject()
            .put("latitude", nearLatitude)
            .put("longitude", nearLongitude)
        val circle = JSONObject()
            .put("center", center)
            .put("radius", NEARBY_RADIUS_METRES)
        val body = JSONObject()
            .put("input", query)
            .put("sessionToken", sessionToken)
            .put("locationBias", JSONObject().put("circle", circle))

        val response = request(
            url = "$PLACES_URL:autocomplete",
            fields = "suggestions.placePrediction.placeId," +
                    "suggestions.placePrediction.structuredFormat",
            body = body
        )
        return parseSuggestions(response)
    }

    /**
     * Gets the location and address of a recommended place.
     *
     * @param placeId The ID of the place, from [PlaceSuggestion.placeId].
     * @param sessionToken The token used for the search that recommended the place.
     * @throws PlaceSearchLimitException if the key's usage limit is reached.
     * @throws IOException if the request fails for another reason, or the place has no location.
     */
    suspend fun getPlace(placeId: String, sessionToken: String): PlaceDetails {
        val response = request(
            url = "$PLACES_URL/${encode(placeId)}?sessionToken=${encode(sessionToken)}",
            fields = "location,formattedAddress,addressComponents"
        )
        return parsePlace(response)
    }

    private fun encode(text: String): String = URLEncoder.encode(text, "UTF-8")

    /**
     * Sends a request to the Places API off the main thread.
     *
     * @param fields The parts of the response that are needed, which Google requires.
     * @param body Sent as a POST request, or `null` to send a GET request.
     */
    private suspend fun request(
        url: String,
        fields: String,
        body: JSONObject? = null
    ): JSONObject = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            // Sent as headers, which keeps the key out of the URL
            connection.setRequestProperty("X-Goog-Api-Key", apiKey)
            connection.setRequestProperty("X-Goog-FieldMask", fields)

            if (body != null) {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toString().toByteArray()) }
            }

            val status = connection.responseCode
            if (status == TOO_MANY_REQUESTS) {
                throw PlaceSearchLimitException()
            }
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("Place search failed with status $status")
            }
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Reads the places in an autocomplete response. Entries without an ID or name are skipped.
 */
internal fun parseSuggestions(response: JSONObject): List<PlaceSuggestion> {
    val places = mutableListOf<PlaceSuggestion>()
    // Google leaves the list out when nothing matches
    val suggestions = response.optJSONArray("suggestions") ?: return places

    for (i in 0 until suggestions.length()) {
        val prediction = suggestions.optJSONObject(i)?.optJSONObject("placePrediction") ?: continue
        val format = prediction.optJSONObject("structuredFormat")
        val placeId = prediction.optString("placeId")
        val name = format?.optJSONObject("mainText")?.optString("text").orEmpty()
        val area = format?.optJSONObject("secondaryText")?.optString("text").orEmpty()

        if (placeId.isNotBlank() && name.isNotBlank()) {
            places.add(PlaceSuggestion(placeId, name, area))
        }
    }
    return places
}

/**
 * Reads the place in a place details response.
 *
 * @throws IOException if the place has no location.
 */
internal fun parsePlace(response: JSONObject): PlaceDetails {
    val location = response.optJSONObject("location")
    // Google leaves out a coordinate that is exactly 0
    val latitude = location?.optDouble("latitude", 0.0)
    val longitude = location?.optDouble("longitude", 0.0)
    if (latitude == null || longitude == null) {
        throw IOException("The place has no location")
    }

    var postalCode = ""
    val components = response.optJSONArray("addressComponents")
    for (i in 0 until (components?.length() ?: 0)) {
        val component = components?.optJSONObject(i) ?: continue
        val types = component.optJSONArray("types") ?: continue
        for (j in 0 until types.length()) {
            if (types.optString(j) == "postal_code") {
                postalCode = component.optString("longText")
            }
        }
    }

    return PlaceDetails(latitude, longitude, response.optString("formattedAddress"), postalCode)
}

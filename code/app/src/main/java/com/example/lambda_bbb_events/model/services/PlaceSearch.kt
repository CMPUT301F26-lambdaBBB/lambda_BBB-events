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
 * Searches for places by name. [GooglePlaceSearch] uses Google's Places API (New).
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
 * Thrown when the place search has been used too much. Google demo keys work again the next day.
 */
class PlaceSearchLimitException : IOException("The place search limit was reached")

/**
 * Thrown when Google does not accept the Maps key, such as when it is wrong or not allowed to
 * search for places.
 */
class PlaceSearchKeyException : IOException("The Maps key was not accepted")

/**
 * Thrown when a place cannot be looked up because its search service was switched since it was
 * recommended. Searching again recommends places from the new service.
 */
class PlaceSearchSwitchedException : IOException("The place search service was switched")

/**
 * A place recommended for what the user has typed.
 *
 * @property placeId The search service's ID for the place.
 * @property name The place's name, such as "Banff National Park".
 * @property area Where the place is, such as "AB, Canada". May be empty.
 * @property details The place's location, if it is already known.
 */
data class PlaceSuggestion(
    val placeId: String,
    val name: String,
    val area: String,
    val details: PlaceDetails? = null
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
 */
interface PlaceSearch {
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
     * @throws PlaceSearchLimitException if the usage limit is reached.
     * @throws IOException if the search fails for another reason, such as being offline.
     */
    @Throws(IOException::class)
    suspend fun suggest(
        query: String,
        nearLatitude: Double,
        nearLongitude: Double,
        sessionToken: String
    ): List<PlaceSuggestion>

    /**
     * Gets the location and address of a recommended place.
     *
     * @param suggestion The place.
     * @param sessionToken The token used for the search that recommended the place.
     * @throws PlaceSearchSwitchedException if the place is from a service no longer used.
     * @throws PlaceSearchLimitException if the usage limit is reached.
     * @throws IOException if the request fails for another reason, or the place has no location.
     */
    @Throws(IOException::class)
    suspend fun getPlace(suggestion: PlaceSuggestion, sessionToken: String): PlaceDetails
}

/**
 * Finds places by name with Google.
 *
 * @param apiKey The Maps API key, which must allow the Places API (New).
 */
class GooglePlaceSearch(
    private val apiKey: String = BuildConfig.MAPS_API_KEY
) : PlaceSearch {

    override suspend fun suggest(
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

    override suspend fun getPlace(suggestion: PlaceSuggestion, sessionToken: String): PlaceDetails {
        val response = request(
            url = "$PLACES_URL/${encode(suggestion.placeId)}?sessionToken=${encode(sessionToken)}",
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
            if (status != HttpURLConnection.HTTP_OK) {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw googleFailure(status, error.orEmpty())
            }
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Finds why Google refused a request.
 *
 * @param status The response's HTTP status.
 * @param error The response's text. Wrong keys have the same status as other bad requests, so
 * they are recognized by the reason Google gives, such as "API_KEY_INVALID".
 */
internal fun googleFailure(status: Int, error: String): IOException = when {
    status == TOO_MANY_REQUESTS -> PlaceSearchLimitException()
    status == HttpURLConnection.HTTP_UNAUTHORIZED ||
            status == HttpURLConnection.HTTP_FORBIDDEN ||
            status == HttpURLConnection.HTTP_BAD_REQUEST && "API_KEY" in error -> PlaceSearchKeyException()
    else -> IOException("Place search failed with status $status")
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

package com.example.lambda_bbb_events.model.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/*
 * Searches OpenStreetMap for places by name, using Photon.
 *
 * Photon is used because OpenStreetMap's own search, Nominatim, does not allow searching while
 * the user types.
 *
 * Reference:
 * https://photon.komoot.io/
 */

private const val PHOTON_URL = "https://photon.komoot.io/api/"
private const val TIMEOUT_MS = 10_000
private const val MAX_SUGGESTIONS = 5

/** More than are shown, since some are the same place mapped twice. */
private const val MAX_RESULTS = 10

/** The statuses Photon responds with when it is used too much. */
private val LIMIT_STATUSES = setOf(429, 403)

/**
 * Finds places by name in OpenStreetMap. Does not need an API key.
 */
class PhotonPlaceSearch : PlaceSearch {

    override suspend fun suggest(
        query: String,
        nearLatitude: Double,
        nearLongitude: Double,
        sessionToken: String
    ): List<PlaceSuggestion> = withContext(Dispatchers.IO) {
        val url = PHOTON_URL +
                "?q=${URLEncoder.encode(query, "UTF-8")}" +
                "&lat=$nearLatitude&lon=$nearLongitude&limit=$MAX_RESULTS&lang=en"
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("User-Agent", MAP_USER_AGENT)

            val status = connection.responseCode
            if (status in LIMIT_STATUSES) {
                throw PlaceSearchLimitException()
            }
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("Place search failed with status $status")
            }
            parsePhotonPlaces(JSONObject(connection.inputStream.bufferedReader().use { it.readText() }))
        } finally {
            connection.disconnect()
        }
    }

    // Photon's suggestions already have their details, so this does not need a request
    override suspend fun getPlace(suggestion: PlaceSuggestion, sessionToken: String): PlaceDetails =
        suggestion.details ?: throw IOException("The place has no location")
}

/**
 * Reads the places in a Photon response. Places without a name or location are skipped, and so
 * are places that would look the same as an earlier one.
 */
internal fun parsePhotonPlaces(response: JSONObject): List<PlaceSuggestion> {
    val places = mutableListOf<PlaceSuggestion>()
    val features = response.optJSONArray("features") ?: return places

    for (i in 0 until features.length()) {
        val feature = features.optJSONObject(i) ?: continue
        val properties = feature.optJSONObject("properties") ?: continue
        // Longitude comes first
        val coordinates = feature.optJSONObject("geometry")?.optJSONArray("coordinates") ?: continue
        val longitude = coordinates.optDouble(0)
        val latitude = coordinates.optDouble(1)

        fun text(name: String) = properties.optString(name)
        val street = joinNotBlank(" ", text("housenumber"), text("street"))
        val name = text("name").ifBlank { street } // houses usually have no name
        if (name.isBlank() || latitude.isNaN() || longitude.isNaN()) {
            continue
        }

        val town = text("city").ifBlank { text("county") }
        val region = text("state")
        val postalCode = text("postcode")
        val country = text("country")
        places.add(
            PlaceSuggestion(
                placeId = text("osm_type") + text("osm_id"),
                name = name,
                area = joinNotBlank(", ", street, town, region, country, except = name),
                details = PlaceDetails(
                    latitude = latitude,
                    longitude = longitude,
                    address = joinNotBlank(
                        ", ",
                        street.ifBlank { name },
                        town,
                        joinNotBlank(" ", region, postalCode),
                        country
                    ),
                    postalCode = postalCode
                )
            )
        )
    }
    return places.distinctBy { it.name to it.area }.take(MAX_SUGGESTIONS)
}

/** Joins the parts that are not blank, leaving out repeated ones and [except]. */
private fun joinNotBlank(separator: String, vararg parts: String, except: String? = null): String =
    parts.filter { it.isNotBlank() && it != except }.distinct().joinToString(separator)

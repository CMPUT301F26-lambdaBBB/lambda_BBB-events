package com.example.lambda_bbb_events.model.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lambda_bbb_events.BuildConfig

/*
 * Chooses the service used for maps and for searching for places.
 */

/** A service that provides maps and place search. */
enum class MapProvider {
    /** Google Maps. Needs an API key, and stops working when the key's daily limit is reached. */
    GOOGLE,

    /**
     * OpenStreetMap, with Photon for searching. Needs no key, but both are shared public
     * servers, so they must not be used heavily.
     *
     * The tile policy is at https://operations.osmfoundation.org/policies/tiles/ and Photon's
     * fair use terms are at https://photon.komoot.io/.
     */
    OPEN_STREET_MAP
}

/**
 * The services the app uses. Maps are Google's whenever there is an API key, since loading them
 * is free and unlimited. Search is Google's until the key fails or reaches its limit, then
 * Photon's until the app is restarted.
 *
 * @param apiKey The Maps API key. If it is blank, Google is never tried.
 */
class MapFallback(apiKey: String = BuildConfig.MAPS_API_KEY) {

    /** The service that draws maps. */
    val mapProvider: MapProvider =
        if (apiKey.isBlank()) MapProvider.OPEN_STREET_MAP else MapProvider.GOOGLE

    /** The service to search with now. Kept as Compose state so open search bars switch over. */
    var provider by mutableStateOf(mapProvider)
        private set

    /** Switches search to Photon, because Google did not accept the API key or its limit was reached. */
    fun reportGoogleFailure() {
        provider = MapProvider.OPEN_STREET_MAP
    }

    companion object {
        /** Shared by every map and search in the app. */
        val shared = MapFallback()
    }
}

/**
 * Tells OpenStreetMap and Photon which app is using them, which OpenStreetMap requires. Give it
 * a name that only this app uses before releasing it.
 */
const val MAP_USER_AGENT = "CampsiteApp/1.0 (Android)"

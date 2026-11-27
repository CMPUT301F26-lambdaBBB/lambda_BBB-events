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
 * The service the app uses: Google until its API key fails or reaches its limit, then
 * OpenStreetMap until the app is restarted. The map and the search always use the same one.
 *
 * Only the search can notice a failure: the native Maps SDK draws a blank map instead of telling
 * the app that it refused the key. A failed search switches the map over too.
 *
 * @param apiKey The Maps API key. If it is blank, Google is never tried.
 */
class MapFallback(apiKey: String = BuildConfig.MAPS_API_KEY) {

    /** The service to use now. Kept as Compose state so open maps switch over. */
    var provider by mutableStateOf(
        if (apiKey.isBlank()) MapProvider.OPEN_STREET_MAP else MapProvider.GOOGLE
    )
        private set

    /** Switches to OpenStreetMap, because Google did not accept the API key or its limit was reached. */
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

package com.example.lambda_bbb_events.model.services

/*
 * Searches for places with Google, or with OpenStreetMap if Google's API key does not work.
 */

/**
 * Finds places with Google until its API key fails or reaches its limit, then with Photon.
 *
 * Other failures, such as being offline, are not the key's fault and do not switch.
 *
 * @param fallback Decides which service to use, and is told when Google fails.
 */
class FallbackPlaceSearch(
    private val fallback: MapFallback = MapFallback.shared,
    private val google: PlaceSearch = GooglePlaceSearch(),
    private val photon: PlaceSearch = PhotonPlaceSearch()
) : PlaceSearch {

    override suspend fun suggest(
        query: String,
        nearLatitude: Double,
        nearLongitude: Double,
        sessionToken: String
    ): List<PlaceSuggestion> {
        if (fallback.provider == MapProvider.GOOGLE) {
            try {
                return google.suggest(query, nearLatitude, nearLongitude, sessionToken)
            } catch (_: PlaceSearchLimitException) {
                fallback.reportGoogleFailure()
            } catch (_: PlaceSearchKeyException) {
                fallback.reportGoogleFailure()
            }
        }
        return photon.suggest(query, nearLatitude, nearLongitude, sessionToken)
    }

    override suspend fun getPlace(suggestion: PlaceSuggestion, sessionToken: String): PlaceDetails {
        // Only Photon's suggestions come with their details
        if (suggestion.details != null) {
            return photon.getPlace(suggestion, sessionToken)
        }

        if (fallback.provider == MapProvider.GOOGLE) {
            try {
                return google.getPlace(suggestion, sessionToken)
            } catch (_: PlaceSearchLimitException) {
                fallback.reportGoogleFailure()
            } catch (_: PlaceSearchKeyException) {
                fallback.reportGoogleFailure()
            }
        }
        // Photon does not know Google's places, and guessing by name could find the wrong one
        throw PlaceSearchSwitchedException()
    }
}

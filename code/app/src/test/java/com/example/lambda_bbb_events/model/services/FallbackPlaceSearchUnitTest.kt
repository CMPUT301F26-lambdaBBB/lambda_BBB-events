package com.example.lambda_bbb_events.model.services

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Tests switching from Google to OpenStreetMap when Google's key does not work.
 */
class FallbackPlaceSearchUnitTest {
    private val fallback = MapFallback(apiKey = "key")
    private val google = mockk<PlaceSearch>()
    private val photon = mockk<PlaceSearch>()
    private val search = FallbackPlaceSearch(fallback, google, photon)

    private val details = PlaceDetails(52.9, -118.1, "Jasper, AB", "")
    private val googlePlace = PlaceSuggestion("google", "Jasper", "AB")
    private val photonPlace = PlaceSuggestion("photon", "Jasper", "Alberta", details)

    init {
        coEvery { google.suggest(any(), any(), any(), any()) } returns listOf(googlePlace)
        coEvery { google.getPlace(any(), any()) } returns details
        coEvery { photon.suggest(any(), any(), any(), any()) } returns listOf(photonPlace)
        coEvery { photon.getPlace(photonPlace, any()) } returns details
    }

    private suspend fun suggest() = search.suggest("Jasper", 51.0, -115.0, "token")

    private fun failGoogle(error: IOException) {
        coEvery { google.suggest(any(), any(), any(), any()) } throws error
        coEvery { google.getPlace(any(), any()) } throws error
    }

    @Test
    fun googleIsUsedFirst() = runTest {
        assertEquals(listOf(googlePlace), suggest())
        assertEquals(details, search.getPlace(googlePlace, "token"))

        assertEquals(MapProvider.GOOGLE, fallback.provider)
        coVerify(exactly = 0) { photon.suggest(any(), any(), any(), any()) }
    }

    @Test
    fun reachingTheLimitSwitchesTheSameSearch() = runTest {
        failGoogle(PlaceSearchLimitException())

        assertEquals(listOf(photonPlace), suggest())
        assertEquals(MapProvider.OPEN_STREET_MAP, fallback.provider)
    }

    @Test
    fun rejectedKeySwitchesTheSameSearch() = runTest {
        failGoogle(PlaceSearchKeyException())

        assertEquals(listOf(photonPlace), suggest())
        assertEquals(MapProvider.OPEN_STREET_MAP, fallback.provider)
    }

    @Test
    fun googleIsNotTriedAgainAfterSwitching() = runTest {
        failGoogle(PlaceSearchLimitException())
        suggest()
        suggest()

        coVerify(exactly = 1) { google.suggest(any(), any(), any(), any()) }
        coVerify(exactly = 2) { photon.suggest(any(), any(), any(), any()) }
    }

    @Test
    fun failedMapSwitchesTheSearch() = runTest {
        fallback.reportGoogleFailure()

        assertEquals(listOf(photonPlace), suggest())
        coVerify(exactly = 0) { google.suggest(any(), any(), any(), any()) }
    }

    @Test
    fun beingOfflineDoesNotSwitch() = runTest {
        failGoogle(IOException("offline"))

        assertTrue(runCatching { suggest() }.exceptionOrNull() is IOException)
        assertEquals(MapProvider.GOOGLE, fallback.provider)
        coVerify(exactly = 0) { photon.suggest(any(), any(), any(), any()) }
    }

    @Test
    fun failingToLookUpAGooglePlaceSwitchesWithoutGuessing() = runTest {
        failGoogle(PlaceSearchLimitException())

        val error = runCatching { search.getPlace(googlePlace, "token") }.exceptionOrNull()

        assertTrue(error is PlaceSearchSwitchedException)
        assertEquals(MapProvider.OPEN_STREET_MAP, fallback.provider)
    }

    @Test
    fun googlePlacesAreNotLookedUpAfterSwitching() = runTest {
        fallback.reportGoogleFailure()

        val error = runCatching { search.getPlace(googlePlace, "token") }.exceptionOrNull()

        assertTrue(error is PlaceSearchSwitchedException)
        coVerify(exactly = 0) { google.getPlace(any(), any()) }
    }

    @Test
    fun photonPlacesAreLookedUpWithPhoton() = runTest {
        fallback.reportGoogleFailure()

        assertEquals(details, search.getPlace(photonPlace, "token"))
    }

    @Test
    fun searchFailureKeepsTheGoogleMap() {
        fallback.reportGoogleFailure()

        assertEquals(MapProvider.GOOGLE, fallback.mapProvider)
    }

    @Test
    fun missingKeyUsesTheOpenStreetMap() {
        assertEquals(MapProvider.OPEN_STREET_MAP, MapFallback(apiKey = "").mapProvider)
    }

    @Test
    fun missingKeyNeverTriesGoogle() {
        assertEquals(MapProvider.OPEN_STREET_MAP, MapFallback(apiKey = "").provider)
    }

    @Test
    fun googleFailuresAreRecognized() {
        assertTrue(googleFailure(429, "") is PlaceSearchLimitException)
        assertTrue(googleFailure(401, "") is PlaceSearchKeyException)
        assertTrue(googleFailure(403, "") is PlaceSearchKeyException)
        assertTrue(googleFailure(400, """{"reason": "API_KEY_INVALID"}""") is PlaceSearchKeyException)

        for (other in listOf(googleFailure(400, "bad place ID"), googleFailure(500, ""))) {
            assertFalse(other is PlaceSearchKeyException || other is PlaceSearchLimitException)
        }
    }
}

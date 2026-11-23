package com.example.lambda_bbb_events.controller

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.lambda_bbb_events.model.AvailabilitySchedule
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.ImageMetaData
import com.example.lambda_bbb_events.store.CampsiteStore
import java.time.LocalDate

class CampsiteController(
    private val campsiteStore: CampsiteStore
) {

    private var _searchQuery = ""

    val searchQuery: String
        get() = _searchQuery

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private var location: String = ""
    private var starRating: Double? = null
    private var guestRange: IntRange? = null

    val campsites: Map<String, Campsite>
        get() = campsiteStore.campsites

    val filteredCampsites: Map<String, Campsite>
        get() = campsites.filterValues { meetsAllFilters(it) }

    fun addCampsite(
        campsiteId: String = "",
        ownerId: String = "",
        campsiteName: String = "",
        description: String = "",
        address: String = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        maxOccupancy: Int = 1,
        rulesPolicies: String = "",
        amenities: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        imageIds: List<ImageMetaData> = emptyList(),
        rating: Float = 0f,
        ratingCount: Int = 0,
        availability: AvailabilitySchedule = AvailabilitySchedule(),
        active: Boolean = true,
        returnMessage: MutableState<String> = mutableStateOf("")
    ): Boolean {

        return campsiteStore.addCampsite(
            campsiteId = campsiteId,
            ownerId = ownerId,
            campsiteName = campsiteName,
            description = description,
            address = address,
            latitude = latitude,
            longitude = longitude,
            maxOccupancy = maxOccupancy,
            rulesPolicies = rulesPolicies,
            amenities = amenities,
            tags = tags,
            imageIds = imageIds,
            rating = rating,
            ratingCount = ratingCount,
            availability = availability,
            active = active,
            returnMessage = returnMessage
        )
    }

    fun removeCampsite(
        campsite: Campsite,
        returnMessage: MutableState<String>
    ): Boolean {

        return campsiteStore.removeCampsite(
            campsite,
            returnMessage
        )
    }

    fun updateCampsite(
        campsiteId: String,
        campsite: Campsite,
        returnMessage: MutableState<String> = mutableStateOf("")
    ): Boolean {

        return campsiteStore.updateCampsite(
            campsiteId,
            campsite,
            returnMessage
        )
    }

    fun getCampsite(
        campsiteId: String
    ): Campsite? {

        return campsiteStore.campsites[campsiteId]
    }

    fun getCampsitesByOwner(
        ownerId: String
    ): List<Campsite> {

        return campsiteStore.campsites.values.filter {
            it.ownerId == ownerId
        }
    }

    fun applyFilters(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        location: String = "",
        starRating: Double? = null,
        guestRange: IntRange? = null
    ): Map<String, Campsite> {

        this.startDate = startDate
        this.endDate = endDate
        this.location = location.trim()
        this.starRating = starRating
        this.guestRange = guestRange
        return filteredCampsites
    }

    fun clearFilters() {
        applyFilters()
    }

    fun search(
        query: String
    ) {
        _searchQuery = query.trim()
    }

    fun getSearchResults(): List<Campsite> {

        return filteredCampsites.values.filter { matchesSearch(it) }
    }

    private fun meetsAllFilters(
        campsite: Campsite
    ): Boolean {

        return isAvailable(campsite) &&
                meetsStarRating(campsite) &&
                meetsGuestRange(campsite) &&
                meetsLocation(campsite)
    }

    private fun isAvailable(
        campsite: Campsite
    ): Boolean {

        val start = startDate ?: return true
        val end = endDate ?: start
        return campsite.availability.isAvailable(start, end)
    }

    private fun meetsStarRating(
        campsite: Campsite
    ): Boolean {

        val rating = starRating ?: return true
        return campsite.rating >= rating
    }

    private fun meetsGuestRange(
        campsite: Campsite
    ): Boolean {

        val range = guestRange ?: return true
        return campsite.maxOccupancy in range
    }

    private fun meetsLocation(
        campsite: Campsite
    ): Boolean {

        return location.isBlank() ||
                campsite.address.contains(location, ignoreCase = true) ||
                campsite.campsiteName.contains(location, ignoreCase = true)
    }

    private fun matchesSearch(
        campsite: Campsite
    ): Boolean {

        return searchQuery.isBlank() ||
                campsite.campsiteName.contains(searchQuery, ignoreCase = true) ||
                campsite.address.contains(searchQuery, ignoreCase = true) ||
                campsite.tags.any { it.contains(searchQuery, ignoreCase = true) }
    }
}

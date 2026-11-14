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

    var searchQuery = ""

    val campsites: Map<String, Campsite>
        get() = campsiteStore.campsites

    val filteredCampsites: Map<String, Campsite>
        get() = campsiteStore.filteredCampsites

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

        return campsiteStore.filterUpdate(
            startDate = startDate,
            endDate = endDate,
            location = location,
            starRating = starRating,
            guestRange = guestRange
        )
    }


    fun clearFilters() {
        campsiteStore.filterUpdate()
    }

    fun search(
        query: String
    ) {
        searchQuery = query.trim()
    }

    fun getSearchResults(): List<Campsite> {

        val source =
            if (searchQuery.isBlank()) {
                filteredCampsites.values.toList()
            } else {
                filteredCampsites.values.filter { campsite ->

                    campsite.campsiteName.contains(
                        searchQuery,
                        ignoreCase = true
                    ) ||

                            campsite.address.contains(
                                searchQuery,
                                ignoreCase = true
                            ) ||

                            campsite.tags.any {
                                it.contains(
                                    searchQuery,
                                    ignoreCase = true
                                )
                            }
                }
            }
        return source
    }
}
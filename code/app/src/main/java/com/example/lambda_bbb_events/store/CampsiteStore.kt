package com.example.lambda_bbb_events.store

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import com.example.lambda_bbb_events.model.AvailabilitySchedule
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.ImageMetaData
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.model.services.generateQR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class CampsiteStore {
    val campsiteRepo = CampsiteRepository()
    var campsites: MutableMap<String, Campsite> = mutableStateMapOf()

    var filteredCampsites: MutableMap<String, Campsite> = mutableStateMapOf()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            campsites = campsiteRepo.getAllCampsites()
            filteredCampsites = campsites
        }
    }

    fun addCampsite(
        campsiteId: String = "",
        ownerId: String = "", // CampsiteOwner deviceId
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
        rating: Float = 0f, // average
        ratingCount: Int = 0,
        availability: AvailabilitySchedule = AvailabilitySchedule(),
        active: Boolean = true,
        returnMessage: MutableState<String>): Boolean {
        try {
            val campsite = Campsite(campsiteId = campsiteId,
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
                active = active
            )

            campsites[campsite.campsiteId] = campsite
            scope.launch {
                campsiteRepo.addCampsite(campsite)
            }
        }
        catch (e: Exception) {
            returnMessage.value = e.message.toString()
            return false
        }
        return true
    }

    fun removeCampsite(campsite: Campsite, returnMessage: MutableState<String>): Boolean {
        try {
            scope.launch {
                campsiteRepo.deleteCampsite(campsite.campsiteId)
            }
            val ans = campsites.remove(campsite.campsiteId) != null
            returnMessage.value = "Success"
            return ans
        }
        catch (e: Exception) {
            returnMessage.value = e.message.toString()
            return false
        }
    }

    fun updateCampsite(campsiteID: String, campsite: Campsite, returnMessage: MutableState<String>): Boolean {
        try {
            campsites[campsiteID] = campsite
            returnMessage.value = "Success"
        }
        catch (e: Exception) {
            returnMessage.value = e.message.toString()
            return false
        }
        return true
    }

    fun getByOwner(ownerId: String): Campsite {
        return campsites[ownerId] ?: throw IllegalArgumentException("Campsite not found")
    }

    fun filterUpdate(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        location: String? = null,
        starRating: Double? = null,
        guestRange: IntRange? = null,
    ): MutableMap<String, Campsite> {
        val givenStartDate = startDate ?: campsites.values.mapNotNull { it.availability.minDate }.minOrNull()

        val givenEndDate = endDate ?: campsites.values.mapNotNull { it.availability.maxDate }.maxOrNull()

        filteredCampsites = campsites.filter { (_, campsite) ->
            val isAvailable = givenStartDate?.let { start ->
                givenEndDate?.let { end ->
                   campsite.availability.isAvailable(start, end)
                } ?: true
            } ?: true

            val meetsStarRating = starRating?.let { rating ->
                campsite.rating >= rating
            } ?: true

            val meetsGuestRange = guestRange?.let { range ->
                campsite.maxOccupancy in range
            } ?: true

            val meetsLocation = location?.let { query ->
                query.isBlank() ||
                        campsite.address.contains(query, ignoreCase = true) ||
                        campsite.campsiteName.contains(query, ignoreCase = true)
            } ?: true

            isAvailable && meetsStarRating && meetsGuestRange && meetsLocation
        }.toMutableMap()
        return filteredCampsites
    }
}
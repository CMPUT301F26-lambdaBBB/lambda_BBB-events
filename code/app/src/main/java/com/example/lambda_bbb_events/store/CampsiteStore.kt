package com.example.lambda_bbb_events.store

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import com.example.lambda_bbb_events.model.campsite.AvailabilitySchedule
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CampsiteStore(
    val campsiteRepo: CampsiteRepository = CampsiteRepository(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    val campsites: MutableMap<String, Campsite> = mutableStateMapOf()

    init {
        scope.launch {
            campsites.putAll(campsiteRepo.getAllCampsites())
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
                active = active,
                createdAt = System.currentTimeMillis()
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
            scope.launch {
                campsiteRepo.updateCampsite(campsiteID, campsite)
            }
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
}
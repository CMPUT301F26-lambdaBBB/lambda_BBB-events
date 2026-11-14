package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.AvailabilitySchedule
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.ImageMetaData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Represents a Firestore campsite listing.
 *
 * @property campsiteId The unique ID of the campsite.
 * @property ownerId The device ID of the campsite owner.
 * @property campsiteName The name of the campsite.
 * @property description A description of the campsite.
 * @property address The campsite's address.
 * @property latitude The campsite's latitude.
 * @property longitude The campsite's longitude.
 * @property maxOccupancy The maximum number of guests allowed.
 * @property rulesPolicies Rules and policies for the campsite.
 * @property amenities Amenities available at the campsite.
 * @property tags Tags for the campsite.
 * @property imageIds IDs of images associated with the campsite.
 * @property rating The mean rating of the campsite.
 * @property ratingCount The number of ratings of the campsite.
 * @property availability The campsite's availability schedule.
 * @property active Whether the campsite listing is active.
 */
data class FirestoreCampsite(
    val campsiteId: String = "",
    val ownerId: String = "", // CampsiteOwner deviceId
    val campsiteName: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val maxOccupancy: Int = 1,
    val rulesPolicies: String = "",
    val amenities: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val imageIds: List<ImageMetaData> = emptyList(),
    val rating: Float = 0f, // average
    val ratingCount: Int = 0,
    val availability: FirestoreAvailabilitySchedule = FirestoreAvailabilitySchedule(),
    var active: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Campsite) return false

        return campsiteId == other.campsiteId &&
                ownerId == other.ownerId &&
                campsiteName == other.campsiteName
    }

    override fun hashCode(): Int {
        var result = campsiteId.hashCode()
        result = 31 * result + ownerId.hashCode()
        result = 31 * result + campsiteName.hashCode()
        return result
    }
}

data class FirestoreAvailabilitySchedule(
    var dates: MutableMap<String, Int> = mutableMapOf()
)

/**
 * Does all the data operations related to databases.
 */
class CampsiteRepository {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")

    // private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    /**
     * Gets all campsites from the database.
     */
    suspend fun addCampsite(campsite: Campsite) {
        val firestoreDates = campsite.availability.dates.mapKeys { it.key.toString() }.toMutableMap()

        val firestoreCampsite = FirestoreCampsite(campsiteId = campsite.campsiteId,
                ownerId = campsite.ownerId,
                campsiteName = campsite.campsiteName,
                description = campsite.description,
                address = campsite.address,
                latitude = campsite.latitude,
                longitude = campsite.longitude,
                maxOccupancy = campsite.maxOccupancy,
                rulesPolicies = campsite.rulesPolicies,
                amenities = campsite.amenities,
                tags = campsite.tags,
                imageIds = campsite.imageIds,
                rating = campsite.rating,
                ratingCount = campsite.ratingCount,
                availability = FirestoreAvailabilitySchedule(dates = firestoreDates),
                active = campsite.active
            )
        campsitesRef.add(firestoreCampsite).await()
    }

    suspend fun deleteCampsite(campsiteID: String) {
        campsitesRef.document(campsiteID).delete().await()
    }

    suspend fun updateCampsite(campsiteID: String, updatedCampsite: Campsite) {
        val firestoreDates = updatedCampsite.availability.dates.mapKeys { it.key.toString() }.toMutableMap()

        val firestoreCampsite = FirestoreCampsite(campsiteId = updatedCampsite.campsiteId,
                ownerId = updatedCampsite.ownerId,
                campsiteName = updatedCampsite.campsiteName,
                description = updatedCampsite.description,
                address = updatedCampsite.address,
                latitude = updatedCampsite.latitude,
                longitude = updatedCampsite.longitude,
                maxOccupancy = updatedCampsite.maxOccupancy,
                rulesPolicies = updatedCampsite.rulesPolicies,
                amenities = updatedCampsite.amenities,
                tags = updatedCampsite.tags,
                imageIds = updatedCampsite.imageIds,
                rating = updatedCampsite.rating,
                ratingCount = updatedCampsite.ratingCount,
                availability = FirestoreAvailabilitySchedule(dates = firestoreDates),
                active = updatedCampsite.active
            )
        campsitesRef.document(campsiteID).set(firestoreCampsite).await()
    }

    suspend fun getAllCampsites(): MutableMap<String, Campsite> {
        val campsites = mutableMapOf<String, Campsite>()
        val snapshot = campsitesRef.get().await()
        campsites.clear()
        for (document in snapshot.documents) {
            val firestoreCampsite = document.toObject(FirestoreCampsite::class.java)
            if (firestoreCampsite != null) {
                val campsite = Campsite(
                    campsiteId = firestoreCampsite.campsiteId,
                    ownerId = firestoreCampsite.ownerId,
                    campsiteName = firestoreCampsite.campsiteName,
                    description = firestoreCampsite.description,
                    address = firestoreCampsite.address,
                    latitude = firestoreCampsite.latitude,
                    longitude = firestoreCampsite.longitude,
                    maxOccupancy = firestoreCampsite.maxOccupancy,
                    rulesPolicies = firestoreCampsite.rulesPolicies,
                    amenities = firestoreCampsite.amenities,
                    tags = firestoreCampsite.tags,
                    imageIds = firestoreCampsite.imageIds,
                    rating = firestoreCampsite.rating,
                    ratingCount = firestoreCampsite.ratingCount,
                    availability = AvailabilitySchedule(dates = firestoreCampsite.availability.dates
                        .mapKeys { LocalDate.parse(it.key) }
                        .toMutableMap()
                    ),
                    active = firestoreCampsite.active
                )
                campsites[campsite.campsiteId] = campsite
            }
        }
        return campsites
    }
}
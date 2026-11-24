package com.example.lambda_bbb_events.model.repository

import android.util.Log
import com.example.lambda_bbb_events.model.campsite.AvailabilitySchedule
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Represents a Firestore campsite listing.
 *
 * Exists to allow for defined conversion between the normal class used by our app and still be able
 * to upload information defined in ways Firebase doesn't support normally.
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
 * @property createdAt When the campsite was created, in milliseconds since 1970.
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
    var active: Boolean = true,
    val createdAt: Long = 0L
)

data class FirestoreAvailabilitySchedule(
    var dates: MutableMap<String, Int> = mutableMapOf()
)

/**
 * Converts a [Campsite] into its Firestore representation. Availability dates are stored
 * as ISO-8601 strings since Firestore cannot store [LocalDate] keys.
 */
fun Campsite.toFirestore() = FirestoreCampsite(
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
    availability = FirestoreAvailabilitySchedule(
        dates = availability.dates.mapKeys { it.key.toString() }.toMutableMap()
    ),
    active = active,
    createdAt = createdAt
)

/**
 * Converts a [FirestoreCampsite] back into a [Campsite].
 *
 * @param id The ID of the campsite, defaults to the stored campsite ID.
 * @throws java.time.format.DateTimeParseException if an availability date is malformed.
 */
fun FirestoreCampsite.toCampsite(id: String = campsiteId) = Campsite(
    campsiteId = id,
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
    availability = AvailabilitySchedule(
        dates = availability.dates.mapKeys { LocalDate.parse(it.key) }.toMutableMap()
    ),
    active = active,
    createdAt = createdAt
)

/**
 * Handles the data in the Firestore database for the [Campsite] class.
 */
class CampsiteRepository(
    db: FirebaseFirestore = Firebase.firestore
) : Repository<Campsite>("campsites", db) {

    override fun idOf(item: Campsite) = item.campsiteId

    override fun fromDocument(document: DocumentSnapshot): Campsite? =
        document.toObject(FirestoreCampsite::class.java)?.toCampsite(document.id)

    override fun toDocument(item: Campsite): Any = item.toFirestore()

    override fun onListenError(error: FirebaseFirestoreException) {
        Log.e("CampsiteRepository", "listener error", error)
    }

    /**
     * Adds a new campsite to the Firestore database.
     *
     * @param campsite The campsite to add.
     */
    suspend fun addCampsite(campsite: Campsite) = add(campsite)

    /**
     * Deletes a specific campsite defined by its campsite id.
     *
     * @param campsiteID The ID of the campsite to delete.
     */
    suspend fun deleteCampsite(campsiteID: String) = delete(campsiteID)

    /**
     * Archives every campsite owned by a user by setting it inactive. The campsites are
     * kept for past bookings and records, but no longer shown to anyone browsing. Reads
     * Firestore rather than the cache, so campsites not loaded yet are archived too.
     *
     * @param ownerId The device ID of the owner.
     * @return The archived campsites. Empty if [ownerId] is blank.
     */
    suspend fun archiveCampsitesByOwner(ownerId: String): List<Campsite> {
        if (ownerId.isBlank()) {
            return emptyList()
        }
        val snapshot = collectionRef.whereEqualTo("ownerId", ownerId).get().await()

        // Archive them all at once, so they are never left partly archived
        val batch = db.batch()
        for (document in snapshot.documents) {
            batch.update(document.reference, "active", false)
        }
        batch.commit().await()

        val archived = mutableListOf<Campsite>()
        for (document in snapshot.documents) {
            val campsite = try {
                fromDocument(document)
            } catch (e: Exception) {
                null // archived in Firestore, but not a valid campsite to cache
            } ?: continue
            val inactive = campsite.copy(active = false)
            putInCache(inactive)
            archived.add(inactive)
        }
        return archived
    }

    /**
     * Saves a campsite to Firestore under the given ID, creating it if absent.
     *
     * The rating is not saved. It belongs to the campsite's reviews (see [ReviewRepository]),
     * and may have changed since the campsite being saved was loaded.
     *
     * @param campsiteID The ID of the campsite to update.
     * @param updatedCampsite The new campsite data.
     * @throws IllegalArgumentException if the ID is blank.
     */
    suspend fun updateCampsite(campsiteID: String, updatedCampsite: Campsite) {
        require(campsiteID.isNotBlank()) { "Cannot store a campsite with a blank ID" }
        val campsite = updatedCampsite.copy(campsiteId = campsiteID)
        collectionRef.document(campsiteID)
            .set(toDocument(campsite), SetOptions.mergeFields(EDITABLE_FIELDS))
            .await()
        putInCache(campsite)
    }

    private companion object {
        /** Every field of [FirestoreCampsite] except the rating and the number of reviews. */
        val EDITABLE_FIELDS = listOf(
            "campsiteId", "ownerId", "campsiteName", "description", "address", "latitude",
            "longitude", "maxOccupancy", "rulesPolicies", "amenities", "tags", "imageIds",
            "availability", "active", "createdAt"
        )
    }

    /**
     * Grabs all campsites that exist and returns them keyed by campsite ID.
     */
    suspend fun getAllCampsites(): MutableMap<String, Campsite> {
        val campsites = mutableMapOf<String, Campsite>()
        for (campsite in getAll()) {
            campsites[campsite.campsiteId] = campsite
        }
        return campsites
    }
}

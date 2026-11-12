package com.example.lambda_bbb_events.model

/*
 * Defines campsite information to represent a listing for campsite
 */

/**
 * Represents a campsite listing.
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
data class Campsite(
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
    val imageIds: List<String> = emptyList(),
    val rating: Float = 0f, // average
    val ratingCount: Int = 0,
    val availability: AvailabilitySchedule = AvailabilitySchedule(),
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
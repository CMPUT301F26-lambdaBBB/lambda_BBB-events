package com.example.lambda_bbb_events.model

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
 */
data class Campsite(
    val campsiteId: String,
    val ownerId: String, // CampsiteOwner deviceId
    val campsiteName: String,
    val description: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val maxOccupancy: Int,
    val rulesPolicies: String = "",
    val amenities: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val imageIds: List<String> = emptyList(),
    val rating: Float = 0f, // average
    val ratingCount: Int = 0,
    val availability: AvailabilitySchedule = AvailabilitySchedule()
)
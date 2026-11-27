package com.example.lambda_bbb_events.controller

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.lambda_bbb_events.model.campsite.AvailabilitySchedule
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.campsite.CampsiteFilters
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.model.local.InMemoryRecentlyViewedStore
import com.example.lambda_bbb_events.model.local.RecentlyViewedStore
import com.example.lambda_bbb_events.store.CampsiteStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Handles campsite operations, searching, filtering, and recently viewed campsites.
 *
 * @param campsiteStore Holds the campsites.
 * @param recentlyViewedStore Saves recently viewed campsites between app launches.
 * @param scope Runs reads and writes of [recentlyViewedStore] off the main thread.
 */
class CampsiteController(
    private val campsiteStore: CampsiteStore,
    private val recentlyViewedStore: RecentlyViewedStore = InMemoryRecentlyViewedStore(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    companion object {
        const val MAX_RECENTLY_VIEWED = 10
        const val MAX_FEATURED = 10

        /** The lowest average rating, in stars, that counts as positive reviews. */
        const val MIN_FEATURED_RATING = 4f
        const val MAX_RECENTLY_ADDED = 10
    }

    private var _searchQuery = ""

    val searchQuery: String
        get() = _searchQuery

    /** The filters currently applied to [filteredCampsites] and [getSearchResults]. */
    var filters = CampsiteFilters()
        private set

    /**
     * Every campsite, including archived (inactive) ones. Use it to look up campsites that
     * records refer to, such as past bookings. Browsing uses [activeCampsites].
     */
    val campsites: Map<String, Campsite>
        get() = campsiteStore.campsites

    /** Campsites that can be browsed and booked. Archived (inactive) ones are left out. */
    val activeCampsites: Map<String, Campsite>
        get() = campsites.filterValues { it.active }

    val filteredCampsites: Map<String, Campsite>
        get() = activeCampsites.filterValues { meetsAllFilters(it) }

    /**
     * Every different number of guests that campsites allow, smallest first, for choosing a
     * range to filter by. Other filters do not change it.
     */
    val guestOptions: List<Int>
        get() = activeCampsites.values.map { it.maxOccupancy }.distinct().sorted()

    /**
     * IDs of recently viewed campsites, most recent first. Saved views are loaded on a
     * background thread, so every use of this list is wrapped in `synchronized` to stop
     * two threads changing it at once.
     */
    private val recentlyViewedIds = mutableStateListOf<String>()
    private var lastViewedAt = 0L

    init {
        scope.launch {
            val saved = recentlyViewedStore.load(MAX_RECENTLY_VIEWED)
            synchronized(recentlyViewedIds) {
                // Campsites viewed while loading are newer, so they stay in front
                for (id in saved) {
                    if (id !in recentlyViewedIds) {
                        recentlyViewedIds.add(id)
                    }
                }
                trimRecentlyViewed()
            }
        }
    }

    /** Recently viewed campsites, most recent first. */
    val recentlyViewed: List<Campsite>
        get() {
            val result = mutableListOf<Campsite>()
            synchronized(recentlyViewedIds) {
                for (id in recentlyViewedIds) {
                    // Skip campsites that have since been deleted or archived
                    val campsite = campsites[id]
                    if (campsite != null && campsite.active) {
                        result.add(campsite)
                    }
                }
            }
            return result
        }

    /**
     * Active campsites ranked by their reviews. Campsites with positive reviews (an average
     * of at least [MIN_FEATURED_RATING] stars) come first, and within each group the
     * campsites with the most points from reviews come first. Campsites with equal points
     * are ordered by average rating, then newest first, so until campsites are reviewed this
     * is the same as the most recently added.
     *
     * @see Campsite.totalPoints
     */
    val allFeaturedCampsites: List<Campsite>
        get() = activeCampsites.values
            .sortedWith(
                compareByDescending<Campsite> { hasPositiveReviews(it) }
                    .thenByDescending { it.totalPoints() }
                    .thenByDescending { it.rating }
                    .thenByDescending { it.createdAt }
            )

    /** Featured campsite preview, limited to [MAX_FEATURED] entries. */
    val featuredCampsites: List<Campsite>
        get() = allFeaturedCampsites.take(MAX_FEATURED)

    /**
     * Active campsites, newest first. Campsites created before creation times were
     * recorded come last.
     */
    val allRecentlyAddedCampsites: List<Campsite>
        get() = activeCampsites.values
            .sortedByDescending { it.createdAt }

    /** Recently added campsite preview, limited to [MAX_RECENTLY_ADDED] entries. */
    val recentlyAddedCampsites: List<Campsite>
        get() = allRecentlyAddedCampsites.take(MAX_RECENTLY_ADDED)

    /**
     * Records that a campsite was viewed, moving it to the front of [recentlyViewed]
     * and saving it to [recentlyViewedStore].
     *
     * @param campsiteId The ID of the viewed campsite.
     */
    fun markViewed(campsiteId: String) {
        var viewedAt = 0L
        synchronized(recentlyViewedIds) {
            recentlyViewedIds.remove(campsiteId)
            recentlyViewedIds.add(0, campsiteId)
            trimRecentlyViewed()

            // Views in the same millisecond still need a distinct order when saved
            lastViewedAt = maxOf(System.currentTimeMillis(), lastViewedAt + 1)
            viewedAt = lastViewedAt
        }

        scope.launch {
            recentlyViewedStore.record(campsiteId, viewedAt, MAX_RECENTLY_VIEWED)
        }
    }

    /** Whether a campsite has been reviewed and averages at least [MIN_FEATURED_RATING] stars. */
    private fun hasPositiveReviews(campsite: Campsite): Boolean {
        return campsite.ratingCount > 0 && campsite.rating >= MIN_FEATURED_RATING
    }

    private fun trimRecentlyViewed() {
        while (recentlyViewedIds.size > MAX_RECENTLY_VIEWED) {
            recentlyViewedIds.removeAt(recentlyViewedIds.lastIndex)
        }
    }

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

    suspend fun removeCampsite(
        campsiteId: String,
        returnMessage: MutableState<String>
    ): Boolean {
        return campsiteStore.removeCampsite(campsiteId, returnMessage)
    }

    /**
     * Archives every campsite owned by a user, such as when they delete their account.
     * Archived campsites are inactive: kept for records, but hidden from browsing.
     *
     * @param ownerId The device ID of the owner.
     */
    suspend fun archiveCampsitesByOwner(ownerId: String) {
        campsiteStore.archiveCampsitesByOwner(ownerId)
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

    /**
     * Updates a campsite's editable details from form input, keeping every other field.
     *
     * Text is trimmed before saving.
     *
     * @param campsiteId The ID of the campsite to update.
     * @param name The new campsite name. Must not be blank.
     * @param address The new address. Must not be blank.
     * @param maxOccupancyText The new maximum occupancy as entered. Must be a whole number of at least 1.
     * @param description The new description.
     * @param rulesPolicies The new rules and policies.
     * @param tags The new tags.
     * @return `true` if the campsite was updated, `false` if it does not exist or the input is invalid.
     */
    fun updateCampsite(
        campsiteId: String,
        name: String,
        address: String,
        maxOccupancyText: String,
        description: String,
        rulesPolicies: String,
        tags: List<String>
    ): Boolean {
        val existing = getCampsite(campsiteId) ?: return false
        val trimmedName = name.trim()
        val trimmedAddress = address.trim()
        val maxOccupancy = maxOccupancyText.trim().toIntOrNull()

        if (trimmedName.isBlank() || trimmedAddress.isBlank() || maxOccupancy == null || maxOccupancy < 1) {
            return false
        }

        return updateCampsite(
            campsiteId,
            existing.copy(
                campsiteName = trimmedName,
                address = trimmedAddress,
                maxOccupancy = maxOccupancy,
                description = description.trim(),
                rulesPolicies = rulesPolicies.trim(),
                tags = tags.toList()
            )
        )
    }

    /**
     * Replaces a campsite's photos, and waits until they are saved. To upload or delete photos
     * as well, use [CampsitePhotoController].
     *
     * @param campsiteId The ID of the campsite.
     * @param photos The photos, starting with the thumbnail.
     * @throws Exception if saving fails, in which case the photos are not changed.
     */
    suspend fun setPhotos(campsiteId: String, photos: List<ImageMetaData>) {
        campsiteStore.setPhotos(campsiteId, photos)
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

        filters = CampsiteFilters(
            startDate = startDate,
            endDate = endDate,
            location = location.trim(),
            starRating = starRating,
            guestRange = guestRange
        )
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

    fun getAllCampsites(): List<Campsite> {
        return campsiteStore.campsites.values.toList()
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

        val start = filters.startDate ?: return true
        val end = filters.endDate ?: start
        return campsite.availability.isAvailable(start, end)
    }

    private fun meetsStarRating(
        campsite: Campsite
    ): Boolean {

        val rating = filters.starRating ?: return true
        return campsite.rating >= rating
    }

    private fun meetsGuestRange(
        campsite: Campsite
    ): Boolean {

        val range = filters.guestRange ?: return true
        return campsite.maxOccupancy in range
    }

    private fun meetsLocation(
        campsite: Campsite
    ): Boolean {

        val location = filters.location
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

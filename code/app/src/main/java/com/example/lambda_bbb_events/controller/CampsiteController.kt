package com.example.lambda_bbb_events.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.google.firebase.Timestamp

/**
 * Controller for [Campsite]-related operations.
 *
 * @property campsiteRepository The repository for [Campsite].
 */
class CampsiteController(
    private val campsiteRepository: CampsiteRepository
) {
    var searchQuery by mutableStateOf("")

    // I have filtered Campsites and non-filtered Campsites
    val campsites: List<Campsite>
        get() = campsiteRepository.campsites

    val exploreCampsites: List<Campsite>
        get() = if (campsiteRepository.isExploreFiltered) {
            campsiteRepository.filteredCampsites
        } else {
            campsiteRepository.campsites
        }

    val isFiltering: Boolean
        get() = campsiteRepository.isFiltering

    val filterError: String?
        get() = campsiteRepository.filterError

    val isExploreFiltered: Boolean
        get() = campsiteRepository.isExploreFiltered

    /**
     * Gets a [Campsite] given its campsiteId.
     *
     * @param campsiteId The `campsiteId` to search for.
     * @return The [Campsite] with the specified campsiteId if it exists. `null` otherwise.
     */
    fun getCampsite(campsiteId: String): Campsite? {
        return campsiteRepository.get(campsiteId)
    }

    /**
     * Saves the query to the [searchQuery] variable. An empty query means no search (show all).
     *
     * @param query The user search query string.
     */
    fun search(query: String) {
        searchQuery = query.trim()
    }

    /**
     * Gets the campsites that match the current search query. The query is
     * matched with the campsite name, address, and tags (ignores case).
     *
     * @return The list of campsites matching the query. If there is no query,
     * all the campsites are returned.
     */
    fun getSearchResults(): List<Campsite> {
        // no search, show everything
        if (searchQuery.isEmpty()) {
            return exploreCampsites
        }

        val result = mutableListOf<Campsite>()

        for (campsite in exploreCampsites) {
            if (matchesSearch(campsite)) {
                result.add(campsite)
            }
        }

        return result
    }

    /**
     * Checks if a campsite matches the current search query.
     *
     * @param campsite The campsite to check.
     * @return `true` if the query is in the campsite's name, address, or tags. `false` otherwise.
     */
    private fun matchesSearch(campsite: Campsite): Boolean {
        if (campsite.campsiteName.contains(searchQuery, ignoreCase = true)) {
            return true
        }

        if (campsite.address.contains(searchQuery, ignoreCase = true)) {
            return true
        }

        for (tag in campsite.tags) {
            if (tag.contains(searchQuery, ignoreCase = true)) {
                return true
            }
        }

        return false
    }

    fun applyFilters(
        startDate: Timestamp?,
        endDate: Timestamp?,
        location: String,
        minGuests: Int,
        maxGuests: Int,
        minRating: Double?
    ) {
        campsiteRepository.filterCampsites(
            startDate = startDate,
            endDate = endDate,
            location = location,
            minGuests = minGuests,
            maxGuests = maxGuests,
            minRating = minRating
        )
    }

    fun clearFilters() {
        campsiteRepository.clearExploreFilters()
    }
}
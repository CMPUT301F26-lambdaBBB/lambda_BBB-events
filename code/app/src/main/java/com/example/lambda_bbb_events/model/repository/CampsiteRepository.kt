package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.roles.CampsiteOwner
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Handles the data in the Firestore database for the [Campsite] class.
 */
class CampsiteRepository(enableListener: Boolean = true) {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")

    private val _campsites = mutableStateListOf<Campsite>()
    private val _filteredCampsites = mutableStateListOf<Campsite>()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null


    init {
        if (enableListener) {
            listenerRegistration = campsitesRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                _campsites.clear()

                snapshot?.documents?.forEach { document ->
                    val campsite = document.toObject(Campsite::class.java)
                    if (campsite != null) {
                        _campsites.add(campsite)
                    }
                }
            }
        }
    }

    val campsites: List<Campsite>
        get() = _campsites

    /**
     * Adds a new campsite to the Firestore database.
     *
     * @param campsite The campsite to add.
     */
    suspend fun addCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.campsiteId)
            .set(campsite)
            .await()

        _campsites.removeAll {
            it.campsiteId == campsite.campsiteId
        }
        _campsites.add(campsite)
    }

    /**
     * Deletes a campsite from the Firestore database.
     *
     * @param campsite The campsite to be deleted.
     */
    suspend fun deleteCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.campsiteId)
            .delete()
            .await()

        _campsites.removeAll {
            it.campsiteId == campsite.campsiteId
        }
    }

    /**
     * Overwrites an existing campsite in the Firestore database with updated data.
     *
     * @param updatedCampsite The campsite to be updated. Must have the same `campsiteId`
     * as the original document.
     */
    suspend fun updateCampsite(updatedCampsite: Campsite) {
        // assumes the old and updated campsite uses same ID bcs this function overwrites
        // the campsite with the same campsiteId
        campsitesRef.document(updatedCampsite.campsiteId)
            .set(updatedCampsite)
            .await()

        _campsites.removeAll {
            it.campsiteId == updatedCampsite.campsiteId
        }
        _campsites.add(updatedCampsite)
    }

    /**
     * Gets a [Campsite] object from the database given its campsiteId.
     *
     * @param campsiteId The `campsiteId` to search for.
     * @return The [Campsite] object with the specified campsiteId if it exists. `null` otherwise.
     */
     fun get(campsiteId: String): Campsite? {
        for (campsite in _campsites) {
            if (campsite.campsiteId == campsiteId) {
                return campsite
            }
        }
        return null
    }

    /**
     * Gets all [Campsite] of a specific [CampsiteOwner].
     *
     * @param ownerId The [CampsiteOwner] device ID.
     * @return The list of [Campsite] made by the owner.
     */
    fun getCampsitesByOwner(ownerId: String): List<Campsite> {
        val result = mutableListOf<Campsite>()

        for (campsite in _campsites) {
            if (campsite.ownerId == ownerId) {
                result.add(campsite)
            }
        }

        return result
    }

    val filteredCampsites: List<Campsite>
        get() = _filteredCampsites

    var isExploreFiltered by mutableStateOf(false)
        private set

    var isFiltering by mutableStateOf(false)
        private set

    var filterError by mutableStateOf<String?>(null)
        private set

    private var filterRequestVersion = 0

    /**
     * Filters campsites based on dates, location, guest capacity, and rating.
     *
     * Retrieves matching campsites from Firestore and excludes campsites
     * with confirmed reservations overlapping the selected dates.
     *
     * @param startDate The start date of the requested stay, or `null` if not specified.
     * @param endDate The end date of the requested stay, or `null` if not specified.
     * @param location The location to search for in the campsite address.
     * @param minGuests The minimum guest capacity.
     * @param maxGuests The maximum guest capacity.
     * @param minRating The minimum campsite rating, or `null` if not specified.
     */
    suspend fun filterCampsites(
        startDate: Timestamp?,
        endDate: Timestamp?,
        location: String,
        minGuests: Int,
        maxGuests: Int,
        minRating: Double?
    ) {
        val requestId = ++filterRequestVersion

        isExploreFiltered = true
        isFiltering = true
        filterError = null
        _filteredCampsites.clear()

        if ((startDate == null) != (endDate == null) || (startDate != null && endDate != null && startDate >= endDate)) {
            filterError = "Invalid date range"
            isFiltering = false
            return
        }

        if (minGuests < 0 || maxGuests < 0 || minGuests > maxGuests) {
            filterError = "Invalid guest range"
            isFiltering = false
            return
        }

        fun complete(results: List<Campsite>) {
            if (requestId != filterRequestVersion) {
                return
            }

            _filteredCampsites.clear()
            _filteredCampsites.addAll(results)

            filterError = null
            isFiltering = false
        }

        fun fail(error: Exception) {
            if (requestId != filterRequestVersion) {
                return
            }

            filterError = error.message ?: "Failed to filter campsites"
            isFiltering = false
        }

        var query: Query = campsitesRef

        if (minGuests > 0) {
            query = query.whereGreaterThanOrEqualTo(
                "maxOccupancy",
                minGuests
            )
        }

        if (maxGuests < 100) {
            query = query.whereLessThanOrEqualTo(
                "maxOccupancy",
                maxGuests
            )
        }

        if (minRating != null && minRating > 0) {
            query = query.whereGreaterThanOrEqualTo(
                "rating",
                minRating
            )
        }

        try {
            val snapshot = query.get().await()

            if (requestId != filterRequestVersion) {
                return
            }

            val matchingCampsites = snapshot.documents
                .mapNotNull { document ->
                    document.toObject(Campsite::class.java)
                }
                .filter { campsite ->
                    location.isBlank() ||
                            campsite.address.contains(
                                location.trim(),
                                ignoreCase = true
                            )
                }

            if (startDate != null && endDate != null) {

                val reservationSnapshot = db.collection("reservations")
                    .whereLessThan("startDate", endDate)
                    .whereGreaterThan("endDate", startDate)
                    .get()
                    .await()

                val occupiedIds = reservationSnapshot.documents
                    .filter {
                        it.getString("status") == "confirmed"
                    }
                    .mapNotNull {
                        it.getString("campsiteId")
                    }
                    .toSet()

                val availableCampsites = matchingCampsites.filterNot {
                    it.campsiteId in occupiedIds
                }


            } else {
                complete(matchingCampsites)
            }

        } catch (error: Exception) {
            fail(error)
        }
    }

    /**
     * Clears all applied campsite filters and resets the filtering state.
     *
     * Removes filtered campsite results, clears any filter errors,
     * and invalidates pending filter requests.
     */
    fun clearExploreFilters() {
        filterRequestVersion++
        isExploreFiltered = false
        isFiltering = false
        filterError = null
        _filteredCampsites.clear()
    }

    fun close() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

}
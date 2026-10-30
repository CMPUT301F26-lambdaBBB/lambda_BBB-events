package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Handles the data in the Firestore database for the [Campsite] class.
 */
class CampsiteRepository {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")

    private val _campsites = mutableStateListOf<Campsite>()

    init {
        campsitesRef.addSnapshotListener { snapshot, error ->
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

    val campsites: List<Campsite>
        get() = _campsites

    /**
     * Adds a new campsite to the Firestore database.
     *
     * @param campsite The campsite to add.
     */
    fun addCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.campsiteId).set(campsite)
    }

    /**
     * Deletes a campsite from the Firestore database.
     *
     * @param campsite The campsite to be deleted.
     */
    fun deleteCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.campsiteId).delete()
    }

    /**
     * Overwrites an existing campsite in the Firestore database with updated data.
     *
     * @param updatedCampsite The campsite to be updated. Must have the same `campsiteId`
     * as the original document.
     */
    fun updateCampsite(updatedCampsite: Campsite) {
        // assumes the old and updated campsite uses same ID bcs this function overwrites
        // the campsite with the same campsiteId
        campsitesRef.document(updatedCampsite.campsiteId).set(updatedCampsite)
    }
}
package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


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

    fun addCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.campsiteId).set(campsite)
    }

    fun deleteCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.campsiteId).delete()
    }

    fun updateCampsite(updatedCampsite: Campsite) {
        // assumes the old and updated campsite uses same ID bcs this function overwrites
        // the campsite with the same campsideId
        campsitesRef.document(updatedCampsite.campsiteId).set(updatedCampsite)
    }
}
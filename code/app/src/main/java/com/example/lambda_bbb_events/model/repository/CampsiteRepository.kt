package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

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
    suspend fun addCampsite() {
        campsitesRef.add(Campsite()).await()
    }

    suspend fun deleteCampsite(campsiteID: String) {
        campsitesRef.document(campsiteID).delete().await()
    }

    suspend fun updateCampsite(campsiteID: String, updatedCampsite: Campsite) {
        campsitesRef.document(campsiteID).set(updatedCampsite).await()
    }

    suspend fun getAllCampsites(): MutableMap<String, Campsite> {
        val campsites = mutableMapOf<String, Campsite>()
        val snapshot = campsitesRef.get().await()
        campsites.clear()
        for (document in snapshot.documents) {
            val campsite = document.toObject(Campsite::class.java)
            if (campsite != null) {
                campsites[campsite.campsiteId] = campsite
            }
        }
        return campsites
    }
}
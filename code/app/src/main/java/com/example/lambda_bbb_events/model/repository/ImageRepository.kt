package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.Image
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Handles the data in the Firestore database for the [Image] class.
 */
class ImageRepository {
    private val db = Firebase.firestore
    private val imagesRef = db.collection("images")

    private val _images = mutableStateListOf<Image>()

    init {
        imagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _images.clear()

            snapshot?.documents?.forEach { document ->
                val image = document.toObject(Image::class.java)
                if (image != null) {
                    _images.add(image)
                }
            }
        }
    }

    val images: List<Image>
        get() = _images

    /**
     * Adds a new image to the Firestore database.
     *
     * @param image The image to add.
     */
    fun addImage(image: Image) {
        imagesRef.document(image.imageId).set(image)
    }

    /**
     * Deletes an image from the Firestore database.
     *
     * @param image The image to be deleted.
     */
    fun deleteImage(image: Image) {
        imagesRef.document(image.imageId).delete()
    }

    /**
     * Overwrites an existing image in the Firestore database with updated data.
     *
     * @param updatedImage The image to be updated. Must have the same `imageId`
     * as the original document.
     */
    fun updateImage(updatedImage: Image) {
        imagesRef.document(updatedImage.imageId).set(updatedImage)
    }
}
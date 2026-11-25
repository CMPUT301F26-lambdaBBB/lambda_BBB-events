package com.example.lambda_bbb_events.controller

import android.graphics.Bitmap
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.model.services.ImageProcessor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Changes the photos of campsites that already exist. Every change is saved straight away.
 *
 * The first photo is the thumbnail shown on campsite cards.
 *
 * @param campsites Holds the campsites.
 * @param images Stores the photos.
 */
class CampsitePhotoController(
    private val campsites: CampsiteController,
    private val images: ImageProcessor
) {
    companion object {
        /** The most photos a campsite can have. */
        const val MAX_PHOTOS = 5
    }

    /** Gets a campsite's photos, or none if the campsite does not exist. */
    fun photosOf(campsiteId: String): List<ImageMetaData> =
        campsites.getCampsite(campsiteId)?.imageIds.orEmpty()

    /**
     * Uploads a photo and adds it after the campsite's other photos. If that fails, whatever
     * was uploaded is deleted, so no photos are stored that nothing shows.
     *
     * @param campsiteId The ID of the campsite.
     * @param uploaderId The ID of the user adding the photo.
     * @param photo The photo.
     * @throws IllegalStateException if the campsite already has [MAX_PHOTOS] photos.
     * @throws Exception if the photo could not be uploaded or saved.
     */
    suspend fun addPhoto(campsiteId: String, uploaderId: String, photo: Bitmap) {
        check(photosOf(campsiteId).size < MAX_PHOTOS) { "The campsite has too many photos" }
        val imageId = UUID.randomUUID().toString()

        val uploaded = try {
            images.uploadImage(imageId = imageId, uploaderUserId = uploaderId, bitmap = photo)
        } catch (e: Exception) {
            withContext(NonCancellable) { images.deleteImage(imageId) }
            throw e
        }

        try {
            savePhotos(campsiteId) { it + uploaded }
        } catch (e: CancellationException) {
            throw e // the photo may have been saved, so it must be kept
        } catch (e: Exception) {
            images.deleteImage(imageId)
            throw e
        }
    }

    /**
     * Removes a photo from a campsite, then deletes it. If deleting it fails, it is still
     * removed from the campsite.
     *
     * @throws Exception if the change could not be saved, in which case the photo is kept.
     */
    suspend fun removePhoto(campsiteId: String, imageId: String) {
        savePhotos(campsiteId) { photos -> photos.filter { it.imageId != imageId } }
        images.deleteImage(imageId)
    }

    /**
     * Moves a photo to the front, making it the thumbnail. The others keep their order.
     *
     * @throws Exception if the change could not be saved.
     */
    suspend fun makeThumbnail(campsiteId: String, imageId: String) {
        savePhotos(campsiteId) { photos -> photos.sortedBy { it.imageId != imageId } }
    }

    /**
     * Changes the part of a photo that stays in view when it is cropped.
     *
     * @throws Exception if the change could not be saved.
     */
    suspend fun changeFocus(campsiteId: String, imageId: String, focusX: Float, focusY: Float) {
        savePhotos(campsiteId) { photos ->
            photos.map {
                if (it.imageId == imageId) it.copy(focusX = focusX, focusY = focusY) else it
            }
        }
    }

    /** Saves a change to the photos. Once started, it finishes even if the screen is closed. */
    private suspend fun savePhotos(
        campsiteId: String,
        change: (List<ImageMetaData>) -> List<ImageMetaData>
    ) {
        withContext(NonCancellable) {
            campsites.setPhotos(campsiteId, change(photosOf(campsiteId)))
        }
    }
}

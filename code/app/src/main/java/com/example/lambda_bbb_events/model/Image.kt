package com.example.lambda_bbb_events.model

/**
 * Represents an image uploaded by a user which stores its unique ID,
 * the user ID of the uploader, and its download URL (for now).
 *
 * @property imageId The unique ID of the image.
 * @property uploaderUserId The ID of the user who uploaded the image.
 * @property url The URL used to access the image.
 */
data class Image (
    val imageId: String,
    val uploaderUserId: String,
    val url: String // will contain URL instead of file, i think firestore free version cant store image files so idk how this will work
)
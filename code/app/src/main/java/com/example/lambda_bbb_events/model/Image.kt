package com.example.lambda_bbb_events.model

/*
 * Stores image metadata and URLs for user-uploaded images.
 */

/**
 * Represents an image uploaded by a user which stores its unique ID,
 * the user ID of the uploader, the number of chunks it was split into, and its download URL (for now).
 *
 * @property imageId The unique ID of the image.
 * @property uploaderUserId The ID of the user who uploaded the image.
 * @property chunkCount The number of chunks the image was split into.
 * @property uploadTimestamp The timestamp of when the image was uploaded.
 */
data class ImageMetaData(
    val imageId: String = "",
    val uploaderUserId: String = "",
    val chunkCount: Int = 0,
    val uploadTimestamp: Long = System.currentTimeMillis()
)

data class ImageChunk(
    val imageId: String = "",
    val priority: Int = 0,
    val bandHeight: Int = 0,
    val imageBytes: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImageChunk) return false

        return imageId == other.imageId && priority == other.priority
    }

    override fun hashCode(): Int {
        var result = imageId.hashCode()
        result = 31 * result + priority
        return result
    }
}
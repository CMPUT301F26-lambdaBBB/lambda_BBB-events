package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.image.ImageChunk
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Handles the data in the Firestore database for images. Each image's [ImageMetaData] is
 * stored as a document, and its [ImageChunk]s are stored in that document's `chunks`
 * subcollection.
 */
class ImageRepository(
    db: FirebaseFirestore = Firebase.firestore
) : Repository<ImageMetaData>("images", db) {

    override fun idOf(item: ImageMetaData) = item.imageId

    override fun fromDocument(document: DocumentSnapshot): ImageMetaData? =
        document.toObject(ImageMetaData::class.java)

    private fun chunksRef(imageId: String) =
        collectionRef.document(imageId).collection("chunks")

    /**
     * Deletes an image's chunks, then its metadata.
     *
     * @param id The ID of the image to delete.
     */
    override suspend fun delete(id: String) {
        val chunkDocs = chunksRef(id).get().await()

        chunkDocs.documents.forEach { doc -> doc.reference.delete().await() }

        super.delete(id)
    }

    /** Saves image metadata and the chunks that make up the encoded image. */
    suspend fun uploadImage(
        metadata: ImageMetaData,
        chunks: List<ImageChunk>
    ): Boolean {
        try {
            add(metadata)

            val chunkCollection = chunksRef(metadata.imageId)

            chunks.forEach { chunk -> chunkCollection.document(chunk.priority.toString())
                .set(mapOf("imageId" to chunk.imageId, "priority" to chunk.priority, "bandHeight" to chunk.bandHeight, "imageBytes" to Blob.fromBytes(chunk.imageBytes)))
                .await() }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /** Loads an image's chunks in priority order, returning an empty list if the read fails. */
    suspend fun getImageChunks(
        imageId: String
    ): List<ImageChunk> {

        try {
            val documents = chunksRef(imageId).get().await().documents
            val chunks = mutableListOf<ImageChunk>()

            for (document in documents) {
                // Skip chunks that are missing their image data
                val blob = document.getBlob("imageBytes")
                if (blob == null) {
                    continue
                }

                chunks.add(
                    ImageChunk(
                        imageId = document.getString("imageId") ?: "",
                        priority = document.getLong("priority")?.toInt() ?: 0,
                        bandHeight = document.getLong("bandHeight")?.toInt() ?: 0,
                        imageBytes = blob.toBytes()
                    )
                )
            }
            return chunks.sortedBy { it.priority }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /** Loads the stored metadata for [imageId], if available. */
    suspend fun getImageMetadata(
        imageId: String
    ): ImageMetaData? {
        return try {
            get(imageId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Loads the ordered chunks for an image with stored metadata. */
    suspend fun getImage(
        imageId: String
    ): List<ImageChunk> {

        val metadata = getImageMetadata(imageId)
            ?: throw IllegalStateException("Image metadata missing")

        val chunks = getImageChunks(imageId)

        if (chunks.size != metadata.chunkCount) {
            throw IllegalStateException("Missing image chunks")
        }
        return chunks
    }

    /** Deletes the image and its chunks, returning whether the operation succeeded. */
    suspend fun deleteImage(
        imageId: String
    ): Boolean {
        return try {
            delete(imageId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

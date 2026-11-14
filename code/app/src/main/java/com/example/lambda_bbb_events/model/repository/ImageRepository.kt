package com.example.lambda_bbb_events.model.repository

import android.graphics.Bitmap
import com.example.lambda_bbb_events.model.ImageChunk
import com.example.lambda_bbb_events.model.ImageMetaData
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ImageRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val imagesRef = db.collection("images")

    suspend fun uploadImage(
        metadata: ImageMetaData,
        chunks: List<ImageChunk>
    ): Boolean {
        try {
            imagesRef.document(metadata.imageId).set(metadata).await()

            val chunkCollection = imagesRef.document(metadata.imageId).collection("chunks")

            chunks.forEach { chunk -> chunkCollection.document(chunk.priority.toString()).set(chunk).await() }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun getImageChunks(
        imageId: String
    ): List<ImageChunk> {

        return try {
            imagesRef.document(imageId).collection("chunks").get().await()
                .documents.mapNotNull { it.toObject(ImageChunk::class.java) }
                .sortedBy { it.priority }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getImageMetadata(
        imageId: String
    ): ImageMetaData? {
        return try {
            imagesRef.document(imageId).get().await().toObject(ImageMetaData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

    suspend fun deleteImage(
        imageId: String
    ): Boolean {
        return try {
            val chunkDocs = imagesRef.document(imageId).collection("chunks").get().await()

            chunkDocs.documents.forEach { doc -> doc.reference.delete().await() }

            imagesRef.document(imageId).delete().await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
package com.example.lambda_bbb_events.model

import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import androidx.core.graphics.createBitmap
import com.example.lambda_bbb_events.model.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 */
class ImageProcessor {
    private val imageRepository: ImageRepository = ImageRepository()

    /**
     *
     */
    fun downscaleImage(image: Bitmap): Bitmap {
        val maxWidth = 1440
        val maxHeight = 2560
        val ratio = minOf(maxWidth.toFloat() / image.width, maxHeight.toFloat() / image.height, 1f)

        return image.scale((image.width * ratio).toInt(), (image.height * ratio).toInt())
    }

    /**
     *
     */
    fun splitImage(imageId: String, bitmap: Bitmap): List<ImageChunk> {
        require(bitmap.width > 0)
        require(bitmap.height > 0)

        val sliceHeight = 256
        val chunks = mutableListOf<ImageChunk>()

        var priority = 0
        var y = 0

        while (y < bitmap.height) {
            val height = minOf(sliceHeight, bitmap.height - y)
            val slice = Bitmap.createBitmap(bitmap, 0, y, bitmap.width, height)
            val stream = ByteArrayOutputStream()

            slice.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            chunks.add(ImageChunk(imageId = imageId, priority = priority, bandHeight = height, imageBytes = stream.toByteArray()))
            priority++
            y += sliceHeight
        }
        return chunks
    }

    /**
     *
     */
    fun rebuildImage(
        chunks: List<ImageChunk>
    ): Bitmap {
        val bitmaps = chunks
            .sortedBy { it.priority }
            .mapNotNull { BitmapFactory.decodeByteArray(it.imageBytes, 0, it.imageBytes.size)
            }
        require(bitmaps.isNotEmpty()) { "No valid image chunks provided." }

        val width = bitmaps.first().width
        require(bitmaps.all { it.width == width }) { "Chunk widths do not match" }
        val totalHeight = bitmaps.sumOf { it.height }
        val result = createBitmap(width, totalHeight)
        val canvas = Canvas(result)
        var y = 0

        for (bitmap in bitmaps) {
            canvas.drawBitmap(bitmap, 0f, y.toFloat(), null)
            y += bitmap.height
        }
        return result
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Loads an image from the repository and calls the appropriate callback.
     */
    fun loadImage(
        imageId: String,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        scope.launch {
            try {
                Log.d("IMAGE", "Loading image $imageId")

                val chunks = imageRepository.getImage(imageId)

                Log.d("IMAGE", "Retrieved ${chunks.size} chunks")

                val bitmap = rebuildImage(chunks)

                Log.d(
                    "IMAGE",
                    "Bitmap rebuilt ${bitmap.width}x${bitmap.height}"
                )

                withContext(Dispatchers.Main) {
                    onSuccess(bitmap)
                }
            } catch (e: Exception) {
                Log.e("IMAGE", "Load failed", e)

                withContext(Dispatchers.Main) {
                    onFailure(e)
                }
            }
        }
    }

    suspend fun uploadImage(
        imageId: String,
        uploaderUserId: String,
        bitmap: Bitmap
    ): ImageMetaData {

        val scaledBitmap = downscaleImage(bitmap)

        val chunks = splitImage(imageId, scaledBitmap)

        val metadata = ImageMetaData(imageId = imageId, uploaderUserId = uploaderUserId, chunkCount = chunks.size)

        imageRepository.uploadImage(metadata = metadata, chunks = chunks)

        return metadata
    }
}

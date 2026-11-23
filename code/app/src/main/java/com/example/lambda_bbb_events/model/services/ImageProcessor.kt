package com.example.lambda_bbb_events.model.services

import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import android.util.LruCache
import androidx.core.graphics.createBitmap
import com.example.lambda_bbb_events.model.image.ImageChunk
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.model.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Prepares images for storage and loads them back for display.
 */
class ImageProcessor(
    private val imageRepository: ImageRepository = ImageRepository()
) {
    companion object {
        /** How many loaded bitmaps are kept in memory. */
        const val MAX_CACHED_IMAGES = 20

        /**
         * Gets the largest power of 2 that an image can be shrunk by while still covering a
         * [targetWidth] by [targetHeight] pixel area. A `null` or non-positive target places
         * no limit on that dimension.
         *
         * @return The sample size, or 1 if neither target is given.
         */
        fun sampleSizeFor(sourceWidth: Int, sourceHeight: Int, targetWidth: Int?, targetHeight: Int?): Int {
            // A missing or non-positive target means there is no limit in that direction
            val width = targetWidth ?: 0
            val height = targetHeight ?: 0
            if ((width <= 0 && height <= 0) || sourceWidth <= 0 || sourceHeight <= 0) {
                return 1
            }

            // Keep halving while the image would still cover the target
            var sampleSize = 1
            while ((width <= 0 || sourceWidth / (sampleSize * 2) >= width) &&
                (height <= 0 || sourceHeight / (sampleSize * 2) >= height)
            ) {
                sampleSize *= 2
            }
            return sampleSize
        }
    }

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
        if (bitmap.width <= 0 || bitmap.height <= 0) {
            throw IllegalArgumentException("Image has no pixels")
        }

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
     * Rebuilds a bitmap from its chunks, stacking them in priority order.
     *
     * @param chunks The chunks of a single image.
     * @param sampleSize Decodes every chunk at 1/[sampleSize] of its size. Must be a power of 2.
     */
    fun rebuildImage(
        chunks: List<ImageChunk>,
        sampleSize: Int = 1
    ): Bitmap {
        val options = BitmapFactory.Options()
        options.inSampleSize = sampleSize

        // Decode each chunk, skipping any that are not valid images
        val bitmaps = mutableListOf<Bitmap>()
        for (chunk in chunks.sortedBy { it.priority }) {
            val bitmap = BitmapFactory.decodeByteArray(chunk.imageBytes, 0, chunk.imageBytes.size, options)
            if (bitmap != null) {
                bitmaps.add(bitmap)
            }
        }
        if (bitmaps.isEmpty()) {
            throw IllegalArgumentException("No valid image chunks provided.")
        }

        val width = bitmaps[0].width
        var totalHeight = 0
        for (bitmap in bitmaps) {
            if (bitmap.width != width) {
                throw IllegalArgumentException("Chunk widths do not match")
            }
            totalHeight += bitmap.height
        }
        val result = createBitmap(width, totalHeight)
        val canvas = Canvas(result)
        var y = 0

        for (bitmap in bitmaps) {
            canvas.drawBitmap(bitmap, 0f, y.toFloat(), null)
            y += bitmap.height
        }
        return result
    }

    /** The most recently loaded bitmaps, so images already shown are not loaded again. */
    private val cache = LruCache<String, Bitmap>(MAX_CACHED_IMAGES)

    /** Names an image loaded for a particular size, since one image can be shown at several sizes. */
    private fun cacheKey(imageId: String, targetWidth: Int?, targetHeight: Int?): String {
        return imageId + "@" + targetWidth + "x" + targetHeight
    }

    /**
     * Gets an already loaded bitmap without reading the database.
     *
     * @param imageId The ID of the image.
     * @param targetWidth The width in pixels the image will be drawn at, or `null` for no limit.
     * @param targetHeight The height in pixels the image will be drawn at, or `null` for no limit.
     * @return The cached bitmap, or `null` if it has not been loaded at that size yet.
     */
    fun getCachedBitmap(imageId: String, targetWidth: Int? = null, targetHeight: Int? = null): Bitmap? {
        return cache.get(cacheKey(imageId, targetWidth, targetHeight))
    }

    /**
     * Loads an image from the repository, decoded no larger than needed to cover the target size.
     *
     * @param imageId The ID of the image.
     * @param targetWidth The width in pixels the image will be drawn at, or `null` for no limit.
     * @param targetHeight The height in pixels the image will be drawn at, or `null` for no limit.
     * @return The bitmap, or `null` if the image could not be loaded.
     */
    suspend fun getBitmap(imageId: String, targetWidth: Int? = null, targetHeight: Int? = null): Bitmap? {
        val cached = getCachedBitmap(imageId, targetWidth, targetHeight)
        if (cached != null) {
            return cached
        }

        // Reading and decoding images is slow, so it is done off the main thread
        return withContext(Dispatchers.IO) {
            try {
                val chunks = imageRepository.getImage(imageId)
                val sampleSize = sampleSizeFor(
                    sourceWidth = sourceWidthOf(chunks),
                    sourceHeight = chunks.sumOf { it.bandHeight },
                    targetWidth = targetWidth,
                    targetHeight = targetHeight
                )
                val bitmap = rebuildImage(chunks, sampleSize)
                cache.put(cacheKey(imageId, targetWidth, targetHeight), bitmap)
                bitmap
            } catch (e: Exception) {
                Log.e("IMAGE", "Load failed for $imageId", e)
                null
            }
        }
    }

    /** Reads the width of an image from its first chunk without decoding the pixels. */
    private fun sourceWidthOf(chunks: List<ImageChunk>): Int {
        if (chunks.isEmpty()) {
            throw IllegalStateException("Image has no chunks")
        }
        val first = chunks.minBy { it.priority }

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(first.imageBytes, 0, first.imageBytes.size, options)
        return options.outWidth
    }

    /**
     * Shrinks an image, splits it into chunks, and uploads it.
     *
     * @param imageId The ID to store the image under.
     * @param uploaderUserId The ID of the user uploading it.
     * @param bitmap The image.
     * @param focusX How far across the image its most important part is, from 0 to 1.
     * @param focusY How far down the image its most important part is, from 0 to 1.
     * @return The uploaded image's details.
     * @throws IllegalStateException if the upload failed.
     */
    suspend fun uploadImage(
        imageId: String,
        uploaderUserId: String,
        bitmap: Bitmap,
        focusX: Float = 0.5f,
        focusY: Float = 0.5f
    ): ImageMetaData {

        // Shrinking and compressing a photo is slow, so it is done off the main thread
        val chunks = withContext(Dispatchers.Default) {
            val scaledBitmap = downscaleImage(bitmap)
            splitImage(imageId, scaledBitmap)
        }

        val metadata = ImageMetaData(
            imageId = imageId,
            uploaderUserId = uploaderUserId,
            chunkCount = chunks.size,
            focusX = focusX,
            focusY = focusY
        )

        val uploaded = imageRepository.uploadImage(metadata = metadata, chunks = chunks)
        if (!uploaded) {
            throw IllegalStateException("Could not upload the image.")
        }

        return metadata
    }

    /**
     * Deletes an uploaded image.
     *
     * @param imageId The ID of the image to delete.
     * @return `true` if it was deleted, `false` if deleting it failed.
     */
    suspend fun deleteImage(imageId: String): Boolean {
        return imageRepository.deleteImage(imageId)
    }
}

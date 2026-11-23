package com.example.lambda_bbb_events.model

import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import android.util.LruCache
import androidx.core.graphics.createBitmap
import com.example.lambda_bbb_events.model.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Prepares images for storage and loads them back for display.
 */
class ImageProcessor(
    private val imageRepository: ImageRepository = ImageRepository()
) {
    companion object {
        /**
         * Gets the largest power of 2 that an image can be shrunk by while still covering a
         * [targetWidth] by [targetHeight] pixel area. A `null` or non-positive target places
         * no limit on that dimension.
         *
         * @return The sample size, or 1 if neither target is given.
         */
        fun sampleSizeFor(sourceWidth: Int, sourceHeight: Int, targetWidth: Int?, targetHeight: Int?): Int {
            val width = targetWidth?.takeIf { it > 0 }
            val height = targetHeight?.takeIf { it > 0 }
            if ((width == null && height == null) || sourceWidth <= 0 || sourceHeight <= 0) {
                return 1
            }
            var sampleSize = 1
            while ((width == null || sourceWidth / (sampleSize * 2) >= width) &&
                (height == null || sourceHeight / (sampleSize * 2) >= height)
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
     * Rebuilds a bitmap from its chunks, stacking them in priority order.
     *
     * @param chunks The chunks of a single image.
     * @param sampleSize Decodes every chunk at 1/[sampleSize] of its size. Must be a power of 2.
     */
    fun rebuildImage(
        chunks: List<ImageChunk>,
        sampleSize: Int = 1
    ): Bitmap {
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmaps = chunks
            .sortedBy { it.priority }
            .mapNotNull { BitmapFactory.decodeByteArray(it.imageBytes, 0, it.imageBytes.size, options)
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

    /** Recently loaded bitmaps, keyed by image ID and sample size, limited to 1/8 of the heap. */
    private val cache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 8 / 1024).toInt()) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount / 1024
    }

    /** Full-size (width, height) of every image loaded so far. */
    private val sourceSizes = ConcurrentHashMap<String, Pair<Int, Int>>()

    private fun cacheKey(imageId: String, sampleSize: Int) = "$imageId@$sampleSize"

    /**
     * Gets an already loaded bitmap without reading the database.
     *
     * @param imageId The ID of the image.
     * @param targetWidth The width in pixels the image will be drawn at, or `null` for no limit.
     * @param targetHeight The height in pixels the image will be drawn at, or `null` for no limit.
     * @return The cached bitmap, or `null` if it has not been loaded at that size yet.
     */
    fun getCachedBitmap(imageId: String, targetWidth: Int? = null, targetHeight: Int? = null): Bitmap? {
        val (sourceWidth, sourceHeight) = sourceSizes[imageId] ?: return null
        return cache.get(cacheKey(imageId, sampleSizeFor(sourceWidth, sourceHeight, targetWidth, targetHeight)))
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
        getCachedBitmap(imageId, targetWidth, targetHeight)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val chunks = imageRepository.getImage(imageId)
                val sourceWidth = sourceWidthOf(chunks)
                val sourceHeight = chunks.sumOf { it.bandHeight }
                sourceSizes[imageId] = sourceWidth to sourceHeight

                val sampleSize = sampleSizeFor(sourceWidth, sourceHeight, targetWidth, targetHeight)
                val bitmap = rebuildImage(chunks, sampleSize)
                cache.put(cacheKey(imageId, sampleSize), bitmap)
                bitmap
            } catch (e: Exception) {
                Log.e("IMAGE", "Load failed for $imageId", e)
                null
            }
        }
    }

    private fun sourceWidthOf(chunks: List<ImageChunk>): Int {
        val first = chunks.minByOrNull { it.priority }
            ?: throw IllegalStateException("Image has no chunks")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(first.imageBytes, 0, first.imageBytes.size, bounds)
        return bounds.outWidth
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

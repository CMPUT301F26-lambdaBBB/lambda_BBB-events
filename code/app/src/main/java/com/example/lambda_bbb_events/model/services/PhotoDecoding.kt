package com.example.lambda_bbb_events.model.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads a photo the user picked into a bitmap. Reading a large photo is slow, so it is done
 * off the main thread to keep the screen responsive.
 *
 * @param context Used to open the photo.
 * @param uri The picked photo.
 * @param maxSize If given, the photo is shrunk so neither side is longer than this many pixels.
 * @return The photo as a bitmap.
 */
suspend fun decodePhoto(context: Context, uri: Uri, maxSize: Int? = null): Bitmap {
    return withContext(Dispatchers.IO) {
        val photo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            val stream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Could not read the selected image.")
            val decoded = BitmapFactory.decodeStream(stream)
            stream.close()
            decoded
        }

        val longestSide = maxOf(photo.width, photo.height)
        if (maxSize == null || longestSide <= maxSize) {
            photo
        } else {
            val ratio = maxSize.toFloat() / longestSide
            photo.scale((photo.width * ratio).toInt(), (photo.height * ratio).toInt())
        }
    }
}

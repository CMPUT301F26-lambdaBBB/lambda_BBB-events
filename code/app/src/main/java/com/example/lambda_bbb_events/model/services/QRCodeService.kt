package com.example.lambda_bbb_events.model.services

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/*
 * Defines generating and scanning QR codes to navigate to campsite details.
 */

/**
 * Generates a QR code from the given content.
 *
 * @param content The content to encode in the QR code.
 * @param width The width of the QR code in pixels.
 * @param height The height of the QR code in pixels.
 * @return A [Bitmap] representing the QR code, or `null` if an error occurred.
 */
fun generateQR(
    content: String,
    width: Int,
    height: Int
): Bitmap? {
    if (content.isBlank()) return null
    return try{
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


/**
 * Navigates to a campsite details page using QR code content.
 *
 * @param navController The NavController used for navigation.
 * @param qrContent A campsite ID or URL ending with a campsite ID.
 */
fun goToPage(
    navController: NavController,
    qrContent: String
){
    val campsiteId = parseCampsiteId(qrContent)
    if (campsiteId != null) {
        navController.navigate("campsite_details/$campsiteId")
    }
}

/**
 * Launches the Google Play Services QR code scanner and navigates to the corresponding campsite details page
 *
 * @param context The current Android [Context].
 * @param navController The [NavController] to use for navigation.
 */
fun scanAndGoToPage(
    context: Context,
    navController: NavController
) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .enableAutoZoom()
        .build()

    val scanner = GmsBarcodeScanning.getClient(context, options)

    scanner.startScan()
        .addOnSuccessListener { barcode ->
            val rawValue = barcode.rawValue
            if (!rawValue.isNullOrBlank()) {
                goToPage(navController, rawValue)
            }
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}

/**
 * Parses the campsite ID from the QR code content.
 */
private fun parseCampsiteId(qrContent: String): String? {
    return if (qrContent.contains("/")) {
        qrContent.substringAfterLast("/").trim()
    } else {
        qrContent.trim()
    }
}

/**
 * Saves a bitmap image to the device's Pictures gallery directory.
 *
 * @param context The Android [Context].
 * @param bitmap The [Bitmap] to save.
 * @param filename The desired filename (without extension).
 * @return `true` if saved successfully, `false` otherwise.
 */
fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    filename: String
): Boolean {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.png")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LambdaEvents")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return false

    return try {
        resolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
package com.example.lambda_bbb_events.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
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
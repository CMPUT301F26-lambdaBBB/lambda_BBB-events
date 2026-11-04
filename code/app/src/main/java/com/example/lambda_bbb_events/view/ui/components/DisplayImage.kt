package com.example.lambda_bbb_events.view.ui.components

import android.webkit.URLUtil
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage

/**
 * Used to display images asynchronously while also not blocking page loading utilizing Coil's [AsyncImage]. This means images won't stop page loading and will load over time even after page is shown.
 *
 * Shows an image given a url ([URLUtil.isValidUrl]) otherwise if given a bad url it will run whatever function is given to [onBadUrl].
 *
 * Providing your own [Modifier] allows you to customize the image such as rounding the image and increasing the size of an image.
 *
 * Example:
 * ```
 * showImage (
        url = imageUrl,
        imageDescription = description,
        onBackUrl = Text("Failed to display image"),
        imageModifier = Modifier.size(150.dp).clip(RoundedCornerShape(12.dp))
    )
 * ```
 *
 * @param url The url the image is loaded from.
 * @param imageDescription The description of the image for text readers.
 * @param imageModifier The [Modifier] that is applied to the image.
 **/
@Composable
fun showImage(url: String,
              imageDescription: String = "",
              onBadUrl: @Composable (() -> Unit)? = null,
              imageModifier: Modifier = Modifier
): Boolean {
    if (!URLUtil.isValidUrl(url)) {
        onBadUrl?.invoke()
        return false
    }
    AsyncImage(
        model = url,
        contentDescription = imageDescription,
        modifier = imageModifier
    )
    return true
}
package com.example.lambda_bbb_events.view.ui.components

import android.webkit.URLUtil
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage


/**
 * Displays an image asynchronously using Coil's [AsyncImage].
 *
 * Loads images from valid URLs and invokes [onBadUrl]
 * when the URL is invalid. Supports custom image styling.
 *
 * @param url The URL of the image to load.
 * @param imageDescription Description for accessibility.
 * @param onBadUrl Callback for handling invalid URLs.
 * @param imageModifier Customizes the image's appearance.
 */
@Composable
fun showImage(url: String, // fix showimg functionality
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
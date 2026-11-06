package com.example.lambda_bbb_events.view.ui.components

import android.webkit.URLUtil
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage


/*
 * Provides a reusable composable for displaying images from URLs using Coil.
 *
 * functionality not fully implemented yet
 */


/**
 * Displays an image asynchronously using Coil's [AsyncImage].
 *
 * Checks the URL before loading and displays fallback content
 * when the URL is invalid.
 *
 * @param url The URL of the image to load.
 * @param imageDescription Description for accessibility.
 * @param onBadUrl Optional composable content for invalid URLs.
 * @param imageModifier Customizes the image's appearance.
 * @return `true` if the URL passes validation, `false` otherwise.
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
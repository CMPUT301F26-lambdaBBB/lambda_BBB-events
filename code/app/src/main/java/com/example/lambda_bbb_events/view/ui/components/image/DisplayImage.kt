
package com.example.lambda_bbb_events.view.ui.components.image

import android.webkit.URLUtil
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage

/*
 * Provides a reusable composable for displaying images
 * from URLs using Coil.
 *
 * Uses Material 3 theme colors for fallback content.
 */

/**
 * Displays an image asynchronously using Coil's [AsyncImage].
 *
 * Checks the URL before loading and displays themed fallback
 * content when the URL is invalid.
 *
 * @param url The URL of the image to load.
 * @param imageDescription Description for accessibility.
 * @param onBadUrl Optional composable content for invalid URLs.
 * @param imageModifier Customizes the image's appearance.
 * @return true if the URL passes validation, false otherwise.
 */
@Composable
fun showImage(
    url: String,
    imageDescription: String = "",
    onBadUrl: @Composable (() -> Unit)? = null,
    imageModifier: Modifier = Modifier
): Boolean {
    if (!URLUtil.isValidUrl(url)) {
        if (onBadUrl != null) {
            onBadUrl()
        } else {
            Box(
                modifier = imageModifier.background(
                    MaterialTheme.colorScheme.surfaceVariant
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Image unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        return false
    }

    AsyncImage(
        model = url,
        contentDescription = imageDescription,
        modifier = imageModifier
    )

    return true
}

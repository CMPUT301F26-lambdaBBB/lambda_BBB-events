package com.example.lambda_bbb_events.view.ui.components

import android.webkit.URLUtil
import androidx.compose.runtime.Composable
import coil3.compose.AsyncImage

@Composable
fun showImage(url: String, imageDescription: String = ""): Boolean {
    if (!URLUtil.isValidUrl(url)) {
        return false
    }
    AsyncImage(
        model = url,
        contentDescription = imageDescription
    )
    return true
}
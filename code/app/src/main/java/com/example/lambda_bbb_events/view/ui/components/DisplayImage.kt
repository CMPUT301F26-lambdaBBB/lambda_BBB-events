package com.example.lambda_bbb_events.view.ui.components

import android.graphics.Bitmap
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import com.example.lambda_bbb_events.model.ImageProcessor


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
 * @return `true` if the URL passes validation, `false` otherwise.
 */
@Composable
fun CampsiteImage(imageId: String, imageProcessor: ImageProcessor, modifier: Modifier = Modifier, contentDescription: String = "", loadImage: ImageProcessor.(String, () -> Unit?, () -> Unit?) -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(imageId) {
        imageProcessor.loadImage(imageId = imageId,
            onSuccess = { bitmap = it
                        loading = false },
            onFailure = { loading = false }
        )
    }

    when {
        loading -> { CircularProgressIndicator() }
        bitmap != null -> { AsyncImage(model = bitmap, contentDescription = contentDescription, modifier = modifier) }
        else -> { Text("Unable to load image") }
    }
}

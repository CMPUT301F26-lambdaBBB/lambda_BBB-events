package com.example.lambda_bbb_events.view.ui.components.common

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/*
 * Lets owners choose which part of a photo stays in view when it is cropped.
 */

/** Size of the ring that marks the chosen spot. */
private val MARKER_SIZE = 28.dp

/**
 * Gets the alignment that keeps a photo's focus point in view when the photo is cropped to
 * fill a space. A focus of (0.5, 0.5) is the centre of the photo.
 *
 * @param focusX How far across the photo the focus point is, from 0 to 1.
 * @param focusY How far down the photo the focus point is, from 0 to 1.
 */
fun focusAlignment(focusX: Float, focusY: Float): Alignment {
    // Alignment uses -1 for the left or top edge and 1 for the right or bottom edge
    return BiasAlignment(focusX * 2 - 1, focusY * 2 - 1)
}

/**
 * Displays a dialog where the owner taps the most important part of a photo, such as a
 * person, so it stays in view when the photo is cropped on cards and thumbnails. Previews of
 * both card shapes update as they tap.
 *
 * @param photo The photo to choose a focus point on.
 * @param initialFocusX The current focus point across the photo, from 0 to 1.
 * @param initialFocusY The current focus point down the photo, from 0 to 1.
 * @param onDismiss Called when the dialog is closed without saving.
 * @param onSave Called with the chosen focus point.
 */
@Composable
fun PhotoFocusDialog(
    photo: Bitmap,
    initialFocusX: Float,
    initialFocusY: Float,
    onDismiss: () -> Unit,
    onSave: (focusX: Float, focusY: Float) -> Unit
) {
    var focusX by remember { mutableFloatStateOf(initialFocusX) }
    var focusY by remember { mutableFloatStateOf(initialFocusY) }
    val image = remember(photo) { photo.asImageBitmap() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Keep in view") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tap the most important part of the photo, such as a person. " +
                            "Cards and thumbnails will crop around it.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FocusPicker(
                        image = image,
                        aspectRatio = photo.width.toFloat() / photo.height,
                        focusX = focusX,
                        focusY = focusY,
                        onFocusChange = { x, y ->
                            focusX = x
                            focusY = y
                        }
                    )
                }

                Text(text = "Preview", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // A wide card, like on the home screen
                    CroppedPreview(
                        image = image,
                        focusX = focusX,
                        focusY = focusY,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f)
                            .testTag("focus_preview_wide")
                    )
                    // A list card, which shows a narrow strip of the photo
                    CroppedPreview(
                        image = image,
                        focusX = focusX,
                        focusY = focusY,
                        modifier = Modifier
                            .width(56.dp)
                            .height(60.dp)
                            .testTag("focus_preview_narrow")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(focusX, focusY) },
                modifier = Modifier.testTag("focus_save")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Displays the whole photo with a ring on the focus point, which moves to wherever is tapped.
 */
@Composable
private fun FocusPicker(
    image: ImageBitmap,
    aspectRatio: Float,
    focusX: Float,
    focusY: Float,
    onFocusChange: (Float, Float) -> Unit
) {
    // The box has the photo's exact shape, so a tap's position in the box is its position
    // in the photo
    BoxWithConstraints(
        modifier = Modifier
            .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
            .testTag("focus_picker")
            // Taps are handled before the rounded clip, so the corners can be tapped too
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    val x = (tap.x / size.width).coerceIn(0f, 1f)
                    val y = (tap.y / size.height).coerceIn(0f, 1f)
                    onFocusChange(x, y)
                }
            }
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            bitmap = image,
            contentDescription = "Photo",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        // Centre the ring on the focus point
        Box(
            modifier = Modifier
                .offset(
                    x = maxWidth * focusX - MARKER_SIZE / 2,
                    y = maxHeight * focusY - MARKER_SIZE / 2
                )
                .size(MARKER_SIZE)
                .border(3.dp, Color.White, CircleShape)
                .border(4.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
        )
    }
}

/**
 * Displays the photo cropped to fill [modifier]'s shape around the focus point.
 */
@Composable
private fun CroppedPreview(
    image: ImageBitmap,
    focusX: Float,
    focusY: Float,
    modifier: Modifier
) {
    Image(
        bitmap = image,
        contentDescription = "Cropped preview",
        contentScale = ContentScale.Crop,
        alignment = focusAlignment(focusX, focusY),
        modifier = modifier.clip(RoundedCornerShape(8.dp))
    )
}

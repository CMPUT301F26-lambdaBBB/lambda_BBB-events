package com.example.lambda_bbb_events.view.ui.components.campsite

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.CampsitePhotoController
import com.example.lambda_bbb_events.controller.CampsitePhotoController.Companion.MAX_PHOTOS
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.model.services.decodePhoto
import com.example.lambda_bbb_events.view.ui.components.common.StoredImage
import com.example.lambda_bbb_events.view.ui.components.profile.ShowAlertDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/*
 * Lets owners change the photos of their campsites.
 */

/** Longest side in pixels photos are shrunk to before uploading. They are stored no larger. */
private const val MAX_PHOTO_SIZE = 2560

/**
 * Lets the owner add and remove a campsite's photos, and choose its thumbnail, in a sheet over
 * the screen. Changes are saved straight away.
 *
 * Keep this on the screen while the sheet is closed too, so photos being added finish uploading.
 *
 * @param isOpen Whether the sheet is shown.
 * @param onClose Called to close the sheet.
 * @param photoController Changes the photos.
 * @param campsiteId The ID of the campsite.
 * @param uploaderId The ID of the user changing the photos.
 * @param photos The campsite's photos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoManager(
    isOpen: Boolean,
    onClose: () -> Unit,
    photoController: CampsitePhotoController,
    campsiteId: String,
    uploaderId: String,
    photos: List<ImageMetaData>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf<String?>(null) } // null when nothing is being saved
    var error by remember { mutableStateOf<String?>(null) }
    var removing by remember { mutableStateOf<ImageMetaData?>(null) }

    /** Makes a change, showing [failure] if it could not be saved. */
    fun change(description: String, failure: String, action: suspend () -> Unit) {
        progress = description
        error = null
        scope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                error = failure
            } finally {
                progress = null
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_PHOTOS)
    ) { picked ->
        val uris = picked.take((MAX_PHOTOS - photos.size).coerceAtLeast(0))
        if (picked.isNotEmpty()) {
            progress = "Adding photos..."
            error = null
            scope.launch {
                var failed = 0
                try {
                    uris.forEachIndexed { index, uri ->
                        progress = "Adding photo ${index + 1} of ${uris.size}..."
                        try {
                            val photo = decodePhoto(context, uri, maxSize = MAX_PHOTO_SIZE)
                            photoController.addPhoto(campsiteId, uploaderId, photo)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            failed++ // the other photos can still be added
                        }
                    }
                    error = when {
                        failed > 0 -> "Could not add $failed of ${uris.size} photos. Please try again."
                        picked.size > uris.size -> "A campsite can have up to $MAX_PHOTOS photos, so some were not added."
                        else -> null
                    }
                } finally {
                    progress = null
                }
            }
        }
    }

    if (isOpen) {
        // Opens straight to fully expanded, with no half-open state
        val sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )
        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("photo_manager"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Manage photos",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${photos.size} of $MAX_PHOTOS photos. Changes are saved right away.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                photos.forEachIndexed { index, photo ->
                    PhotoRow(
                        photo = photo,
                        index = index,
                        enabled = progress == null,
                        onMakeThumbnail = {
                            change("Saving...", "Could not change the thumbnail. Please try again.") {
                                photoController.makeThumbnail(campsiteId, photo.imageId)
                            }
                        },
                        onRemove = { removing = photo }
                    )
                }

                progress?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = progress == null && photos.size < MAX_PHOTOS,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("photo_manager_add"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (photos.size < MAX_PHOTOS) "Add photos" else "Photo limit reached",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    removing?.let { photo ->
        ShowAlertDialog(
            dialogLabel = "Remove photo?",
            dialogCaption = "This photo will be deleted. This action cannot be undone.",
            confirmButton = {
                removing = null
                change("Removing photo...", "Could not remove the photo. Please try again.") {
                    photoController.removePhoto(campsiteId, photo.imageId)
                }
            },
            confirmText = "Remove",
            dismissState = { removing = null },
            cancelText = "Cancel"
        )
    }
}

/** Displays one photo with the actions for it. */
@Composable
private fun PhotoRow(
    photo: ImageMetaData,
    index: Int,
    enabled: Boolean,
    onMakeThumbnail: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("photo_manager_photo_$index"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StoredImage(
            imageId = photo.imageId,
            contentDescription = "Photo ${index + 1}",
            focusX = photo.focusX,
            focusY = photo.focusY,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Box(modifier = Modifier.weight(1f)) {
            if (index == 0) {
                Text(
                    text = "Thumbnail",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            } else {
                TextButton(
                    onClick = onMakeThumbnail,
                    enabled = enabled,
                    modifier = Modifier.testTag("photo_manager_thumbnail_$index")
                ) {
                    Text("Make thumbnail")
                }
            }
        }
        IconButton(
            onClick = onRemove,
            enabled = enabled,
            modifier = Modifier.testTag("photo_manager_remove_$index")
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Remove photo ${index + 1}",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

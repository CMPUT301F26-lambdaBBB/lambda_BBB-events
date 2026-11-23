
package com.example.lambda_bbb_events.view.ui.owner_screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.view.ui.components.campsite.TagEditor
import com.example.lambda_bbb_events.view.ui.components.common.CampsiteImageCarousel
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldMultiLine
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.PhotoFocusDialog
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.common.StoredImage
import com.example.lambda_bbb_events.view.ui.components.common.loadStoredImage
import com.example.lambda_bbb_events.view.ui.components.navigation.rememberBackAction
import com.example.lambda_bbb_events.view.ui.user_screens.create.MAX_OCCUPANCY_DIGITS

/*
 * Provides a screen for editing campsite details.
 * Allows owners to save changes or cancel editing.
 *
 * TODO: Implement campsite photo viewing and management.
 */

/**
 * Allows users to edit campsite details and tags.
 *
 * Saves or discards changes and displays errors when needed.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves and updates campsite data.
 * @param campsiteId The ID of the campsite being edited.
 */
@Composable
fun EditCampsiteScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val goBack = rememberBackAction(navController)

    val campsite = controllers.campsites.getCampsite(campsiteId)

    if (campsite == null) {
        Text(
            text = "Campsite not found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    var name by remember(campsiteId) {
        mutableStateOf(campsite.campsiteName)
    }

    var address by remember(campsiteId) {
        mutableStateOf(campsite.address)
    }

    var maxOccupancy by remember(campsiteId) {
        mutableStateOf(campsite.maxOccupancy.toString())
    }

    var description by remember(campsiteId) {
        mutableStateOf(campsite.description)
    }

    var rulesPolicies by remember(campsiteId) {
        mutableStateOf(campsite.rulesPolicies)
    }

    val tags = remember(campsiteId) {
        campsite.tags.toMutableStateList()
    }

    // The photos, kept here so a changed focus point shows straight away
    var photos by remember(campsiteId) {
        mutableStateOf(campsite.imageIds)
    }

    var showError by remember(campsiteId) {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTitle(
            title = "Edit campsite",
            navController = navController,
            showBack = true
        )

        // Scrollable campsite editing form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormTextFieldWithTitle(
                title = "Campsite name",
                value = name,
                onValueChange = {
                    name = it
                },
                placeholder = "Campsite name"
            )

            // Photos
            CampsiteImageCarousel(
                campsite = campsite.copy(imageIds = photos),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            if (photos.isNotEmpty()) {
                PhotoFocusRow(
                    photos = photos,
                    onFocusChange = { changed ->
                        val updated = photos.map { photo ->
                            if (photo.imageId == changed.imageId) changed else photo
                        }
                        photos = updated
                        controllers.campsites.updateCampsite(campsiteId, campsite.copy(imageIds = updated))
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    // TODO: Open the photo manager.
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Manage photos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Address
            Text(
                text = "Address",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            FormTextFieldMultiLine(
                value = address,
                onValueChange = {
                    address = it
                },
                placeholder = "Address"
            )

            // Max occupancy
            FormTextFieldWithTitle(
                title = "Max Occupancy",
                value = maxOccupancy,
                onValueChange = { input ->
                    maxOccupancy = input.filter { it.isDigit() }.take(MAX_OCCUPANCY_DIGITS)
                },
                placeholder = "Number of people",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.testTag("edit_max_occupancy")
            )

            // Description
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            FormTextFieldMultiLine(
                value = description,
                onValueChange = {
                    description = it
                },
                placeholder = "Describe the campsite"
            )

            // Rules and policies
            FormTextFieldWithTitle(
                title = "Rules and policies",
                value = rulesPolicies,
                onValueChange = {
                    rulesPolicies = it
                },
                placeholder = "Rules entrants must follow"
            )

            // Tags
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            TagEditor(
                tags = tags,
                onAddTag = {
                    tags.add(it)
                },
                onRemoveTag = {
                    tags.remove(it)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Validation error
        if (showError) {
            Text(
                text = "Please enter a name, an address, and a max occupancy of at least 1.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        // Fixed bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    goBack()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    val saved = controllers.campsites.updateCampsite(
                        campsiteId = campsiteId,
                        name = name,
                        address = address,
                        maxOccupancyText = maxOccupancy,
                        description = description,
                        rulesPolicies = rulesPolicies,
                        tags = tags
                    )

                    if (saved) {
                        goBack()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save changes",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Displays a campsite's photos so the owner can tap one and choose what stays in view when
 * it is cropped on cards and thumbnails.
 *
 * @param photos The campsite's photos.
 * @param onFocusChange Called with a photo whose focus point was changed.
 */
@Composable
private fun PhotoFocusRow(
    photos: List<ImageMetaData>,
    onFocusChange: (ImageMetaData) -> Unit
) {
    // The photo being changed, and that photo loaded for the dialog
    var editing by remember { mutableStateOf<ImageMetaData?>(null) }
    var loadedPhoto by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(editing) {
        loadedPhoto = null
        val photo = editing
        if (photo != null) {
            loadedPhoto = loadStoredImage(photo.imageId, maxSize = 1080)
            if (loadedPhoto == null) {
                // The photo could not be loaded, so there is nothing to choose a focus on
                editing = null
            }
        }
    }

    Text(
        text = "Tap a photo to choose what stays in view on cards.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(photos) { index, photo ->
            StoredImage(
                imageId = photo.imageId,
                contentDescription = "Photo ${index + 1}",
                focusX = photo.focusX,
                focusY = photo.focusY,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { editing = photo }
                    .testTag("edit_photo_focus_$index")
            )
        }
    }

    val photo = editing
    val bitmap = loadedPhoto
    if (photo != null && bitmap != null) {
        PhotoFocusDialog(
            photo = bitmap,
            initialFocusX = photo.focusX,
            initialFocusY = photo.focusY,
            onDismiss = { editing = null },
            onSave = { x, y ->
                onFocusChange(photo.copy(focusX = x, focusY = y))
                editing = null
            }
        )
    }
}

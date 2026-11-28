package com.example.lambda_bbb_events.view.ui.admin_screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.model.user.UserRole
import com.example.lambda_bbb_events.ui.theme.TypeTabBar
import com.example.lambda_bbb_events.view.ui.components.admin.NoAccessMessage
import com.example.lambda_bbb_events.view.ui.components.common.ImageFraming
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.common.SearchBar
import com.example.lambda_bbb_events.view.ui.components.common.StoredImage
import com.example.lambda_bbb_events.view.ui.components.profile.ShowAlertDialog
import kotlinx.coroutines.launch

/**
 * The image data for an image in the gallery.
 *
 * @property imageId The ID of the image.
 * @property label The label under the image.
 * @property isUserPicture `true` if profile picture, `false` if campsite photo.
 * @property focusX Offset of the image in the X direction (left/right).
 * @property focusY Offset of the image in the Y direction (up/down).
 */
private data class GalleryImage(
    val imageId: String,
    val label: String,
    val isUserPicture: Boolean,
    val campsiteId: String = "",
    val userId: String = "",
    val focusX: Float = 0.5f,
    val focusY: Float = 0.5f
)

/** Displays campsite images for administrator review and moderation. */
@Composable
fun AdminImagesScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val isAdmin = controllers.users.currentUser?.role == UserRole.ADMIN

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val tabNames = listOf("All", "Campsites", "Users")
    var selectedTab by remember { mutableStateOf(0) }

    var showDialog by remember { mutableStateOf(false) }
    var selectedGalleryImage: GalleryImage? by remember { mutableStateOf(null) }
    var removeError by remember { mutableStateOf(false) }
    var reloadCount by remember { mutableStateOf(0) } // used to refresh manually

    val scope = rememberCoroutineScope()

    LaunchedEffect(isAdmin, reloadCount) {
        isLoading = true
        if (isAdmin) {
            users = try {
                controllers.users.getAllUsers()
            } catch (_: Exception) { // if fails just show nothing
                emptyList()
            }
        }
        isLoading = false
    }

    val allImages = mutableListOf<GalleryImage>()

    val campsites = controllers.campsites.getAllCampsites().sortedBy { it.campsiteName.lowercase() }
    for (campsite in campsites) {
        campsite.imageIds.forEachIndexed { index, image ->
            allImages.add(
                GalleryImage(
                    imageId = image.imageId,
                    label = "${campsite.campsiteName} #${index + 1}",
                    isUserPicture = false,
                    campsiteId = campsite.campsiteId,
                    focusX = image.focusX,
                    focusY = image.focusY
                )
            )
        }
    }

    val sortedUsers = users.sortedBy { it.name.lowercase() }
    for (user in sortedUsers) {
        if (user.profilePictureImageId.isNotBlank()) {
            allImages.add(
                GalleryImage(
                    imageId = user.profilePictureImageId,
                    label = user.name,
                    isUserPicture = true,
                    userId = user.deviceId
                )
            )
        }
    }

    val shownImages = mutableListOf<GalleryImage>()
    for (image in allImages) {
        var matchesTab = true
        if (selectedTab == 1 && image.isUserPicture) matchesTab = false // Campsites tab
        if (selectedTab == 2 && !image.isUserPicture) matchesTab = false // Users tab

        val matchesSearch = query.isBlank() || image.label.contains(query, ignoreCase = true)

        if (matchesTab && matchesSearch) {
            shownImages.add(image)
        }
    }

    Scaffold(
        modifier = Modifier.testTag("screen_admin_images"),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
        // TODO: add the admin bottom navigation bar here
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            ScreenTitle(
                title = "Images",
                navController = navController,
                showBack = true
            )

            if (!isAdmin) {
                NoAccessMessage(navController)
            } else if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SearchBar(
                    label = "Search Images",
                    placeholder = "Search images...",
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adminImageSearch")
                )

                Spacer(modifier = Modifier.height(8.dp))

                TypeTabBar(
                    tabs = tabNames,
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Images",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = shownImages.size.toString() + " items",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Click an image to delete it.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (shownImages.isEmpty()) {
                    Text(
                        text = "No images found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    // A grid with 2 pictures per row
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(shownImages) { image ->
                            AdminImageCard(
                                image,
                                onClick = {
                                    selectedGalleryImage = image
                                    removeError = false
                                    showDialog = true
                                }
                            )
                        }
                    }
                }

                if (showDialog) {
                    ShowAlertDialog(
                        dialogLabel = "Remove Image?",
                        dialogCaption = "Are you sure you want to delete? This image will be removed.",
                        confirmButton = {
                            showDialog = false
                            scope.launch {
                                try {
                                    val image = selectedGalleryImage
                                    if (image != null) {
                                        // take the image off its campsite or user first before deleting in db
                                        if (image.isUserPicture) {
                                            controllers.users.removeProfilePicture(image.userId)
                                        } else {
                                            val campsite = controllers.campsites.getCampsite(image.campsiteId)
                                            if (campsite != null) {
                                                val remaining = campsite.imageIds.filter { it.imageId != image.imageId }
                                                controllers.campsites.updateCampsite(
                                                    image.campsiteId,
                                                    campsite.copy(imageIds = remaining)
                                                )
                                            }
                                        }

                                        if (controllers.images.deleteImage(image.imageId)) {
                                            reloadCount++
                                        } else {
                                            removeError = true
                                        }
                                    }
                                } catch (_: Exception) { // catches error and shows error to user
                                    removeError = true
                                }
                            }
                        },
                        confirmText = "Remove",
                        dismissState = { showDialog = false },
                        cancelText = "Cancel",
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

/**
 * Shows one picture with its label underneath.
 *
 * @param image The picture to show.
 */
@Composable
private fun AdminImageCard(
    image: GalleryImage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{ onClick() }
            .testTag("adminImageItem"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        StoredImage(
            imageId = image.imageId,
            contentDescription = image.label,
            focusX = image.focusX,
            focusY = image.focusY,
            framing = ImageFraming.CROP,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
        )

        Text(
            text = image.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(14.dp)
        )
    }
}

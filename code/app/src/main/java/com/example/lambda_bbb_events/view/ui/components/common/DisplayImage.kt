package com.example.lambda_bbb_events.view.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.services.ImageProcessor
import kotlinx.coroutines.launch

/*
 * Provides reusable composables for displaying images stored in the database.
 */

/** Loads every image shown on screen, shared so each loaded image is cached once for the whole app. */
private val sharedImageProcessor = ImageProcessor()

/**
 * Displays an image stored in the database, filling the space given by [modifier].
 *
 * The image is decoded at the size it is drawn at, and a themed loading indicator or
 * error icon fills the same space until it is ready. [modifier] should give a bounded size.
 *
 * @param imageId The ID of the image to display.
 * @param modifier Sets the size and shape of the image.
 * @param contentDescription Description for accessibility.
 * @param contentScale How the image is scaled into its bounds. Crops to fill by default.
 */
@Composable
fun StoredImage(
    imageId: String,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    contentScale: ContentScale = ContentScale.Crop
) {
    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Load the image at the size it is drawn at, or full size if that size is unlimited
        var targetWidth: Int? = null
        if (constraints.hasBoundedWidth) {
            targetWidth = constraints.maxWidth
        }
        var targetHeight: Int? = null
        if (constraints.hasBoundedHeight) {
            targetHeight = constraints.maxHeight
        }

        // Start with the image straight away if it was already loaded at this size
        var bitmap by remember(imageId, targetWidth, targetHeight) {
            mutableStateOf(sharedImageProcessor.getCachedBitmap(imageId, targetWidth, targetHeight))
        }
        var failed by remember(imageId, targetWidth, targetHeight) {
            mutableStateOf(false)
        }

        LaunchedEffect(imageId, targetWidth, targetHeight) {
            if (bitmap == null) {
                bitmap = sharedImageProcessor.getBitmap(imageId, targetWidth, targetHeight)
                failed = bitmap == null
            }
        }

        val loadedBitmap = bitmap
        if (loadedBitmap != null) {
            Image(
                bitmap = loadedBitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else if (failed) {
            Icon(
                imageVector = Icons.Outlined.BrokenImage,
                contentDescription = "Unable to load image",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

/**
 * Displays the placeholder shown when a campsite has no images.
 *
 * @param modifier Sets the size and shape of the placeholder.
 * @param text Optional text shown below the icon.
 */
@Composable
fun NoImagePlaceholder(
    modifier: Modifier = Modifier,
    text: String? = null
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (text == null) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "No image",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Displays a campsite's first image, or a placeholder if it has none.
 *
 * @param campsite The campsite to show an image of.
 * @param modifier Sets the size and shape of the image.
 */
@Composable
fun CampsiteThumbnail(
    campsite: Campsite?,
    modifier: Modifier = Modifier
) {
    val imageId = campsite?.imageIds?.firstOrNull()?.imageId

    if (imageId == null) {
        NoImagePlaceholder(modifier = modifier)
    } else {
        StoredImage(
            imageId = imageId,
            contentDescription = campsite.campsiteName,
            modifier = modifier
        )
    }
}

/**
 * Displays a carousel of a campsite's images, starting with its thumbnail (the first image).
 *
 * When there is more than one image, Previous and Next buttons and a page indicator are
 * shown, and the images can also be swiped. Shows a placeholder if the campsite has no images.
 *
 * @param campsite The campsite whose images are displayed.
 * @param modifier Sets the size and shape of the carousel.
 */
@Composable
fun CampsiteImageCarousel(
    campsite: Campsite,
    modifier: Modifier = Modifier
) {
    val images = campsite.imageIds

    if (images.isEmpty()) {
        NoImagePlaceholder(modifier = modifier, text = "No images available")
        return
    }

    val pagerState = rememberPagerState { images.size }
    val scope = rememberCoroutineScope()
    val isFirstImage = pagerState.currentPage == 0
    val isLastImage = pagerState.currentPage == images.size - 1

    Box(modifier = modifier.testTag("campsite_image_carousel")) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            StoredImage(
                imageId = images[page].imageId,
                contentDescription = "${campsite.campsiteName} image ${page + 1} of ${images.size}",
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("carousel_image_${page + 1}")
            )
        }

        if (images.size > 1) {
            if (!isFirstImage) {
                CarouselButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous image",
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .testTag("carousel_previous")
                )
            }
            if (!isLastImage) {
                CarouselButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next image",
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .testTag("carousel_next")
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
            }
        }
    }
}

/**
 * Displays a round button over the carousel images for moving between them.
 *
 * @param icon The arrow shown on the button.
 * @param contentDescription Description for accessibility.
 * @param onClick Called when the button is tapped.
 * @param modifier Sets the position of the button.
 */
@Composable
private fun CarouselButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

/**
 * Displays a user's profile picture as a circle, or a default icon if they have none.
 *
 * @param imageId The ID of the user's profile picture, blank if they have none.
 * @param size The diameter of the picture.
 * @param modifier Customizes the picture's layout.
 */
@Composable
fun ProfilePicture(
    imageId: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    if (imageId.isBlank()) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile picture",
            modifier = modifier.size(size),
            tint = MaterialTheme.colorScheme.primary
        )
    } else {
        StoredImage(
            imageId = imageId,
            contentDescription = "Profile picture",
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    }
}

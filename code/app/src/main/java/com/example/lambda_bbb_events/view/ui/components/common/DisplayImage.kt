package com.example.lambda_bbb_events.view.ui.components.common

import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.ImageProcessor

/*
 * Provides reusable composables for displaying images stored in the database.
 */

/**
 * The [ImageProcessor] used to load images, shared so loaded images are cached app-wide.
 */
val LocalImageProcessor = staticCompositionLocalOf { ImageProcessor() }

private sealed interface ImageLoadState {
    data object Loading : ImageLoadState
    data class Loaded(val bitmap: Bitmap) : ImageLoadState
    data object Failed : ImageLoadState
}

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
    val imageProcessor = LocalImageProcessor.current

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val targetWidth = constraints.maxWidth.takeIf { constraints.hasBoundedWidth }
        val targetHeight = constraints.maxHeight.takeIf { constraints.hasBoundedHeight }

        val state by produceState<ImageLoadState>(
            initialValue = imageProcessor.getCachedBitmap(imageId, targetWidth, targetHeight)
                ?.let { ImageLoadState.Loaded(it) } ?: ImageLoadState.Loading,
            imageId, targetWidth, targetHeight
        ) {
            if (value !is ImageLoadState.Loaded) {
                value = imageProcessor.getBitmap(imageId, targetWidth, targetHeight)
                    ?.let { ImageLoadState.Loaded(it) } ?: ImageLoadState.Failed
            }
        }

        when (val current = state) {
            ImageLoadState.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )

            is ImageLoadState.Loaded -> Image(
                bitmap = current.bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )

            ImageLoadState.Failed -> Icon(
                imageVector = Icons.Outlined.BrokenImage,
                contentDescription = "Unable to load image",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
 * Displays a swipeable carousel of a campsite's images, with a page indicator when there
 * is more than one. Shows a placeholder if the campsite has no images.
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

    Box(modifier = modifier.testTag("campsite_image_carousel")) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            StoredImage(
                imageId = images[page].imageId,
                contentDescription = "${campsite.campsiteName} image ${page + 1} of ${images.size}",
                modifier = Modifier.fillMaxSize()
            )
        }

        if (images.size > 1) {
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

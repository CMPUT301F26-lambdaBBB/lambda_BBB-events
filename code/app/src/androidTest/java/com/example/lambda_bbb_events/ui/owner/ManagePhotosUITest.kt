package com.example.lambda_bbb_events.ui.owner

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.core.app.ActivityOptionsCompat
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.view.ui.owner_screens.EditCampsiteScreen
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Tests changing a campsite's photos from the edit campsite screen. Photos are stored by a mock,
 * and Android's photo picker is replaced, so nothing is uploaded.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class ManagePhotosUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val controllers = mockk<AppControllers>(relaxed = true)
    private val campsites = controllers.campsites
    private val images = controllers.images
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private var photos by mutableStateOf(emptyList<ImageMetaData>())
    private val ids get() = photos.map { it.imageId }

    /** The photos that the photo picker returns. */
    private var picked = emptyList<Uri>()

    /** Stands in for Android's photo picker, which is a separate app that tests cannot control. */
    private val pickerOwner = object : ActivityResultRegistryOwner {
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, picked)
            }
        }
    }

    private fun photoFile(name: String): Uri {
        val file = File(context.cacheDir, "$name.jpg")
        file.outputStream().use {
            Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return Uri.fromFile(file)
    }

    private fun openManager(vararg imageIds: String) {
        photos = imageIds.map { ImageMetaData(imageId = it) }
        every { campsites.getCampsite("site-1") } answers {
            Campsite(campsiteId = "site-1", ownerId = "owner-1", campsiteName = "Lakeside", imageIds = photos)
        }
        coEvery { campsites.setPhotos("site-1", any()) } answers { photos = secondArg() }
        coEvery { images.deleteImage(any()) } returns true
        coEvery { images.uploadImage(any(), any(), any(), any(), any()) } answers {
            ImageMetaData(imageId = firstArg())
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides pickerOwner) {
                EditCampsiteScreen(rememberNavController(), controllers, "site-1")
            }
        }
        composeTestRule.onNodeWithTag("edit_manage_photos").performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    private fun addPhotos(vararg uris: Uri) {
        picked = uris.toList()
        composeTestRule.onNodeWithTag("photo_manager_add").performScrollTo().performClick()
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun manageButtonShowsThePhotos() {
        openManager("a", "b")

        composeTestRule.onNodeWithTag("photo_manager").assertIsDisplayed()
        composeTestRule.onNodeWithTag("photo_manager_photo_1").assertExists()
        composeTestRule.onNodeWithText("2 of 5 photos. Changes are saved right away.").assertExists()
    }

    @Test
    fun photosCanBeAddedToACampsiteWithoutAny() {
        openManager()
        addPhotos(photoFile("one"), photoFile("two"))

        composeTestRule.waitUntil(10_000) { photos.size == 2 }
        composeTestRule.onNodeWithText("Thumbnail").assertExists()
        coVerify(exactly = 2) { images.uploadImage(any(), any(), any(), any(), any()) }
    }

    @Test
    fun onlyPhotosUpToTheLimitAreAdded() {
        openManager("a", "b", "c", "d")
        addPhotos(photoFile("one"), photoFile("two"))

        composeTestRule.waitUntilAtLeastOneExists(hasText("some were not added", substring = true), 10_000)
        assertEquals(5, photos.size)
        composeTestRule.onNodeWithTag("photo_manager_add").assertIsNotEnabled()
    }

    @Test
    fun photosThatCannotBeAddedAreExplained() {
        openManager("a")
        addPhotos(Uri.fromFile(File(context.cacheDir, "missing.jpg")), photoFile("one"))

        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Could not add 1 of 2 photos. Please try again."), 10_000
        )
        assertEquals(2, photos.size)
    }

    @Test
    fun removingAPhotoAsksFirst() {
        openManager("a", "b")
        composeTestRule.onNodeWithTag("photo_manager_remove_0").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Remove photo?").assertIsDisplayed()
        assertEquals(listOf("a", "b"), ids)

        composeTestRule.onNodeWithText("Remove").performClick()
        composeTestRule.waitUntil(5_000) { ids == listOf("b") }
        coVerify(exactly = 1) { images.deleteImage("a") }
    }

    @Test
    fun photoShownInTheCarouselCanBeRemoved() {
        openManager("a", "b")
        composeTestRule.onNodeWithTag("carousel_next").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("photo_manager_remove_1").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Remove").performClick()

        composeTestRule.waitUntil(5_000) { ids == listOf("a") }
        composeTestRule.onNodeWithTag("carousel_image_1").assertExists()
    }

    @Test
    fun anotherPhotoCanBeMadeTheThumbnail() {
        openManager("a", "b", "c")
        composeTestRule.onNodeWithTag("photo_manager_thumbnail_2").performScrollTo().performClick()

        composeTestRule.waitUntil(5_000) { ids == listOf("c", "a", "b") }
    }

    @Test
    fun failedChangesAreExplained() {
        openManager("a", "b")
        coEvery { campsites.setPhotos(any(), any()) } throws IllegalStateException("offline")
        composeTestRule.onNodeWithTag("photo_manager_thumbnail_1").performScrollTo().performClick()

        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Could not change the thumbnail. Please try again."), 5_000
        )
        assertEquals(listOf("a", "b"), ids)
    }

    @Test
    fun unsavedDetailsAreKeptWhenPhotosChange() {
        openManager("a", "b")
        composeTestRule.onNodeWithText("Lakeside").performTextReplacement("New name")
        composeTestRule.onNodeWithTag("photo_manager_thumbnail_1").performScrollTo().performClick()
        composeTestRule.waitUntil(5_000) { ids == listOf("b", "a") }

        composeTestRule.onNodeWithText("New name").assertExists()
    }
}

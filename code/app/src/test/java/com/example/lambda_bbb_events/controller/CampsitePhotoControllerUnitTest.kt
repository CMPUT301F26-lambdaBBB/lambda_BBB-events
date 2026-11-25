package com.example.lambda_bbb_events.controller

import android.graphics.Bitmap
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.model.services.ImageProcessor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests changing the photos of campsites.
 */
class CampsitePhotoControllerUnitTest {
    private val campsites = mockk<CampsiteController>()
    private val images = mockk<ImageProcessor>()
    private val controller = CampsitePhotoController(campsites, images)
    private val bitmap = mockk<Bitmap>()

    private var photos = listOf("a", "b", "c").map { ImageMetaData(imageId = it) }
    private val ids get() = photos.map { it.imageId }

    @Before
    fun setup() {
        every { campsites.getCampsite("site-1") } answers { Campsite(campsiteId = "site-1", imageIds = photos) }
        coEvery { campsites.setPhotos("site-1", any()) } answers { photos = secondArg() }
        coEvery { images.deleteImage(any()) } returns true
        coEvery { images.uploadImage(any(), any(), any(), any(), any()) } answers {
            ImageMetaData(imageId = firstArg(), uploaderUserId = secondArg())
        }
    }

    private fun failSaving() {
        coEvery { campsites.setPhotos(any(), any()) } throws IllegalStateException("offline")
    }

    private suspend fun errorFrom(action: suspend () -> Unit) = runCatching { action() }.exceptionOrNull()

    @Test
    fun addedPhotoGoesAfterTheOthers() = runTest {
        controller.addPhoto("site-1", "owner-1", bitmap)

        assertEquals(listOf("a", "b", "c"), ids.take(3))
        assertEquals("owner-1", photos[3].uploaderUserId)
        coVerify(exactly = 0) { images.deleteImage(any()) }
    }

    @Test
    fun photosCannotGoOverTheLimit() = runTest {
        photos = (1..CampsitePhotoController.MAX_PHOTOS).map { ImageMetaData(imageId = "$it") }

        assertTrue(errorFrom { controller.addPhoto("site-1", "owner-1", bitmap) } is IllegalStateException)
        coVerify(exactly = 0) { images.uploadImage(any(), any(), any(), any(), any()) }
    }

    @Test
    fun failedUploadIsCleanedUp() = runTest {
        coEvery { images.uploadImage(any(), any(), any(), any(), any()) } throws IllegalStateException("offline")

        assertNotNull(errorFrom { controller.addPhoto("site-1", "owner-1", bitmap) })
        assertEquals(listOf("a", "b", "c"), ids)
        coVerify(exactly = 1) { images.deleteImage(any()) }
    }

    @Test
    fun uploadedPhotoIsDeletedIfSavingFails() = runTest {
        failSaving()

        assertNotNull(errorFrom { controller.addPhoto("site-1", "owner-1", bitmap) })
        coVerify(exactly = 1) { images.deleteImage(match { it !in ids }) }
    }

    @Test
    fun removedPhotoIsDeletedAfterSaving() = runTest {
        controller.removePhoto("site-1", "b")

        assertEquals(listOf("a", "c"), ids)
        coVerifyOrder {
            campsites.setPhotos("site-1", any())
            images.deleteImage("b")
        }
    }

    @Test
    fun photoIsKeptIfRemovingItCannotBeSaved() = runTest {
        failSaving()

        assertNotNull(errorFrom { controller.removePhoto("site-1", "b") })
        coVerify(exactly = 0) { images.deleteImage(any()) }
    }

    @Test
    fun lastPhotoCanBeRemoved() = runTest {
        photos = listOf(ImageMetaData(imageId = "a"))

        controller.removePhoto("site-1", "a")

        assertTrue(photos.isEmpty())
    }

    @Test
    fun thumbnailMovesToTheFrontAndOthersKeepTheirOrder() = runTest {
        controller.makeThumbnail("site-1", "c")

        assertEquals(listOf("c", "a", "b"), ids)
    }

    @Test
    fun focusChangesOnlyThatPhoto() = runTest {
        controller.changeFocus("site-1", "b", 0.1f, 0.9f)

        assertEquals(0.1f, photos[1].focusX)
        assertEquals(0.9f, photos[1].focusY)
        assertEquals(0.5f, photos[0].focusX)
        assertEquals(listOf("a", "b", "c"), ids)
    }
}

package com.example.lambda_bbb_events.store

import androidx.compose.runtime.mutableStateOf
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CampsiteStoreUnitTest {
    private val repo = mockk<CampsiteRepository>()
    private val lotteries = mockk<LotteryRepository>()
    private val scope = TestScope()
    private lateinit var store: CampsiteStore

    /** Reports the campsites in Firestore to the store, like the repository's listener. */
    private val onChange = slot<(List<Campsite>) -> Unit>()

    private val existing = Campsite(campsiteId = "site-1", campsiteName = "Lakeside")

    @Before
    fun setup() {
        coEvery { repo.getAllCampsites() } returns mutableMapOf("site-1" to existing)
        every { repo.startListening(capture(onChange)) } just runs
        coEvery { repo.addCampsite(any()) } just runs
        coEvery { repo.updateCampsite(any(), any()) } just runs
        coEvery { repo.deleteCampsiteWithLotteries(any(), any()) } just runs
        store = CampsiteStore(repo, scope, lotteries)
    }

    @Test
    fun initLoadsCampsitesFromRepository() = scope.runTest {
        advanceUntilIdle()

        assertEquals(mapOf("site-1" to existing), store.campsites)
    }

    @Test
    fun addCampsiteCachesAndPersists() = scope.runTest {
        assertTrue(store.addCampsite(campsiteId = "site-2", campsiteName = "Forest", returnMessage = mutableStateOf("")))
        advanceUntilIdle()

        assertEquals("Forest", store.campsites.getValue("site-2").campsiteName)
        coVerify(exactly = 1) { repo.addCampsite(match { it.campsiteId == "site-2" }) }
    }

    @Test
    fun updateCampsitePersistsChange() = scope.runTest {
        advanceUntilIdle()
        val renamed = existing.copy(campsiteName = "Lakeview")

        assertTrue(store.updateCampsite("site-1", renamed, mutableStateOf("")))
        advanceUntilIdle()

        assertEquals("Lakeview", store.campsites.getValue("site-1").campsiteName)
        coVerify(exactly = 1) { repo.updateCampsite("site-1", renamed) }
    }

    @Test
    fun removeCampsiteRemovesAndPersists() = scope.runTest {
        advanceUntilIdle()
        val message = mutableStateOf("")

        assertTrue(store.removeCampsite("site-1", message))
        advanceUntilIdle()

        assertTrue(store.campsites.isEmpty())
        assertEquals("", message.value)
        coVerify(exactly = 1) { repo.deleteCampsiteWithLotteries("site-1", lotteries) }
    }

    @Test
    fun setPhotosReplacesTheCachedPhotos() = scope.runTest {
        advanceUntilIdle()
        val photos = listOf(ImageMetaData(imageId = "img-1"))
        coEvery { repo.setPhotos(any(), any()) } just runs

        store.setPhotos("site-1", photos)

        assertEquals(photos, store.campsites.getValue("site-1").imageIds)
        coVerify(exactly = 1) { repo.setPhotos("site-1", photos) }
    }

    @Test
    fun photosAreNotChangedIfSavingFails() = scope.runTest {
        advanceUntilIdle()
        coEvery { repo.setPhotos(any(), any()) } throws IllegalStateException("offline")

        runCatching { store.setPhotos("site-1", listOf(ImageMetaData(imageId = "img-1"))) }

        assertTrue(store.campsites.getValue("site-1").imageIds.isEmpty())
    }

    @Test
    fun testPlaceholder() {
        assertTrue(true)
    }
}

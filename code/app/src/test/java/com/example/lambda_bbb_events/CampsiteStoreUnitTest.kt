package com.example.lambda_bbb_events

import androidx.compose.runtime.mutableStateOf
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.store.CampsiteStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CampsiteStoreUnitTest {
    private val repo = mockk<CampsiteRepository>()
    private val scope = TestScope()
    private lateinit var store: CampsiteStore

    private val existing = Campsite(campsiteId = "site-1", campsiteName = "Lakeside")

    @Before
    fun setup() {
        coEvery { repo.getAllCampsites() } returns mutableMapOf("site-1" to existing)
        coEvery { repo.addCampsite(any()) } just runs
        coEvery { repo.updateCampsite(any(), any()) } just runs
        coEvery { repo.deleteCampsite(any()) } just runs
        store = CampsiteStore(repo, scope)
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

        assertTrue(store.removeCampsite(existing, message))
        advanceUntilIdle()

        assertTrue(store.campsites.isEmpty())
        assertEquals("Success", message.value)
        coVerify(exactly = 1) { repo.deleteCampsite("site-1") }
    }
}

package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.campsite.AvailabilitySchedule
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CampsiteRepositoryUnitTest {
    private val db = mockk<FirebaseFirestore>()
    private lateinit var campsites: MockCollection
    private lateinit var repository: CampsiteRepository

    private val campsite = Campsite(
        campsiteId = "site-1",
        ownerId = "owner-1",
        campsiteName = "Lakeside",
        description = "By the lake",
        address = "1 Lake Rd",
        latitude = 53.5,
        longitude = -113.5,
        maxOccupancy = 6,
        rulesPolicies = "No fires",
        amenities = listOf("Water"),
        tags = listOf("lake"),
        imageIds = listOf(ImageMetaData(imageId = "img-1", chunkCount = 2, uploadTimestamp = 1L)),
        rating = 4.5f,
        ratingCount = 2,
        availability = AvailabilitySchedule(
            mutableMapOf(LocalDate.of(2026, 7, 1) to 0, LocalDate.of(2026, 7, 2) to 3)
        ),
        active = false
    )

    @Before
    fun setup() {
        campsites = MockCollection(db, "campsites")
        repository = CampsiteRepository(db)
    }

    @Test
    fun toFirestoreStoresDatesAsIsoStrings() {
        val firestoreCampsite = campsite.toFirestore()

        assertEquals(
            mutableMapOf("2026-07-01" to 0, "2026-07-02" to 3),
            firestoreCampsite.availability.dates
        )
    }

    @Test
    fun conversionRoundTripPreservesEveryField() {
        val restored = campsite.toFirestore().toCampsite()

        // Campsite.equals only compares a few fields, so compare the data copies field by field
        assertEquals(campsite.toFirestore(), restored.toFirestore())
        assertEquals(campsite.availability, restored.availability)
    }

    @Test
    fun toCampsiteUsesGivenId() {
        assertEquals("doc-id", campsite.toFirestore().toCampsite("doc-id").campsiteId)
    }

    @Test
    fun addCampsiteWritesFirestoreCampsiteAtCampsiteId() = runTest {
        val written = slot<Any>()
        io.mockk.every { campsites.document("site-1").set(capture(written)) } returns completedTask(null)

        repository.addCampsite(campsite)

        assertEquals(campsite.toFirestore(), written.captured)
        assertEquals(listOf(campsite), repository.cached)
    }

    @Test
    fun updateCampsiteStoresUnderGivenId() = runTest {
        val written = slot<Any>()
        io.mockk.every { campsites.document("site-2").set(capture(written), any()) } returns completedTask(null)

        repository.updateCampsite("site-2", campsite)

        assertEquals("site-2", (written.captured as FirestoreCampsite).campsiteId)
        assertEquals("site-2", repository.cached.single().campsiteId)
    }

    @Test
    fun updateCampsiteSavesEveryFieldExceptTheRating() = runTest {
        val options = slot<SetOptions>()
        io.mockk.every { campsites.document("site-1").set(any(), capture(options)) } returns completedTask(null)

        repository.updateCampsite("site-1", campsite)

        val allFields = FirestoreCampsite::class.java.declaredFields.map { it.name }
        val expected = SetOptions.mergeFields(
            allFields.filter { it != "rating" && it != "ratingCount" && !it.startsWith("$") }
        )
        assertEquals(expected, options.captured)
    }

    @Test
    fun deleteCampsiteDeletesDocument() = runTest {
        repository.deleteCampsite("site-1")

        verify(exactly = 1) { campsites.document("site-1").delete() }
    }

    @Test
    fun getAllCampsitesKeysByDocumentIdAndSkipsMalformedDates() = runTest {
        val malformed = FirestoreCampsite(
            campsiteId = "site-3",
            availability = FirestoreAvailabilitySchedule(mutableMapOf("not-a-date" to 0))
        )
        campsites.returnsOnGet(
            documentOf("site-1", FirestoreCampsite::class.java, campsite.toFirestore()),
            documentOf("doc-2", FirestoreCampsite::class.java, FirestoreCampsite(campsiteName = "Forest")),
            documentOf("site-3", FirestoreCampsite::class.java, malformed)
        )

        val result = repository.getAllCampsites()

        assertEquals(setOf("site-1", "doc-2"), result.keys)
        assertEquals("Forest", result.getValue("doc-2").campsiteName)
        assertEquals(campsite.availability, result.getValue("site-1").availability)
    }
}

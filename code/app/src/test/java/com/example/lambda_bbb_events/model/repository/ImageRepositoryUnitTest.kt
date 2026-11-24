package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.image.ImageChunk
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImageRepositoryUnitTest {
    private val db = mockk<FirebaseFirestore>()
    private lateinit var images: MockCollection
    private lateinit var repository: ImageRepository
    private val chunksRef = mockk<CollectionReference>()
    private val chunkRefs = mutableMapOf<String, DocumentReference>()

    private val metadata = ImageMetaData(imageId = "image-1", uploaderUserId = "ed", chunkCount = 2, uploadTimestamp = 1L)

    private fun chunkDocument(priority: Int): DocumentSnapshot {
        val document = mockk<DocumentSnapshot>()
        every { document.getBlob("imageBytes") } returns Blob.fromBytes(byteArrayOf(priority.toByte()))
        every { document.getString("imageId") } returns "image-1"
        every { document.getLong("priority") } returns priority.toLong()
        every { document.getLong("bandHeight") } returns 10L
        every { document.reference } returns chunkRef(priority.toString())
        return document
    }

    private fun chunkRef(id: String): DocumentReference {
        val existing = chunkRefs[id]
        if (existing != null) {
            return existing
        }
        val ref = mockk<DocumentReference>()
        every { ref.set(any()) } returns completedTask(null)
        every { ref.delete() } returns completedTask(null)
        chunkRefs[id] = ref
        return ref
    }

    @Before
    fun setup() {
        images = MockCollection(db, "images")
        every { images.document("image-1").collection("chunks") } returns chunksRef
        every { chunksRef.document(any<String>()) } answers { chunkRef(firstArg()) }
        every { images.document("image-1").get() } returns
            completedTask(documentOf("image-1", ImageMetaData::class.java, metadata))
        repository = ImageRepository(db)
    }

    @Test
    fun uploadImageWritesMetadataThenEachChunk() = runTest {
        val chunks = listOf(
            ImageChunk("image-1", 0, 10, byteArrayOf(1)),
            ImageChunk("image-1", 1, 10, byteArrayOf(2))
        )

        assertTrue(repository.uploadImage(metadata, chunks))

        verifyOrder {
            images.document("image-1").set(metadata)
            chunkRef("0").set(any())
            chunkRef("1").set(any())
        }
    }

    @Test
    fun uploadImageReturnsFalseOnFailure() = runTest {
        every { images.document("image-1").set(any()) } returns failedTask(IllegalStateException("offline"))

        assertFalse(repository.uploadImage(metadata, emptyList()))
    }

    @Test
    fun getImageReturnsChunksSortedByPriority() = runTest {
        every { chunksRef.get() } returns completedTask(querySnapshotOf(chunkDocument(1), chunkDocument(0)))

        val chunks = repository.getImage("image-1")

        assertEquals(listOf(0, 1), chunks.map { it.priority })
        assertEquals(1, chunks[1].imageBytes.single().toInt())
    }

    @Test
    fun getImageWithMissingChunksThrows() = runTest {
        every { chunksRef.get() } returns completedTask(querySnapshotOf(chunkDocument(0)))

        var error: Exception? = null
        try {
            repository.getImage("image-1")
        } catch (e: Exception) {
            error = e
        }

        assertTrue(error is IllegalStateException)
        assertEquals("Missing image chunks", error?.message)
    }

    @Test
    fun getImageWithoutMetadataThrows() = runTest {
        every { images.document("image-2").get() } returns
            completedTask(documentOf("image-2", ImageMetaData::class.java, null))

        var error: Exception? = null
        try {
            repository.getImage("image-2")
        } catch (e: Exception) {
            error = e
        }

        assertEquals("Image metadata missing", error?.message)
    }

    @Test
    fun deleteImageDeletesChunksThenMetadata() = runTest {
        every { chunksRef.get() } returns completedTask(querySnapshotOf(chunkDocument(0), chunkDocument(1)))

        assertTrue(repository.deleteImage("image-1"))

        verifyOrder {
            chunkRef("0").delete()
            chunkRef("1").delete()
            images.document("image-1").delete()
        }
    }

    @Test
    fun deleteImageReturnsFalseOnFailure() = runTest {
        every { chunksRef.get() } returns failedTask(IllegalStateException("offline"))

        assertFalse(repository.deleteImage("image-1"))
        verify(exactly = 0) { images.document("image-1").delete() }
    }
}

package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.Image
import com.example.lambda_bbb_events.model.repository.ImageRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImageRepositoryUnitTest {
    private val db = mockk<FirebaseFirestore>()
    private val imagesRef = mockk<CollectionReference>()
    private val documentRef = mockk<DocumentReference>()
    private val task = mockk<Task<Void>>()
    private val registration = mockk<ListenerRegistration>()
    private val listenerSlot = slot<EventListener<QuerySnapshot>>()
    private lateinit var repository: ImageRepository
    private lateinit var image: Image

    @Before
    fun setup() {
        image = mockk<Image>()

        every { image.imageId } returns "image-1"

        every {
            db.collection("images")
        } returns imagesRef

        every {
            imagesRef.addSnapshotListener(capture(listenerSlot))
        } returns registration

        every {
            imagesRef.document("image-1")
        } returns documentRef

        every {
            documentRef.set(image)
        } returns task

        every {
            documentRef.delete()
        } returns task

        repository = ImageRepository(db)
    }

    @Test
    fun imagesInitiallyEmpty() {
        assertTrue(repository.images.isEmpty())
    }

    @Test
    fun addImage() {
        repository.addImage(image)

        verify(exactly = 1) {
            imagesRef.document("image-1")
        }

        verify(exactly = 1) {
            documentRef.set(image)
        }
    }

    @Test
    fun deleteImage() {
        repository.deleteImage(image)

        verify(exactly = 1) {
            imagesRef.document("image-1")
        }

        verify(exactly = 1) {
            documentRef.delete()
        }
    }

    @Test
    fun updateImage() {
        repository.updateImage(image)

        verify(exactly = 1) {
            imagesRef.document("image-1")
        }

        verify(exactly = 1) {
            documentRef.set(image)
        }
    }

    @Test
    fun snapshotUpdatesImages() {
        val snapshot = mockk<QuerySnapshot>()
        val document = mockk<DocumentSnapshot>()

        every {
            snapshot.documents
        } returns listOf(document)

        every {
            document.toObject(Image::class.java)
        } returns image

        listenerSlot.captured.onEvent(snapshot, null)

        assertEquals(
            listOf(image),
            repository.images
        )
    }
}

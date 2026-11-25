package com.example.lambda_bbb_events.model.campsite

import com.example.lambda_bbb_events.model.image.ImageMetaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the [Campsite] model.
 */
class CampsiteUnitTest {

    @Test
    fun campsitesAreEqualByIdOwnerAndName() {
        val campsite = Campsite(campsiteId = "a", ownerId = "olive", campsiteName = "Lakeside", rating = 1f)
        val sameButRerated = campsite.copy(rating = 5f, description = "changed")

        assertEquals(campsite, sameButRerated)
        assertEquals(campsite.hashCode(), sameButRerated.hashCode())
        assertNotEquals(campsite, campsite.copy(campsiteName = "Forest"))
        assertNotEquals(campsite, campsite.copy(ownerId = "max"))
        assertNotEquals(campsite, campsite.copy(campsiteId = "b"))
    }

    @Test
    fun campsiteHasLocationUnlessCoordinatesAreUnset() {
        assertFalse(Campsite().hasLocation())
        assertTrue(Campsite(latitude = 53.5, longitude = -113.5).hasLocation())
        assertTrue(Campsite(latitude = 0.0, longitude = -113.5).hasLocation())
        assertTrue(Campsite(latitude = 53.5, longitude = 0.0).hasLocation())
    }

    @Test
    fun photosKeepTheirCentreInViewUntilAFocusIsChosen() {
        val photo = ImageMetaData(imageId = "photo-1")

        assertEquals(0.5f, photo.focusX)
        assertEquals(0.5f, photo.focusY)
    }

    @Test
    fun totalPointsAreAllStarsReceived() {
        assertEquals(135f, Campsite(rating = 4.5f, ratingCount = 30).totalPoints(), 0.001f)
        assertEquals(0f, Campsite().totalPoints(), 0.001f)
    }
}

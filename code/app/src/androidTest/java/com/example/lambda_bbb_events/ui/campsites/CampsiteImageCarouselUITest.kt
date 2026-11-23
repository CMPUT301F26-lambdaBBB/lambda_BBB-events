package com.example.lambda_bbb_events.ui.campsites

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.view.ui.components.common.CampsiteImageCarousel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests moving between a campsite's images with the carousel's Previous and Next buttons.
 */
@RunWith(AndroidJUnit4::class)
class CampsiteImageCarouselUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun showCarousel(imageCount: Int) {
        val images = mutableListOf<ImageMetaData>()
        for (i in 1..imageCount) {
            images.add(ImageMetaData(imageId = "carousel-test-image-$i"))
        }
        val campsite = Campsite(campsiteId = "carousel-test", campsiteName = "Lakeside", imageIds = images)

        composeTestRule.setContent {
            CampsiteImageCarousel(
                campsite = campsite,
                modifier = Modifier.fillMaxWidth().height(240.dp)
            )
        }
    }

    @Test
    fun startsOnThumbnailWithOnlyNextButton() {
        showCarousel(imageCount = 3)

        composeTestRule.onNodeWithTag("carousel_image_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("carousel_next").assertIsDisplayed()
        composeTestRule.onNodeWithTag("carousel_previous").assertDoesNotExist()
    }

    @Test
    fun nextButtonShowsNextImage() {
        showCarousel(imageCount = 3)

        composeTestRule.onNodeWithTag("carousel_next").performClick()

        composeTestRule.onNodeWithTag("carousel_image_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("carousel_previous").assertIsDisplayed()
        composeTestRule.onNodeWithTag("carousel_next").assertIsDisplayed()
    }

    @Test
    fun lastImageHasOnlyPreviousButton() {
        showCarousel(imageCount = 2)

        composeTestRule.onNodeWithTag("carousel_next").performClick()

        composeTestRule.onNodeWithTag("carousel_image_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("carousel_next").assertDoesNotExist()
        composeTestRule.onNodeWithTag("carousel_previous").assertIsDisplayed()
    }

    @Test
    fun previousButtonGoesBack() {
        showCarousel(imageCount = 3)

        composeTestRule.onNodeWithTag("carousel_next").performClick()
        composeTestRule.onNodeWithTag("carousel_previous").performClick()

        composeTestRule.onNodeWithTag("carousel_image_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("carousel_previous").assertDoesNotExist()
    }

    @Test
    fun singleImageHasNoButtons() {
        showCarousel(imageCount = 1)

        composeTestRule.onNodeWithTag("carousel_next").assertDoesNotExist()
        composeTestRule.onNodeWithTag("carousel_previous").assertDoesNotExist()
    }
}

package com.example.lambda_bbb_events.ui.campsites

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.view.ui.components.common.CampsiteImageCarousel
import com.example.lambda_bbb_events.view.ui.theme.Lambda_BBBeventsTheme
import org.junit.Assert.assertTrue
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

    private fun showCarousel(imageCount: Int, darkTheme: Boolean = false) {
        val images = mutableListOf<ImageMetaData>()
        for (i in 1..imageCount) {
            images.add(ImageMetaData(imageId = "carousel-test-image-$i"))
        }
        val campsite = Campsite(campsiteId = "carousel-test", campsiteName = "Lakeside", imageIds = images)

        composeTestRule.setContent {
            Lambda_BBBeventsTheme(darkTheme = darkTheme) {
                CampsiteImageCarousel(
                    campsite = campsite,
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                )
            }
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

    /** Checks that the Next button's arrow stands out from its circle, using the WCAG contrast ratio. */
    private fun assertArrowStandsOut() {
        val pixels = composeTestRule.onNodeWithTag("carousel_next").captureToImage().toPixelMap()
        // Inside the circle, but left of the arrow
        val circle = pixels[pixels.width / 4, pixels.height / 2].luminance()
        // The arrow is in the middle of the button
        var arrow = 0f
        for (x in pixels.width * 2 / 5..pixels.width * 3 / 5) {
            for (y in pixels.height * 2 / 5..pixels.height * 3 / 5) {
                arrow = maxOf(arrow, pixels[x, y].luminance())
            }
        }

        val contrast = (arrow + 0.05f) / (circle + 0.05f)
        assertTrue("Contrast is only $contrast", contrast >= 4.5f)
    }

    @Test
    fun arrowStandsOutInLightMode() {
        showCarousel(imageCount = 2, darkTheme = false)
        assertArrowStandsOut()
    }

    @Test
    fun arrowStandsOutInDarkMode() {
        showCarousel(imageCount = 2, darkTheme = true)
        assertArrowStandsOut()
    }
}

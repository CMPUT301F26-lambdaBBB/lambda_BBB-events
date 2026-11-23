package com.example.lambda_bbb_events.ui.create

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.view.ui.components.common.PhotoFocusDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests choosing the part of a photo that stays in view when it is cropped.
 */
@RunWith(AndroidJUnit4::class)
class PhotoFocusDialogUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** A tall photo, like a picture of a standing person. */
    private val photo = Bitmap.createBitmap(200, 400, Bitmap.Config.ARGB_8888)

    private var savedFocus: Pair<Float, Float>? = null
    private var dismissed = false

    private fun showDialog(focusX: Float = 0.5f, focusY: Float = 0.5f) {
        composeTestRule.setContent {
            PhotoFocusDialog(
                photo = photo,
                initialFocusX = focusX,
                initialFocusY = focusY,
                onDismiss = { dismissed = true },
                onSave = { x, y -> savedFocus = Pair(x, y) }
            )
        }
    }

    @Test
    fun showsPhotoAndBothCardPreviews() {
        showDialog()

        composeTestRule.onNodeWithTag("focus_picker").assertIsDisplayed()
        composeTestRule.onNodeWithTag("focus_preview_wide").assertIsDisplayed()
        composeTestRule.onNodeWithTag("focus_preview_narrow").assertIsDisplayed()
    }

    @Test
    fun savingWithoutTappingKeepsCurrentFocus() {
        showDialog(focusX = 0.3f, focusY = 0.2f)

        composeTestRule.onNodeWithTag("focus_save").performClick()

        assertEquals(Pair(0.3f, 0.2f), savedFocus)
    }

    @Test
    fun tappingThePhotoMovesTheFocus() {
        showDialog()

        // Tap a quarter of the way across and three quarters of the way down
        composeTestRule.onNodeWithTag("focus_picker").performTouchInput {
            click(percentOffset(0.25f, 0.75f))
        }
        composeTestRule.onNodeWithTag("focus_save").performClick()

        val focus = savedFocus!!
        assertEquals(0.25f, focus.first, 0.02f)
        assertEquals(0.75f, focus.second, 0.02f)
    }

    @Test
    fun cancellingDoesNotSave() {
        showDialog()

        composeTestRule.onNodeWithTag("focus_picker").performTouchInput { click(topLeft) }
        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(dismissed)
        assertNull(savedFocus)
    }

    @Test
    fun focusStaysInsideThePhoto() {
        showDialog()

        composeTestRule.onNodeWithTag("focus_picker").performTouchInput { click(bottomRight) }
        composeTestRule.onNodeWithTag("focus_save").performClick()

        val focus = savedFocus!!
        assertFalse(focus.first > 1f || focus.second > 1f)
        assertTrue("focus was $focus", focus.first > 0.9f && focus.second > 0.9f)
    }
}

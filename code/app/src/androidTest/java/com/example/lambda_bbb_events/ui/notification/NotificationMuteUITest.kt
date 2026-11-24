package com.example.lambda_bbb_events.ui.notification

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.toSize
import androidx.navigation.compose.rememberNavController
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.NotificationController
import com.example.lambda_bbb_events.view.ui.user_screens.notification.NotificationsScreen
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests muting people and categories by pressing and holding them. */
@RunWith(AndroidJUnit4::class)
class NotificationMuteUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var time = 1_000_000L
    private val notifications = NotificationController { time }
    private val controllers = mockk<AppControllers>(relaxed = true)

    @Before
    fun setUp() {
        every { controllers.notifications } returns notifications
        composeTestRule.setContent {
            NotificationsScreen(rememberNavController(), controllers)
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    private fun row(id: String) = composeTestRule.onNodeWithTag("notification_$id")

    private fun pressAndHold(id: String) {
        row(id).performScrollTo().performTouchInput { longClick() }
    }

    private fun choose(option: String) {
        composeTestRule.onNodeWithTag(option).performClick()
    }

    @Test
    fun tappingDoesNotOpenTheMenu() {
        row(PERSON).performClick()

        composeTestRule.onNodeWithTag("mute_option").assertDoesNotExist()
    }

    @Test
    fun personCanBeMutedAndUnmuted() {
        pressAndHold(PERSON)
        composeTestRule.onNodeWithTag("unmute_option").assertIsNotEnabled()
        choose("mute_option")

        composeTestRule.onNodeWithTag("mute_option").assertDoesNotExist()
        row(PERSON).assertTextContains("Muted")
        row("person_yuta").assertTextContains("Unmuted")

        pressAndHold(PERSON)
        composeTestRule.onNodeWithTag("mute_option").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("mute_temporarily_option").assertIsEnabled()
        choose("unmute_option")

        row(PERSON).assertTextContains("Unmuted")
    }

    @Test
    fun lastCategoryCanBeMutedAndUnmuted() {
        pressAndHold("category_sent")
        choose("mute_option")
        row("category_sent").assertTextContains("Muted")

        pressAndHold("category_sent")
        choose("unmute_option")
        row("category_sent").assertTextContains("Weekly updates")
    }

    @Test
    fun temporaryMuteEndsWhileTheScreenIsOpen() {
        pressAndHold(PERSON)
        choose("mute_temporarily_option")
        row(PERSON).assertTextContains("Muted until", substring = true)

        time += TEN_MINUTES
        composeTestRule.mainClock.advanceTimeBy(TEN_MINUTES + 1_000)

        row(PERSON).assertTextContains("Unmuted")
    }

    @Test
    fun menuOpensBelowTheRowWhereItWasPressed() {
        val rowBounds = screenBounds("notification_$PERSON")
        row(PERSON).performTouchInput { longClick(Offset(width * 0.25f, height / 2f)) }

        val option = screenBounds("mute_option")
        assertTrue(option.top >= rowBounds.bottom)
        assertEquals(rowBounds.left + rowBounds.width * 0.25f, option.left, 2f)
    }

    @Test
    fun menuOfTheLastRowDoesNotCoverIt() {
        pressAndHold("category_sent")

        val rowBounds = screenBounds("notification_category_sent")
        val isAbove = screenBounds("unmute_option").bottom < rowBounds.top
        val isBelow = screenBounds("mute_option").top > rowBounds.bottom
        assertTrue(isAbove || isBelow)
    }

    @Test
    fun menuOpensFromTheRowWhenUsingAScreenReader() {
        val rowBounds = screenBounds("notification_$PERSON")
        // A touch beforehand must not decide where the menu opens
        row(PERSON).performTouchInput { click(Offset(width * 0.25f, height / 2f)) }
        row(PERSON).performSemanticsAction(SemanticsActions.OnLongClick)

        assertEquals(rowBounds.left, screenBounds("mute_option").left, 2f)
    }

    @Test
    fun menuCanBeClosedWithoutMuting() {
        pressAndHold(PERSON)
        Espresso.pressBack()

        composeTestRule.onNodeWithTag("mute_option").assertDoesNotExist()
        row(PERSON).assertTextContains("Unmuted")
        assertFalse(notifications.isMuted(PERSON))
    }

    /** Bounds on the screen, since the menu is in its own window. */
    private fun screenBounds(tag: String): Rect {
        val node = composeTestRule.onNodeWithTag(tag).fetchSemanticsNode()
        return Rect(node.positionOnScreen, node.size.toSize())
    }

    private companion object {
        const val PERSON = "person_admin"
        const val TEN_MINUTES = 600_000L
    }
}

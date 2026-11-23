package com.example.lambda_bbb_events

import androidx.compose.material3.Text
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests that spamming the back arrow only goes back one screen.
 */
class BackNavigationUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: NavHostController

    @Before
    fun setup() {
        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(navController = navController, startDestination = "first") {
                composable("first") {
                    ScreenTitle(title = "First", navController = navController)
                }
                composable("second") {
                    ScreenTitle(title = "Second", navController = navController, showBack = true)
                }
                composable("third") {
                    ScreenTitle(title = "Third", navController = navController, showBack = true)
                }
            }
        }
    }

    private fun navigate(route: String) {
        composeTestRule.runOnIdle { navController.navigate(route) }
        composeTestRule.waitForIdle()
    }

    private fun tapBackRepeatedly(times: Int) {
        // Pause the clock so every tap lands while the screen is still animating out.
        // While both the leaving and entering screens are shown, tap every back arrow
        // on screen, which covers whichever one a real tap would hit.
        composeTestRule.mainClock.autoAdvance = false
        repeat(times) {
            val arrows = composeTestRule.onAllNodesWithContentDescription("Back")
            arrows.fetchSemanticsNodes().indices.forEach { index ->
                arrows[index].performSemanticsAction(SemanticsActions.OnClick)
            }
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
    }

    @Test
    fun spammingBackFromSecondScreenStopsAtFirst() {
        navigate("second")

        tapBackRepeatedly(5)

        composeTestRule.onNodeWithText("First").assertExists()
        composeTestRule.runOnIdle {
            assertEquals("first", navController.currentDestination?.route)
        }
    }

    @Test
    fun spammingBackFromThirdScreenOnlyGoesBackOne() {
        navigate("second")
        navigate("third")

        tapBackRepeatedly(5)

        composeTestRule.onNodeWithText("Second").assertExists()
        composeTestRule.runOnIdle {
            assertEquals("second", navController.currentDestination?.route)
        }
    }

    @Test
    fun backWorksAgainAfterReturningToScreen() {
        navigate("second")
        tapBackRepeatedly(1)
        navigate("second")

        tapBackRepeatedly(1)

        composeTestRule.runOnIdle {
            assertEquals("first", navController.currentDestination?.route)
        }
    }
}

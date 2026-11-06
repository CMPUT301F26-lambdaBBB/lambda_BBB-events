package com.example.lambda_bbb_events

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.view.ui.SignupScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.setContent {
            SignupScreen(
                navController = rememberNavController(),
                onCreateEntrant = { _, _, _ -> true }
            )
        }
    }

    @Test
    fun lambdaCampingTitleIsDisplayed() {
        composeTestRule.onNodeWithText("Lambda Camping").assertIsDisplayed()
    }

    @Test
    fun nameLabelIsDisplayed() {
        composeTestRule.onNodeWithTag("nameLabel").assertIsDisplayed()
    }

    @Test
    fun emailLabelIsDisplayed() {
        composeTestRule.onNodeWithTag("emailLabel").assertIsDisplayed()
    }

    @Test
    fun phoneNumberLabelIsDisplayed() {
        composeTestRule.onNodeWithTag("phonenumberLabel").assertIsDisplayed()
    }

    @Test
    fun nameInputWorks() {
        composeTestRule.onNodeWithTag("nameInput").performTextInput("For UITest")
        composeTestRule.onNodeWithTag("nameInput").assertTextEquals("For UITest")
    }

    @Test
    fun emailInputWorks() {
        composeTestRule.onNodeWithTag("emailInput").performTextInput("uitest@hotmail.com")
        composeTestRule.onNodeWithTag("emailInput").assertTextEquals("uitest@hotmail.com")
    }

    @Test
    fun phoneNumberInputWorks() {
        composeTestRule.onNodeWithTag("phonenumberInput").performTextInput("123456789")
        composeTestRule.onNodeWithTag("phonenumberInput").assertTextEquals("123456789")
    }

    @Test
    fun continueButtonIsDisplayed () {
        composeTestRule.onNodeWithTag("continueButton").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("continueButton").performScrollTo().assertHasClickAction()
    }
}
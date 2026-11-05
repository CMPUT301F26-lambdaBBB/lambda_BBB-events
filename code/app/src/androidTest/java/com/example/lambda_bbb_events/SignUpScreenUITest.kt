package com.example.lambda_bbb_events

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpScreenUITest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun lambdaCampingTitleIsDisplayed() {
        composeTestRule.onNodeWithText("Lambda Camping").assertIsDisplayed()
    }

    @Test
    fun nameLabelIsDisplayed() {
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
    }

    @Test
    fun emailLabelIsDisplayed() {
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun phoneNumberLabelIsDisplayed() {
        composeTestRule.onNodeWithText("Phone Number").assertIsDisplayed()
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
        composeTestRule.onNodeWithTag("continueButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("continueButton").assertHasClickAction()
    }
}
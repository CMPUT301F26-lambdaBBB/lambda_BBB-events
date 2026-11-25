package com.example.lambda_bbb_events.ui.account

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.view.ui.user_screens.signup.SignupScreen
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests picking a country calling code from the dropdown for the phone number on the sign up screen.
 */
@RunWith(AndroidJUnit4::class)
class PhoneNumberFieldUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var submittedPhone: String? = null

    @Before
    fun setup() {
        composeTestRule.setContent {
            SignupScreen(
                navController = rememberNavController(),
                onCreateEntrant = { _, _, phone ->
                    submittedPhone = phone
                    true
                }
            )
        }
    }

    private fun fillNameAndEmail() {
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Ed")
        composeTestRule.onNodeWithTag("emailInput").performTextInput("ed@example.com")
    }

    private fun submit() {
        composeTestRule.onNodeWithTag("continueButton").performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    private fun pickCountry(search: String, isoCode: String) {
        composeTestRule.onNodeWithTag("country_code_selector").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("country_search").performTextInput(search)
        composeTestRule.onNodeWithTag("country_option_$isoCode").performClick()
    }

    @Test
    fun defaultsToCanada() {
        composeTestRule.onNodeWithTag("country_code_selector").assertTextContains("+1", substring = true)
    }

    @Test
    fun numberIsSavedWithDefaultCountryCode() {
        fillNameAndEmail()
        composeTestRule.onNodeWithTag("phonenumberInput").performTextInput("7805550123")

        submit()

        assertEquals("+1 7805550123", submittedPhone)
    }

    @Test
    fun pickedCountryCodeIsShownAndSaved() {
        fillNameAndEmail()
        pickCountry(search = "44", isoCode = "GB")
        composeTestRule.onNodeWithTag("phonenumberInput").performTextInput("7911123456")

        composeTestRule.onNodeWithTag("country_code_selector").assertTextContains("+44", substring = true)
        submit()

        assertEquals("+44 7911123456", submittedPhone)
    }

    @Test
    fun dropdownOpensWithEveryCountryListed() {
        composeTestRule.onNodeWithTag("country_code_selector").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("country_option_CA").assertIsDisplayed()
        composeTestRule.onNodeWithTag("country_option_US").assertExists()
    }

    @Test
    fun searchIsClearedWhenDropdownReopens() {
        pickCountry(search = "Japan", isoCode = "JP")
        composeTestRule.onNodeWithTag("country_code_selector").performClick()

        composeTestRule.onNodeWithTag("country_option_CA").assertIsDisplayed()
    }

    @Test
    fun countriesCanBeSearchedByName() {
        pickCountry(search = "Japan", isoCode = "JP")

        composeTestRule.onNodeWithTag("country_code_selector").assertTextContains("+81", substring = true)
    }

    @Test
    fun canadianNumberIsShownFormattedButSavedAsDigits() {
        fillNameAndEmail()
        composeTestRule.onNodeWithTag("phonenumberInput").performTextInput("7805550123")

        composeTestRule.onNodeWithTag("phonenumberInput").assertTextEquals("(780) 555-0123")
        submit()

        assertEquals("+1 7805550123", submittedPhone)
    }

    @Test
    fun typingDigitByDigitKeepsTheirOrder() {
        for (digit in "7805550123") {
            composeTestRule.onNodeWithTag("phonenumberInput").performTextInput(digit.toString())
        }

        composeTestRule.onNodeWithTag("phonenumberInput").assertTextEquals("(780) 555-0123")
    }

    @Test
    fun pastedFormattedNumberKeepsOnlyDigits() {
        composeTestRule.onNodeWithTag("phonenumberInput").performTextInput("(780) 555-0123")

        composeTestRule.onNodeWithTag("phonenumberInput").assertTextEquals("(780) 555-0123")
    }

    @Test
    fun canadianNumberStopsAtTenDigits() {
        composeTestRule.onNodeWithTag("phonenumberInput").performTextInput("780555012399")

        composeTestRule.onNodeWithTag("phonenumberInput").assertTextEquals("(780) 555-0123")
    }

    @Test
    fun otherCountriesUseTheirOwnFormat() {
        fillNameAndEmail()
        pickCountry(search = "44", isoCode = "GB")
        composeTestRule.onNodeWithTag("phonenumberInput").performTextInput("7911123456")

        composeTestRule.onNodeWithTag("phonenumberInput").assertTextEquals("7911 123456")
        submit()

        assertEquals("+44 7911123456", submittedPhone)
    }

    @Test
    fun formatChangesWhenCountryChanges() {
        composeTestRule.onNodeWithTag("phonenumberInput").performTextInput("612345678")
        pickCountry(search = "France", isoCode = "FR")

        composeTestRule.onNodeWithTag("phonenumberInput").assertTextEquals("6 12 34 56 78")
    }

    @Test
    fun searchWithoutMatchesSaysSo() {
        composeTestRule.onNodeWithTag("country_code_selector").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("country_search").performTextInput("Atlantis")

        composeTestRule.onNodeWithText("No countries found.").assertIsDisplayed()
    }

    @Test
    fun dismissingDropdownKeepsCountry() {
        composeTestRule.onNodeWithTag("country_code_selector").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("country_code_picker").assertIsDisplayed()

        Espresso.pressBack()

        composeTestRule.onNodeWithTag("country_code_picker").assertDoesNotExist()
        composeTestRule.onNodeWithTag("country_code_selector").assertTextContains("+1", substring = true)
    }
}

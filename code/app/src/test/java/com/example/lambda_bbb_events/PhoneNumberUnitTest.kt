package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.COUNTRY_CODES
import com.example.lambda_bbb_events.model.CountryCode
import com.example.lambda_bbb_events.model.DEFAULT_COUNTRY_CODE
import com.example.lambda_bbb_events.model.formatPhoneNumber
import com.example.lambda_bbb_events.model.parsePhoneNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberUnitTest {
    private fun country(iso: String) = COUNTRY_CODES.first { it.isoCode == iso }

    @Test
    fun formatJoinsCountryCodeAndDigits() {
        assertEquals("+1 7805550123", formatPhoneNumber(DEFAULT_COUNTRY_CODE, "7805550123"))
        assertEquals("+44 7911123456", formatPhoneNumber(country("GB"), "7911123456"))
    }

    @Test
    fun formatKeepsOnlyDigits() {
        assertEquals("+1 7805550123", formatPhoneNumber(DEFAULT_COUNTRY_CODE, " (780) 555-0123 "))
    }

    @Test
    fun formatWithoutDigitsIsEmpty() {
        assertEquals("", formatPhoneNumber(DEFAULT_COUNTRY_CODE, ""))
        assertEquals("", formatPhoneNumber(DEFAULT_COUNTRY_CODE, " - "))
    }

    @Test
    fun parseSplitsStoredNumber() {
        assertEquals(country("GB") to "7911123456", parsePhoneNumber("+44 7911123456"))
        assertEquals(country("IN") to "9876543210", parsePhoneNumber("+91 9876543210"))
    }

    @Test
    fun parsePrefersLongestCallingCode() {
        // +353 (Ireland) and +351 (Portugal) must not be read as +35x prefixes of other codes
        assertEquals(country("IE") to "851234567", parsePhoneNumber("+353 851234567"))
        assertEquals(country("PT") to "912345678", parsePhoneNumber("+351 912345678"))
        assertEquals(country("RU") to "9161234567", parsePhoneNumber("+7 9161234567"))
    }

    @Test
    fun sharedCallingCodePrefersDefaultCountry() {
        assertEquals(DEFAULT_COUNTRY_CODE to "7805550123", parsePhoneNumber("+1 7805550123"))
    }

    @Test
    fun numbersSavedWithoutCountryCodeUseDefault() {
        assertEquals(DEFAULT_COUNTRY_CODE to "7805550123", parsePhoneNumber("7805550123"))
        assertEquals(DEFAULT_COUNTRY_CODE to "7805550123", parsePhoneNumber("780-555-0123"))
        assertEquals(DEFAULT_COUNTRY_CODE to "", parsePhoneNumber(""))
    }

    @Test
    fun formatAndParseRoundTripForEveryCountry() {
        for (option in COUNTRY_CODES) {
            val (parsed, local) = parsePhoneNumber(formatPhoneNumber(option, "5551234"))

            assertEquals("5551234", local)
            assertEquals(option.dialCode, parsed.dialCode)
        }
    }

    @Test
    fun countryListStartsWithDefaultAndHasNoDuplicates() {
        assertEquals(DEFAULT_COUNTRY_CODE, COUNTRY_CODES.first())
        assertEquals(COUNTRY_CODES.size, COUNTRY_CODES.map { it.isoCode }.toSet().size)
        assertTrue(COUNTRY_CODES.all { it.dialCode.matches(Regex("\\+\\d{1,3}")) })
    }

    @Test
    fun flagIsBuiltFromIsoCode() {
        assertEquals("🇨🇦", CountryCode("CA", "+1").flag)
        assertEquals("🇯🇵", CountryCode("JP", "+81").flag)
    }
}

package com.example.lambda_bbb_events.model.formatting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies phone-number parsing and formatting. */
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
        assertEquals(PhoneNumber(country("GB"), "7911123456"), parsePhoneNumber("+44 7911123456"))
        assertEquals(PhoneNumber(country("IN"), "9876543210"), parsePhoneNumber("+91 9876543210"))
    }

    @Test
    fun parsePrefersLongestCallingCode() {
        // +353 (Ireland) and +351 (Portugal) must not be read as +35x prefixes of other codes
        assertEquals(PhoneNumber(country("IE"), "851234567"), parsePhoneNumber("+353 851234567"))
        assertEquals(PhoneNumber(country("PT"), "912345678"), parsePhoneNumber("+351 912345678"))
        assertEquals(PhoneNumber(country("RU"), "9161234567"), parsePhoneNumber("+7 9161234567"))
    }

    @Test
    fun sharedCallingCodePrefersDefaultCountry() {
        assertEquals(PhoneNumber(DEFAULT_COUNTRY_CODE, "7805550123"), parsePhoneNumber("+1 7805550123"))
    }

    @Test
    fun numbersSavedWithoutCountryCodeUseDefault() {
        assertEquals(PhoneNumber(DEFAULT_COUNTRY_CODE, "7805550123"), parsePhoneNumber("7805550123"))
        assertEquals(PhoneNumber(DEFAULT_COUNTRY_CODE, "7805550123"), parsePhoneNumber("780-555-0123"))
        assertEquals(PhoneNumber(DEFAULT_COUNTRY_CODE, ""), parsePhoneNumber(""))
    }

    @Test
    fun formatAndParseRoundTripForEveryCountry() {
        for (option in COUNTRY_CODES) {
            val parsed = parsePhoneNumber(formatPhoneNumber(option, "5551234"))

            assertEquals("5551234", parsed.localNumber)
            assertEquals(option.dialCode, parsed.country.dialCode)
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

    @Test
    fun northAmericanNumberIsFormattedWithAreaCode() {
        assertEquals("(780) 555-0123", formatNorthAmericanNumber("7805550123"))
    }

    @Test
    fun partlyTypedNorthAmericanNumberIsFormattedAsFarAsItGoes() {
        assertEquals("", formatNorthAmericanNumber(""))
        assertEquals("(7", formatNorthAmericanNumber("7"))
        assertEquals("(780", formatNorthAmericanNumber("780"))
        assertEquals("(780) 5", formatNorthAmericanNumber("7805"))
        assertEquals("(780) 555", formatNorthAmericanNumber("780555"))
        assertEquals("(780) 555-0", formatNorthAmericanNumber("7805550"))
    }

    @Test
    fun numbersThatAreNotNorthAmericanLengthAreLeftAlone() {
        assertEquals("78055501234", formatNorthAmericanNumber("78055501234"))
        assertEquals("780-555", formatNorthAmericanNumber("780-555"))
    }

    @Test
    fun otherCountriesUseTheirOwnFormat() {
        assertEquals("7911 123456", formatLocalNumber(country("GB"), "7911123456"))
        assertEquals("6 12 34 56 78", formatLocalNumber(country("FR"), "612345678"))
        assertEquals("90-1234-5678", formatLocalNumber(country("JP"), "9012345678"))
        assertEquals("412 345 678", formatLocalNumber(country("AU"), "412345678"))
        assertEquals("11 91234-5678", formatLocalNumber(country("BR"), "11912345678"))
    }

    @Test
    fun canadianNumbersKeepAreaCodeFormat() {
        assertEquals("(780) 555-0123", formatLocalNumber(DEFAULT_COUNTRY_CODE, "7805550123"))
        assertEquals("(202) 555-0147", formatLocalNumber(country("US"), "2025550147"))
    }

    @Test
    fun partlyTypedNumbersAreFormattedAsTheyGo() {
        assertEquals("7911 12", formatLocalNumber(country("GB"), "791112"))
        assertEquals("6 12 3", formatLocalNumber(country("FR"), "6123"))
    }

    @Test
    fun formattingNeverChangesTheDigits() {
        for (option in COUNTRY_CODES) {
            val digits = "123456789012".take(maxLocalNumberLength(option))
            for (length in 0..digits.length) {
                val typed = digits.take(length)
                val formatted = formatLocalNumber(option, typed)

                assertEquals(option.isoCode, typed, formatted.filter { it.isDigit() })
            }
        }
    }

    @Test
    fun localNumberLengthLimits() {
        assertEquals(10, maxLocalNumberLength(DEFAULT_COUNTRY_CODE))
        assertEquals(13, maxLocalNumberLength(country("GB")))
        assertEquals(12, maxLocalNumberLength(country("IE")))
    }

    @Test
    fun everyCountryHasAnExampleInItsFormat() {
        assertEquals("(", exampleLocalNumber(DEFAULT_COUNTRY_CODE).take(1))
        for (option in COUNTRY_CODES) {
            assertTrue(option.isoCode, exampleLocalNumber(option).isNotEmpty())
        }
    }
}

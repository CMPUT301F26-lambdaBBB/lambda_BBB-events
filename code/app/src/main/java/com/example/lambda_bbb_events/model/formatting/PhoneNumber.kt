package com.example.lambda_bbb_events.model.formatting

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

/*
 * Defines country calling codes and how phone numbers are stored.
 */

/**
 * A country and its international calling code.
 *
 * @property isoCode The two-letter ISO 3166 country code, such as "CA".
 * @property dialCode The calling code including the plus sign, such as "+1".
 */
data class CountryCode(
    val isoCode: String,
    val dialCode: String
) {
    /** The country's name in the user's language. */
    val name: String
        get() = Locale.Builder().setRegion(isoCode).build().getDisplayCountry(Locale.getDefault())

    /**
     * The country's flag emoji. A flag is made of two special "regional indicator" letters,
     * one for each letter of the ISO code, which phones draw together as a flag.
     */
    val flag: String
        get() {
            var flag = ""
            for (letter in isoCode.uppercase()) {
                val regionalLetter = 0x1F1E6 + (letter - 'A')
                flag += String(Character.toChars(regionalLetter))
            }
            return flag
        }
}

/**
 * A stored phone number split into its parts.
 *
 * @property country The country the number belongs to.
 * @property localNumber The digits after the country code.
 */
data class PhoneNumber(
    val country: CountryCode,
    val localNumber: String
)

/** Canada, the default country for new numbers and numbers saved without a country code. */
val DEFAULT_COUNTRY_CODE = CountryCode("CA", "+1")

/** Countries that can be picked, with the default first and the rest sorted by name. */
val COUNTRY_CODES: List<CountryCode> = listOf(DEFAULT_COUNTRY_CODE) + listOf(
    CountryCode("US", "+1"),
    CountryCode("MX", "+52"),
    CountryCode("BR", "+55"),
    CountryCode("AR", "+54"),
    CountryCode("CL", "+56"),
    CountryCode("CO", "+57"),
    CountryCode("PE", "+51"),
    CountryCode("GB", "+44"),
    CountryCode("IE", "+353"),
    CountryCode("FR", "+33"),
    CountryCode("DE", "+49"),
    CountryCode("ES", "+34"),
    CountryCode("PT", "+351"),
    CountryCode("IT", "+39"),
    CountryCode("NL", "+31"),
    CountryCode("BE", "+32"),
    CountryCode("CH", "+41"),
    CountryCode("AT", "+43"),
    CountryCode("SE", "+46"),
    CountryCode("NO", "+47"),
    CountryCode("DK", "+45"),
    CountryCode("FI", "+358"),
    CountryCode("PL", "+48"),
    CountryCode("UA", "+380"),
    CountryCode("GR", "+30"),
    CountryCode("TR", "+90"),
    CountryCode("RU", "+7"),
    CountryCode("IL", "+972"),
    CountryCode("AE", "+971"),
    CountryCode("SA", "+966"),
    CountryCode("EG", "+20"),
    CountryCode("NG", "+234"),
    CountryCode("KE", "+254"),
    CountryCode("ZA", "+27"),
    CountryCode("IN", "+91"),
    CountryCode("PK", "+92"),
    CountryCode("BD", "+880"),
    CountryCode("CN", "+86"),
    CountryCode("HK", "+852"),
    CountryCode("TW", "+886"),
    CountryCode("JP", "+81"),
    CountryCode("KR", "+82"),
    CountryCode("PH", "+63"),
    CountryCode("VN", "+84"),
    CountryCode("TH", "+66"),
    CountryCode("MY", "+60"),
    CountryCode("SG", "+65"),
    CountryCode("ID", "+62"),
    CountryCode("AU", "+61"),
    CountryCode("NZ", "+64")
).sortedBy { it.name }

/**
 * Combines a country code and a local number into the stored form, such as "+1 7805550123".
 *
 * Everything but digits is removed from the local number.
 *
 * @param country The selected country.
 * @param localNumber The number as typed, without the country code.
 * @return The full number, or an empty string if no digits were entered.
 */
fun formatPhoneNumber(country: CountryCode, localNumber: String): String {
    val digits = localNumber.filter { it.isDigit() }
    return if (digits.isEmpty()) "" else "${country.dialCode} $digits"
}

/**
 * Splits a stored phone number back into its country and local number.
 *
 * Numbers saved before country codes were added have no "+" and are treated as belonging
 * to [DEFAULT_COUNTRY_CODE]. When several countries share a calling code, such as Canada
 * and the United States, the default country is preferred.
 *
 * @param phone The stored phone number.
 * @return The country and the local digits.
 */
fun parsePhoneNumber(phone: String): PhoneNumber {
    val trimmed = phone.trim()
    val digits = trimmed.filter { it.isDigit() }
    if (!trimmed.startsWith("+")) {
        return PhoneNumber(DEFAULT_COUNTRY_CODE, digits)
    }

    // Find the longest calling code the number starts with. The default country comes
    // first in the list, so it wins when several countries share the same code.
    var bestMatch: CountryCode? = null
    for (country in COUNTRY_CODES) {
        val code = country.dialCode.removePrefix("+")
        val isLonger = bestMatch == null || country.dialCode.length > bestMatch.dialCode.length
        if (digits.startsWith(code) && isLonger) {
            bestMatch = country
        }
    }

    if (bestMatch == null) {
        return PhoneNumber(DEFAULT_COUNTRY_CODE, digits)
    }
    val localNumber = digits.removePrefix(bestMatch.dialCode.removePrefix("+"))
    return PhoneNumber(bestMatch, localNumber)
}

/**
 * Formats the digits of a Canadian or US number (+1) the way they are usually written,
 * such as "(780) 555-0123". Partly typed numbers are formatted as far as they go, such as
 * "(780) 55". Anything that is not up to 10 digits is returned unchanged.
 *
 * @param digits The local number's digits, without the country code.
 * @return The formatted number.
 */
fun formatNorthAmericanNumber(digits: String): String {
    if (digits.isEmpty() || digits.length > 10 || !digits.all { it.isDigit() }) {
        return digits
    }

    val areaCode = digits.take(3)
    val exchange = digits.drop(3).take(3)
    val lineNumber = digits.drop(6)

    var formatted = "($areaCode"
    if (exchange.isNotEmpty()) {
        formatted += ") $exchange"
    }
    if (lineNumber.isNotEmpty()) {
        formatted += "-$lineNumber"
    }
    return formatted
}

/**
 * Formats a local number the way it is usually written in its country, such as
 * "7911 123456" for the United Kingdom or "6 12 34 56 78" for France. Canadian and US
 * numbers are written as "(780) 555-0123". Partly typed numbers are formatted as far as
 * they go, and numbers that cannot be formatted are returned as plain digits.
 *
 * @param country The country the number belongs to.
 * @param digits The local number's digits, without the country code.
 * @return The formatted number.
 */
fun formatLocalNumber(country: CountryCode, digits: String): String {
    if (country.dialCode == "+1") {
        return formatNorthAmericanNumber(digits)
    }
    if (digits.isEmpty() || !digits.all { it.isDigit() }) {
        return digits
    }

    // Google's libphonenumber knows every country's format. Type the whole international
    // number into its formatter, the way a phone's dialer does, then take the country
    // code back off the front.
    val formatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(country.isoCode)
    var formatted = ""
    for (character in country.dialCode + digits) {
        formatted = formatter.inputDigit(character)
    }

    val countryCodePrefix = country.dialCode + " "
    if (formatted.startsWith(countryCodePrefix)) {
        return formatted.removePrefix(countryCodePrefix)
    }
    return digits
}

/**
 * Gets the most digits a local number can have in [country]. Phone numbers have at most
 * 15 digits including the country code, and Canadian and US numbers always have 10.
 */
fun maxLocalNumberLength(country: CountryCode): Int {
    if (country.dialCode == "+1") {
        return 10
    }
    val countryCodeDigits = country.dialCode.length - 1
    return 15 - countryCodeDigits
}

/**
 * Gets an example mobile number for [country], formatted for display, such as
 * "7400 123456" for the United Kingdom.
 *
 * @return The example, or an empty string if none is known for the country.
 */
fun exampleLocalNumber(country: CountryCode): String {
    val example = PhoneNumberUtil.getInstance()
        .getExampleNumberForType(country.isoCode, PhoneNumberUtil.PhoneNumberType.MOBILE)
    if (example == null) {
        return ""
    }
    return formatLocalNumber(country, example.nationalNumber.toString())
}

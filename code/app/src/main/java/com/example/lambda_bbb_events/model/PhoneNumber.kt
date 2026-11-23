package com.example.lambda_bbb_events.model

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

    /** The country's flag, built from the regional indicator symbols for its ISO code. */
    val flag: String
        get() = isoCode.uppercase().map { Character.toChars(0x1F1E6 + (it - 'A')).concatToString() }
            .joinToString("")
}

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
fun parsePhoneNumber(phone: String): Pair<CountryCode, String> {
    val trimmed = phone.trim()
    if (!trimmed.startsWith("+")) {
        return DEFAULT_COUNTRY_CODE to trimmed.filter { it.isDigit() }
    }

    val digits = trimmed.filter { it.isDigit() }
    // Prefer the longest matching calling code, then the default country among equals
    val country = COUNTRY_CODES
        .filter { digits.startsWith(it.dialCode.drop(1)) }
        .sortedWith(
            compareByDescending<CountryCode> { it.dialCode.length }
                .thenByDescending { it == DEFAULT_COUNTRY_CODE }
        )
        .firstOrNull()
        ?: return DEFAULT_COUNTRY_CODE to digits

    return country to digits.drop(country.dialCode.length - 1)
}

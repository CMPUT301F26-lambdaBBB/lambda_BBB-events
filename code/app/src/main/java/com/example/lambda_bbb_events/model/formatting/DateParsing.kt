package com.example.lambda_bbb_events.model.formatting

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Reads a date written as "yyyy-MM-dd", such as "2026-07-01".
 *
 * @param text The date as text.
 * @return The date, or `null` if the text is not a valid date.
 */
fun parseDateOrNull(text: String): LocalDate? {
    try {
        return LocalDate.parse(text)
    } catch (e: DateTimeParseException) {
        return null
    }
}

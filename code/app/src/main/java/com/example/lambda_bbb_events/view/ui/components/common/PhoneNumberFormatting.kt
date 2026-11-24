package com.example.lambda_bbb_events.view.ui.components.common

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import com.example.lambda_bbb_events.model.formatting.CountryCode
import com.example.lambda_bbb_events.model.formatting.formatLocalNumber

/**
 * Keeps only digits in a phone number field, up to [maxLength] of them.
 *
 * Runs on everything typed or pasted, so "(780) 555-0123" is kept as "7805550123".
 *
 * @param maxLength The most digits the number can have.
 */
class PhoneDigitsInputTransformation(
    private val maxLength: Int
) : InputTransformation {

    override fun TextFieldBuffer.transformInput() {
        val typed = asCharSequence().toString()
        val digits = typed.filter { it.isDigit() }.take(maxLength)
        if (digits != typed) {
            replace(0, length, digits)
        }
    }
}

/**
 * Shows the digits in a phone number field formatted for [country], such as
 * "(780) 555-0123" in Canada or "7911 123456" in the United Kingdom. Only what is shown
 * changes; the field's text stays plain digits.
 *
 * @param country The country whose format is used.
 */
class PhoneNumberOutputTransformation(
    private val country: CountryCode
) : OutputTransformation {

    override fun TextFieldBuffer.transformOutput() {
        val formatted = formatLocalNumber(country, asCharSequence().toString())

        // The digits are already in the field, so only the brackets, spaces, and dashes are
        // added, each at its place. Compose keeps the cursor in the right spot by itself.
        for (i in formatted.indices) {
            if (!formatted[i].isDigit()) {
                insert(i, formatted[i].toString())
            }
        }
    }
}

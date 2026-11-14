
package com.example.lambda_bbb_events.view.ui.components.calendar

import android.app.DatePickerDialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.Locale

/**
 * Displays an editable date field with a calendar picker.
 *
 * @param value Current date in yyyy-MM-dd format.
 * @param onValueChange Called when the date changes.
 * @param modifier Modifier for the date field.
 */
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    OutlinedTextField(
        value = value,
        onValueChange = { input ->

            val digits = input
                .filter { it.isDigit() }
                .take(8)

            val formattedDate = buildString {
                append(digits.take(4))

                if (digits.length > 4) {
                    append("-")
                    append(
                        digits.substring(
                            4,
                            minOf(6, digits.length)
                        )
                    )
                }

                if (digits.length > 6) {
                    append("-")
                    append(digits.substring(6))
                }
            }

            onValueChange(formattedDate)
        },
        placeholder = {
            Text(
                text = "yyyy-mm-dd",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        modifier = modifier,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        trailingIcon = {
            IconButton(
                onClick = {
                    val selectedDate = runCatching {
                        LocalDate.parse(value)
                    }.getOrNull() ?: LocalDate.now()

                    DatePickerDialog(
                        context,
                        { _, year, month, day ->

                            val formattedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                day
                            )

                            onValueChange(formattedDate)
                        },
                        selectedDate.year,
                        selectedDate.monthValue - 1,
                        selectedDate.dayOfMonth
                    ).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}


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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/*
 * Provides a date input field with automatic formatting
 * and an Android calendar date picker.
 *
 * Uses Material 3 theme colors and typography.
 */

/**
 * Displays an editable date field with a calendar date picker.
 *
 * Users can manually enter a date in yyyy-MM-dd format or
 * select a date using the calendar icon.
 *
 * @param value The currently entered date as a string
 * in yyyy-MM-dd format.
 * @param onValueChange Callback invoked when the date
 * changes through manual input or calendar selection.
 * @param modifier Modifier used to customize the
 * appearance and layout of the date field.
 */
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val digits = newValue.text
                .filter { it in '0'..'9' }
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

            textFieldValue = TextFieldValue(
                text = formattedDate,
                selection = TextRange(formattedDate.length)
            )

            onValueChange(formattedDate)
        },

        placeholder = {
            Text(
                text = "yyyy-mm-dd",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            cursorColor = MaterialTheme.colorScheme.primary,

            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
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

                            textFieldValue = TextFieldValue(
                                text = formattedDate,
                                selection = TextRange(formattedDate.length),
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

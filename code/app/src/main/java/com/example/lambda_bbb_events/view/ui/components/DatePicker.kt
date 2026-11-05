package com.example.lambda_bbb_events.view.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import java.time.LocalDate
import java.util.Locale

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
            Text("yyyy-mm-dd")
        },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
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
                                selection = TextRange(formattedDate.length)
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
                    contentDescription = "Select Date"
                )
            }
        }
    )
}

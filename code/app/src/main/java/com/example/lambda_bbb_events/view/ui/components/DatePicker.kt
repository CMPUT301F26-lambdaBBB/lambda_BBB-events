package com.example.lambda_bbb_events.view.ui.components

import android.app.DatePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text("yyyy-mm-dd")
        },
        modifier = modifier,
        singleLine = true,
        trailingIcon = {
            IconButton(
                onClick = {
                    val calendar = Calendar.getInstance()

                    // Use the typed date if it is valid
                    val format = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                    ).apply {
                        isLenient = false
                    }

                    val typedDate = if (
                        Regex("""\d{4}-\d{2}-\d{2}""").matches(value)
                    ) {
                        runCatching {
                            format.parse(value)
                        }.getOrNull()
                    } else {
                        null
                    }

                    if (typedDate != null) {
                        calendar.time = typedDate
                    }

                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val selectedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                day
                            )
                            onValueChange(selectedDate)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
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

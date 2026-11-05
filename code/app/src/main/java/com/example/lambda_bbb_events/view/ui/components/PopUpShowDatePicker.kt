package com.example.lambda_bbb_events.view.ui.components

import android.app.DatePickerDialog
import android.content.Context
import java.util.Calendar

/**
 * Show a date picker dialog.
 *
 * @param context The context used to display the date picker dialog.
 * @param onDateSelected A lambda function that is called when a date is selected.
 */
fun PopUpShowDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected("$year-${month + 1}-$dayOfMonth")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
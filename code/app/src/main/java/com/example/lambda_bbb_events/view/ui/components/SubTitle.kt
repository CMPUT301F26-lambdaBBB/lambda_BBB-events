package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Defines a reusable subtitle component for section headings.
 */

/**
 * Displays a subtitle for a section of the application.
 *
 * @param title The text displayed as the subtitle.
 */
@Composable
fun SubTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
}
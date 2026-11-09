package com.example.lambda_bbb_events.view.ui.components.lottery

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Defines a status box for displaying campsite registration status
 */

/**
 * Displays the current status of a campsite registration.
 *
 * @param registrationStatus The current registration status
 * displayed prominently in the box.
 * @param message Additional information describing
 * the registration status.
 * @param boxColor Background color of the status box.
 */
@Composable
fun RegistrationStatusBox(
    registrationStatus: String,
    message: String,
    boxColor : Color
    ) {
    Box (
        modifier = Modifier.fillMaxWidth()
            .background(
                color = boxColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text (
                text = "Registration Status"
            )

            Text(
                text = registrationStatus,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text (
                text = message,
                fontSize = 15.sp,
                maxLines = 2
            )
        }
    }
}

package com.example.lambda_bbb_events.view.ui.components.lottery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * Defines a status box for displaying campsite registration status.
 */

/**
 * Displays the current status of a campsite registration.
 *
 * Uses Material 3 theme typography and text colors.
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
    boxColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = boxColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(
                text = "Registration Status",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = registrationStatus,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

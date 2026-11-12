package com.example.lambda_bbb_events.view.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Displays a navigation indicator for the campsite
 * creation process.
 *
 * Users can select a stage to navigate directly to it.
 *
 * @param currentStep Index of the currently active stage.
 * @param onStepClick Callback invoked when a stage is clicked.
 */
@Composable
fun StepStageIndicator(
    currentStep: Int,
    onStepClick: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepPill(
                text = "Basic Info",
                isActive = currentStep == 0,
                onClick = { onStepClick(0) }
            )

            Text(
                text = "→",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            StepPill(
                text = "Location",
                isActive = currentStep == 1,
                onClick = { onStepClick(1) }
            )

            Text(
                text = "→",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            StepPill(
                text = "Lottery",
                isActive = currentStep == 2,
                onClick = { onStepClick(2) }
            )
        }
    }
}

/**
 * Displays a selectable stage indicator.
 *
 * @param text Label displayed inside the stage indicator.
 * @param isActive Whether the stage is currently selected.
 * @param onClick Callback invoked when the stage is clicked.
 */
@Composable
private fun StepPill(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            style = if (isActive) {
                MaterialTheme.typography.labelLarge
            } else {
                MaterialTheme.typography.bodySmall
            },
            color = if (isActive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            )
        )
    }
}

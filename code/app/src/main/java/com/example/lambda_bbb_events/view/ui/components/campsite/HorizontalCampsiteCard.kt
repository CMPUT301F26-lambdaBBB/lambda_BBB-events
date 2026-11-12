package com.example.lambda_bbb_events.view.ui.components.campsite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
 * Defines a horizontal card for displaying campsite information.
 *
 * TODO: Replace the placeholder with actual campsite details.
 */

/**
 * Displays a placeholder card for campsite information.
 */
@Composable
fun HorizontalCampsiteCard() {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(220.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SOMETHING stuff here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
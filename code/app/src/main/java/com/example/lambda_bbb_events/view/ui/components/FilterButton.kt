package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * Defines a reusable filter button for opening search filters.
 *
 * This component may not stay. not used or idk but work in progress or to be removed later
 */

/**
 * Displays a filter button for accessing search filtering options.
 *
 * @param onClick Callback invoked when the user clicks
 * the filter button.
 */

@Composable
fun FilterButton(
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text("Filters", fontWeight = FontWeight.Bold) },
        leadingIcon = {
            Icon(Icons.Outlined.FilterAlt, contentDescription = "Filter", tint = Color.Black)
        },
        shape = RoundedCornerShape(100.dp)
    )
}
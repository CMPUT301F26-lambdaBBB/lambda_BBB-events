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

@Composable
fun FilterButton() {
    AssistChip(
        onClick = { /* open filters */ },
        label = { Text("Filters", fontWeight = FontWeight.Bold) },
        leadingIcon = {
            Icon(Icons.Outlined.FilterAlt, contentDescription = "Filter", tint = Color.Black)
        },
        shape = RoundedCornerShape(100.dp)
    )
}
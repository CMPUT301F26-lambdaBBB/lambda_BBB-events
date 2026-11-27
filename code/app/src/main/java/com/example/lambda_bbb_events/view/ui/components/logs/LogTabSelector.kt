package com.example.lambda_bbb_events.view.ui.components.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.LogType

/**
 * Displays a segmented tab selector for the admin logs screen.
 *
 * Allows the administrator to switch between notification
 * logs and message logs.
 *
 * @param selectedTab The currently selected log type.
 * @param onTabSelected Callback invoked when a tab is selected.
 * @param modifier Modifier applied to the tab selector.
 */
@Composable
fun LogTabSelector(
    selectedTab: LogType,
    onTabSelected: (LogType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
            .selectableGroup()
    ) {
        LogTab(
            title = "Notifications",
            selected = selectedTab == LogType.NOTIFICATION,
            onClick = {
                onTabSelected(LogType.NOTIFICATION)
            },
            modifier = Modifier.weight(1f)
        )

        LogTab(
            title = "Messages",
            selected = selectedTab == LogType.MESSAGE,
            onClick = {
                onTabSelected(LogType.MESSAGE)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Displays an individual selectable log tab.
 *
 * The background and text colors change depending
 * on whether the tab is selected.
 *
 * @param title The text displayed inside the tab.
 * @param selected Whether the tab is currently selected.
 * @param onClick Callback invoked when the tab is selected.
 * @param modifier Modifier applied to the individual tab.
 */
@Composable
private fun LogTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
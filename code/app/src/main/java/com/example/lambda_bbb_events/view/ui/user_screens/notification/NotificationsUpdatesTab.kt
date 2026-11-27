package com.example.lambda_bbb_events.view.ui.user_screens.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.NotificationController
import com.example.lambda_bbb_events.model.notification.NotificationHistory
import java.text.DateFormat
import java.util.Date

/**
 * Displays the current user's system notification history.
 *
 * @param currentUserId Device ID of the current user.
 * @param notificationController Controller for system notifications.
 * @param modifier Modifier applied to the tab.
 */
@Composable
fun NotificationUpdatesTab(
    currentUserId: String,
    notificationController: NotificationController,
    modifier: Modifier = Modifier
) {
    val historyFlow = remember(
        currentUserId,
        notificationController
    ) {
        notificationController.observeHistory(currentUserId)
    }

    val history by key(currentUserId) {
        historyFlow.collectAsState(
            initial = emptyList<NotificationHistory>()
        )
    }

    var showClearDialog by remember(currentUserId) {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recent notifications",
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(
                onClick = { showClearDialog = true },
                enabled = history.isNotEmpty()
            ) {
                Text("Clear all")
            }
        }

        if (history.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "No notifications",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Your lottery and reservation updates will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = history,
                    key = { it.id }
                ) { notification ->

                    NotificationHistoryCard(
                        notification = notification
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = {
                showClearDialog = false
            },
            title = {
                Text("Clear notifications?")
            },
            text = {
                Text("This will remove your notification history.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        notificationController.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Displays an individual system notification.
 */
@Composable
private fun NotificationHistoryCard(
    notification: NotificationHistory
) {
    val formattedDate = remember(notification.timestamp) {
        DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT
        ).format(Date(notification.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
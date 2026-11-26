package com.example.lambda_bbb_events.view.ui.components.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.AdminLog
import com.example.lambda_bbb_events.model.LogType

/**
 * Displays the complete details of an administrative log.
 *
 * Allows the administrator to review the sender name, recipient
 * names, timestamp, campsite name, and content of a notification
 * or message.
 *
 * The dialog displays resolved names instead of database
 * identifiers and supports logs with multiple recipients.
 *
 * This dialog is read-only and does not allow administrators
 * to modify or delete logs.
 *
 * @param log The notification or message log to display.
 * @param onDismiss Callback invoked when the dialog is closed.
 */
@Composable
fun AdminLogDialog(
    log: AdminLog,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = when (log.type) {
                    LogType.NOTIFICATION -> "Notification Details"
                    LogType.MESSAGE -> "Message Details"
                },
                style = MaterialTheme.typography.titleLarge
            )
        },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (log.title.isNotBlank()) {
                    LogDetailRow(
                        label = "Title",
                        value = log.title
                    )
                }

                LogDetailRow(
                    label = "From",
                    value = log.senderText
                )

                LogDetailRow(
                    label = "To",
                    value = log.recipientText
                )

                LogDetailRow(
                    label = "Sent at",
                    value = formatLogTimestamp(log.timestamp)
                )

                if (log.campsiteId.isNotBlank()) {
                    LogDetailRow(
                        label = "Campsite Name",
                        value = log.campsiteText
                    )
                }

                HorizontalDivider()

                Text(
                    text = when (log.type) {
                        LogType.NOTIFICATION -> "Notification"
                        LogType.MESSAGE -> "Message"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = log.content.ifBlank {
                        "No content available."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Display all recipient names for group messages.
                if (log.recipientCount > 1) {

                    HorizontalDivider()

                    Text(
                        text = "Recipients (${log.recipientCount})",
                        style = MaterialTheme.typography.titleMedium
                    )

                    log.recipientIds.forEach { recipientId ->

                        Text(
                            text = log.recipientNames[recipientId]
                                ?.takeIf { it.isNotBlank() }
                                ?: "Unknown recipient",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Close")
            }
        }
    )
}

/**
 * Displays a labelled piece of information within a log dialog.
 *
 * @param label The label describing the information.
 * @param value The value associated with the label.
 */
@Composable
private fun LogDetailRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
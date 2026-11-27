package com.example.lambda_bbb_events.view.ui.components.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.AdminLog
import com.example.lambda_bbb_events.model.LogType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Displays an individual notification or message log.
 *
 * Displays resolved sender and recipient names instead of
 * their database identifiers.
 *
 * Selecting the card allows the administrator to review
 * the complete details of the log.
 *
 * @param log The notification or message log to display.
 * @param onClick Callback invoked when the card is selected.
 * @param modifier Modifier applied to the card.
 */
@Composable
fun AdminLogCard(
    log: AdminLog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LogIcon(type = log.type)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = when (log.type) {
                        LogType.NOTIFICATION -> {
                            log.title.ifBlank { "Notification" }
                        }

                        LogType.MESSAGE -> {
                            "From: ${log.senderText}"
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (log.type == LogType.MESSAGE) {
                    Text(
                        text = "To: ${log.recipientText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = when (log.recipientCount) {
                        0 -> "Unknown recipients"

                        1 -> {
                            if (log.type == LogType.NOTIFICATION) {
                                "Sent to ${log.recipientText}"
                            } else {
                                "Sent to 1 user"
                            }
                        }

                        else -> {
                            "Sent to ${log.recipientCount} users"
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatLogTimestamp(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View log details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Displays an icon representing the type of administrative log.
 *
 * Notification logs use a notification icon, while message
 * logs use an email icon.
 *
 * @param type The type of log represented by the icon.
 */
@Composable
private fun LogIcon(type: LogType) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (type) {
                LogType.NOTIFICATION -> Icons.Default.Notifications
                LogType.MESSAGE -> Icons.Default.Email
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Formats a log timestamp into a human-readable date and time.
 *
 * Uses the device's locale and time zone.
 *
 * @param timestamp The timestamp in milliseconds since the Unix epoch.
 * @return The formatted date and time, or "Unknown date"
 * if the timestamp is unavailable.
 */
fun formatLogTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) {
        return "Unknown date"
    }

    val formatter = SimpleDateFormat(
        "MMM d, yyyy, hh:mm a",
        Locale.getDefault()
    )

    return formatter.format(Date(timestamp))
}
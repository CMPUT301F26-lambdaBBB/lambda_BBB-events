package com.example.lambda_bbb_events.view.ui.components.messaging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.Message
import java.text.SimpleDateFormat

/**
 * Displays an individual message within a conversation.
 *
 * Messages sent by the current user are aligned to the right,
 * while messages received from other users are aligned to the left.
 *
 * @param message The message to display.
 * @param currentUserId Unique identifier of the current user.
 * @param senderName Display name of the message sender.
 * @param modifier Modifier applied to the message container.
 */
@Composable
fun MessageBubble(
    message: Message,
    currentUserId: String,
    senderName: String = "",
    modifier: Modifier = Modifier
) {

    val isOwnMessage = message.senderId == currentUserId

    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

    val formattedTime = remember(message.timestamp, locale) {
        SimpleDateFormat(
            "h:mm a",
            locale
        ).format(message.timestamp.toDate())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isOwnMessage) {
            Alignment.End
        } else {
            Alignment.Start
        }
    ) {

        if (!isOwnMessage && senderName.isNotBlank()) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = 8.dp,
                    bottom = 4.dp
                )
            )
        }

        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            color = if (isOwnMessage) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isOwnMessage) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ) {

            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                )
            )
        }

        Text(
            text = formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
        )
    }
}
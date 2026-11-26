package com.example.lambda_bbb_events.view.ui.components.messaging

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.Message
import java.text.SimpleDateFormat

/**
 * Displays an individual message within a conversation.
 *
 * Messages sent by the current user are aligned to the right,
 * while messages received from other users are aligned to the left.
 * Deleted messages keep their place and show [Message.DELETED_TEXT].
 *
 * @param message The message to display.
 * @param currentUserId Unique identifier of the current user.
 * @param senderName Display name of the message sender.
 * @param onLongClick Called when the message is long-pressed, or `null` if
 * it cannot be acted on, such as when it cannot be deleted.
 * @param modifier Modifier applied to the message container.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Changed with the help of this AI tool.
 * Prompts:
 * - "Also please clean up a database and offer a way for users to delete messages with a message
 *   deleted plus. Show also that messages are active."
 * - "So to clarify I want to allow users and admins to remove messages."
 */
@Composable
fun MessageBubble(
    message: Message,
    currentUserId: String,
    senderName: String = "",
    onLongClick: (() -> Unit)? = null,
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

        if (message.deleted) {
            DeletedMessageBubble()
        } else {
            MessageContentBubble(
                content = message.content,
                isOwnMessage = isOwnMessage,
                onLongClick = onLongClick
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

/**
 * Displays the text of a message that has not been deleted.
 *
 * @param content Text of the message.
 * @param isOwnMessage Whether the current user sent the message.
 * @param onLongClick Called when the bubble is long-pressed, or `null` to ignore long presses.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Written with the help of this AI tool.
 * Prompts:
 * - "Also please clean up a database and offer a way for users to delete messages with a message
 *   deleted plus. Show also that messages are active."
 * - "So to clarify I want to allow users and admins to remove messages."
 */
@Composable
private fun MessageContentBubble(
    content: String,
    isOwnMessage: Boolean,
    onLongClick: (() -> Unit)?
) {
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
    )

    Surface(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clip(shape)
            .combinedClickable(
                enabled = onLongClick != null,
                onClickLabel = null,
                onLongClickLabel = "Delete message",
                onLongClick = onLongClick,
                onClick = {}
            )
            .testTag("messageBubble"),
        shape = shape,
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
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 10.dp
            )
        )
    }
}

/**
 * Displays the outlined placeholder left where a deleted message was.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Written with the help of this AI tool.
 * Prompts:
 * - "Also please clean up a database and offer a way for users to delete messages with a message
 *   deleted plus. Show also that messages are active."
 * - "So to clarify I want to allow users and admins to remove messages."
 */
@Composable
private fun DeletedMessageBubble() {
    Surface(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .testTag("deletedMessage"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Block,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = Message.DELETED_TEXT,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

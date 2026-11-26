package com.example.lambda_bbb_events.view.ui.components.messaging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.view.ui.components.common.ProfilePicture
import java.text.SimpleDateFormat

/**
 * Displays a conversation preview within the messaging tab.
 *
 * Shows the contact's profile picture and display name, what connects them to
 * the user, the most recent message, and its time. Lines up with the other
 * notification rows.
 *
 * @param conversation The conversation to display.
 * @param contactName Display name of the conversation contact.
 * @param subtitle What connects the contact to the user, such as their campsites.
 * @param contactImageId The contact's profile picture ID, blank to show the default icon.
 * @param onClick Callback invoked when the conversation is selected.
 * @param modifier Modifier applied to the conversation container.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Changed with the help of this AI tool.
 */
@Composable
fun ConversationCard(
    conversation: Conversation,
    contactName: String,
    subtitle: String,
    contactImageId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val locale = LocalConfiguration.current.locales[0]

    val formattedTime = remember(
        conversation.updatedAt,
        locale
    ) {
        SimpleDateFormat(
            "h:mm a",
            locale
        ).format(conversation.updatedAt.toDate())
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        ProfilePicture(
            imageId = contactImageId,
            size = 48.dp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = contactName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (conversation.lastMessage.isNotBlank()) {

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = conversation.lastMessage.ifBlank {
                    "No messages yet"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
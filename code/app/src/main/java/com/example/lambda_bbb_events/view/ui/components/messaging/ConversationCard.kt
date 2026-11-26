package com.example.lambda_bbb_events.view.ui.components.messaging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.Conversation
import java.text.SimpleDateFormat

/**
 * Displays a conversation preview within the messaging tab.
 *
 * Shows the contact's display name, associated campsite,
 * most recent message, and timestamp.
 *
 * @param conversation The conversation to display.
 * @param contactName Display name of the conversation contact.
 * @param campsiteName Name of the associated campsite.
 * @param onClick Callback invoked when the conversation is selected.
 * @param modifier Modifier applied to the conversation container.
 */
@Composable
fun ConversationCard(
    conversation: Conversation,
    contactName: String,
    campsiteName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val locale = LocalConfiguration.current.locales[0]

    val formattedTime = remember(
        conversation.updatedAt,
        locale
    ) {
        conversation.updatedAt?.let {
            SimpleDateFormat(
                "h:mm a",
                locale
            ).format(it.toDate())
        } ?: ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Contact avatar
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                if (contactName.isNotBlank()) {

                    Text(
                        text = contactName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme
                            .onPrimaryContainer
                    )

                } else {

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme
                            .onPrimaryContainer
                    )
                }
            }
        }

        // Conversation information
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Contact name
                Text(
                    text = contactName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Timestamp
                if (conversation.lastMessage.isNotBlank()) {

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                }
            }

            // Campsite name
            Text(
                text = campsiteName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Latest message preview
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

        // Navigation arrow
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
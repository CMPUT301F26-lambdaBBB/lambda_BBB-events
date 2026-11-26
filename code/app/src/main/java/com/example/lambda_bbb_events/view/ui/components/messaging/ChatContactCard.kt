package com.example.lambda_bbb_events.view.ui.components.messaging

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.view.ui.components.common.ProfilePicture

/*
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * This whole file was written with the help of this AI tool.
 * Prompts:
 * - "Hey could you fix the view for the messages. It seems a bit weird with a fully non rounded
 *   top area and the fact you can't press enter to send a message and also because the profile
 *   photo isn't shown."
 * - "Can you make sure the new messages stuff follows the previous app layouts."
 */

/**
 * Displays who a chat is with, as a card showing the contact's profile picture, name,
 * and the conversation subtitle. Matches the layout of the app's other user cards.
 *
 * @param name Display name of the contact.
 * @param subtitle Additional information about the conversation, hidden when blank.
 * @param contactImageId The contact's profile picture ID, blank to show the default icon.
 * @param modifier Modifier applied to the card.
 */
@Composable
fun ChatContactCard(
    name: String,
    subtitle: String,
    contactImageId: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chatContactCard"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePicture(
                imageId = contactImageId,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            ChatContactText(
                name = name,
                subtitle = subtitle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Displays the contact's name with the conversation subtitle beneath it.
 *
 * @param name Display name of the contact.
 * @param subtitle Additional information about the conversation, hidden when blank.
 * @param modifier Modifier applied to the text column.
 */
@Composable
private fun ChatContactText(
    name: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

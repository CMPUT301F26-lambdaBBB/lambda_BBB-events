package com.example.lambda_bbb_events.view.ui.components.messaging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays a text input field and send button for messaging.
 *
 * Prevents users from sending empty messages and limits
 * message content to 2000 characters.
 *
 * @param content Current content of the message input field.
 * @param onContentChange Callback invoked when the content changes.
 * @param onSend Callback invoked when the send button is pressed.
 * @param enabled Whether the input field and send button are enabled.
 * @param modifier Modifier applied to the input container.
 */
@Composable
fun MessageInputField(
    content: String,
    onContentChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(
            value = content,
            onValueChange = {
                if (it.length <= 2000) {
                    onContentChange(it)
                }
            },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Type a message...")
            },
            shape = RoundedCornerShape(24.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            maxLines = 4,
            enabled = enabled
        )

        IconButton(
            onClick = onSend,
            enabled = enabled && content.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message"
            )
        }
    }
}
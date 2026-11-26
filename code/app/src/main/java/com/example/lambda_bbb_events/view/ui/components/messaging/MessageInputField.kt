package com.example.lambda_bbb_events.view.ui.components.messaging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.view.ui.components.common.formTextFieldColors

/**
 * Displays a text input field and send button for messaging.
 *
 * Prevents users from sending empty messages and limits
 * message content to 2000 characters. Pressing the keyboard's send
 * action or a hardware Enter key sends the message, while Shift+Enter
 * inserts a new line.
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
            .padding(vertical = 8.dp),
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
            modifier = Modifier
                .weight(1f)
                .onPreviewKeyEvent { sendOnEnter(it, onSend) },
            placeholder = {
                Text(
                    text = "Type a message...",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = formTextFieldColors(),
            maxLines = 4,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { onSend() }
            )
        )

        IconButton(
            onClick = onSend,
            enabled = enabled && content.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message",
                tint = if (enabled && content.isNotBlank()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * Sends the message when [event] is an Enter key press without Shift held.
 *
 * Both the key press and its release are consumed so no new line is typed. Shift+Enter
 * and every other key are left for the text field to handle.
 *
 * @param event The key event received by the text field.
 * @param onSend Called when the message should be sent.
 * @return `true` if the event was consumed.
 */
private fun sendOnEnter(event: KeyEvent, onSend: () -> Unit): Boolean {
    val isEnter = event.key == Key.Enter || event.key == Key.NumPadEnter
    if (!isEnter || event.isShiftPressed) {
        return false
    }
    if (event.type == KeyEventType.KeyDown) {
        onSend()
    }
    return true
}

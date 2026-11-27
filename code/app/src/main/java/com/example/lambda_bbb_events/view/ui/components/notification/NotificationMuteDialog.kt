package com.example.lambda_bbb_events.view.ui.components.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.message.NotificationPreference

/**
 * Represents the available notification mute options.
 *
 * @property label Text displayed for the option.
 */
enum class MuteOption(val label: String) {
    UNMUTED("Unmuted"),
    ONE_HOUR("Mute for 1 hour"),
    TWENTY_FOUR_HOURS("Mute for 24 hours"),
    INDEFINITE("Mute indefinitely")
}

/**
 * Displays a dialog for managing a contact's notification preferences.
 *
 * Allows users to enable notifications or temporarily
 * or indefinitely mute them.
 *
 * @param contactName Display name of the contact.
 * @param preference Current notification preference.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param onConfirm Callback returning the selected mute option.
 */
@Composable
fun NotificationMuteDialog(
    contactName: String,
    preference: NotificationPreference,
    onDismiss: () -> Unit,
    onConfirm: (MuteOption) -> Unit
) {
    var selectedOption by remember(preference.contactId) {
        mutableStateOf<MuteOption?>(null)
    }

    val currentStatus = when {
        !preference.isMuted() -> "Unmuted"
        preference.mutedUntil == null -> "Muted indefinitely"
        else -> "Temporarily muted"
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Notification settings")
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = contactName,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = currentStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    MuteOption.entries.forEach { option ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedOption == option,
                                    onClick = {
                                        selectedOption = option
                                    },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = null
                            )

                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Text(
                    text = "Muting notifications does not prevent you " +
                            "from receiving messages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },

        confirmButton = {
            TextButton(
                enabled = selectedOption != null,
                onClick = {
                    selectedOption?.let(onConfirm)
                }
            ) {
                Text("Save")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
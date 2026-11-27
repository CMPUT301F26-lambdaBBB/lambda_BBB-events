package com.example.lambda_bbb_events.view.ui.components.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Displays a dialog that asks for the admin password before a user is made an admin. The
 * password is hidden while it is typed, and is forgotten when the dialog closes.
 *
 * @param name The name of the user to make an admin.
 * @param isSaving Whether the password is being checked, so nothing can be changed.
 * @param isPasswordWrong Whether the last password was not accepted.
 * @param onConfirm Called with the entered password.
 * @param onDismiss Called when the dialog is closed without confirming.
 */
@Composable
fun MakeAdminDialog(
    name: String,
    isSaving: Boolean,
    isPasswordWrong: Boolean,
    onConfirm: (password: String) -> Unit,
    onDismiss: () -> Unit
) {
    val passwordState = rememberTextFieldState()
    val password = passwordState.text.toString()

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = { Text("Make Admin?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "$name will be able to manage all campsites and profiles, including " +
                            "removing them. Enter the admin password to continue."
                )
                OutlinedSecureTextField(
                    state = passwordState,
                    textObfuscationMode = TextObfuscationMode.Hidden,
                    label = { Text("Admin password") },
                    enabled = !isSaving,
                    isError = isPasswordWrong,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adminPasswordInput")
                )
                if (isPasswordWrong) {
                    Text(
                        text = "That password was not accepted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("adminPasswordError")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password) },
                enabled = password.isNotEmpty() && !isSaving,
                modifier = Modifier.testTag("adminPasswordConfirm")
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}

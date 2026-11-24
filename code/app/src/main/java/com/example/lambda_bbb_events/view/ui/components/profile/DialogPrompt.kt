
package com.example.lambda_bbb_events.view.ui.components.profile


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/*
Prompts an easy to call alert dialog for more consequential requests
 */

/**
 * Displays a dialog with customizable text and actions.
 *
 * @param dialogLabel Dialog title.
 * @param dialogCaption Dialog message.
 * @param confirmButton Action when confirmed ( Should be enclosed in {} ).
 * @param confirmText Confirmation button label.
 * @param dismissState Action when dismissed ( Should be enclosed in {} ).
 * @param cancelText Cancellation button label.
 * @param modifier Modifier applied to function.
 */

@Composable
fun ShowAlertDialog(
    dialogLabel: String,
    dialogCaption: String,
    confirmButton: () -> Unit,
    confirmText: String,
    dismissState: () -> Unit,
    cancelText: String,
    modifier: Modifier = Modifier
) {
        AlertDialog(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface,
            // close dialog when the user taps outside the box in specific state.
            onDismissRequest = dismissState,
            title = { Text(dialogLabel) },
            text = { Text(dialogCaption) },
            confirmButton = {
                TextButton( onClick = confirmButton ) { Text(confirmText, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton( onClick = dismissState ) {
                    Text(cancelText)
                }
            }
        )
}

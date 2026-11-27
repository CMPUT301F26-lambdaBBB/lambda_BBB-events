package com.example.lambda_bbb_events.view.ui.components.admin

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.controller.AdminLotteryController
import kotlinx.coroutines.launch

/** Logcat tag for run-lotteries failures. */
private const val LOG_TAG = "RunLotteries"

/**
 * Admin-only button that runs all lotteries immediately after the admin confirms.
 *
 * @param controller Runs the lotteries on the server.
 * @param deviceId The signed-in admin's device ID, sent so the function can verify the role.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun RunLotteriesButton(
    controller: AdminLotteryController,
    deviceId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showConfirm = true },
        enabled = !isRunning,
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_run_lotteries_button")
    ) {
        Icon(imageVector = Icons.Outlined.Casino, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isRunning) "Running lotteries..." else "Run all lotteries now (Admin)",
            style = MaterialTheme.typography.labelLarge,
            fontSize = 12.sp,
            maxLines = 1
        )
    }

    if (showConfirm) {
        RunLotteriesConfirmDialog(
            onConfirm = {
                showConfirm = false
                isRunning = true
                scope.launch {
                    val result = controller.runAllLotteriesNow(deviceId)
                    isRunning = false
                    result.exceptionOrNull()?.let { Log.e(LOG_TAG, "Run lotteries failed", it) }
                    Toast.makeText(context, resultMessage(result), Toast.LENGTH_LONG).show()
                }
            },
            onDismiss = { showConfirm = false }
        )
    }
}

/**
 * Asks the admin to confirm running every lottery now.
 *
 * @param onConfirm Called when the admin confirms.
 * @param onDismiss Called when the dialog is cancelled.
 */
@Composable
private fun RunLotteriesConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag("admin_run_lotteries_dialog"),
        onDismissRequest = onDismiss,
        title = { Text("Run all lotteries?") },
        text = { Text("This draws winners for every lottery right now instead of waiting for the scheduled run.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("admin_run_lotteries_confirm")
            ) { Text("Run") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Builds the toast text shown after a run attempt.
 *
 * @param result Outcome of the run.
 * @return A short user-facing message.
 */
private fun resultMessage(result: Result<Unit>): String =
    result.fold(
        onSuccess = { "Lotteries ran successfully." },
        onFailure = { "Failed to run lotteries: ${it.message}" }
    )

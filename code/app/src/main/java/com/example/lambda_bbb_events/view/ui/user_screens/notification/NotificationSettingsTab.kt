package com.example.lambda_bbb_events.view.ui.user_screens.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.NotificationPreferenceController
import com.example.lambda_bbb_events.model.message.MessagingAccess
import com.example.lambda_bbb_events.view.ui.components.notification.MuteOption
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationMuteDialog
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationRow
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationSectionHeader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Displays notification settings organized by people
 * and notification categories.
 *
 * Allows users to temporarily or indefinitely mute
 * notifications from campsite owners and administrators.
 *
 * @param messagingAccess List of campsite messaging access records.
 * @param profileNames Map of profile IDs to display names.
 * @param preferenceController Controller for notification preferences.
 * @param modifier Modifier applied to the settings container.
 */
@Composable
fun NotificationSettingsTab(
    messagingAccess: List<MessagingAccess>,
    profileNames: Map<String, String>,
    preferenceController: NotificationPreferenceController,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var selectedContactId by remember {
        mutableStateOf<String?>(null)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // Retrieve unique campsite owner IDs.
    val ownerIds = messagingAccess
        .filter { it.active }
        .map { it.ownerId }
        .filter { it.isNotBlank() }
        .distinct()

    // All administrators share one notification preference.
    val contactIds = buildList {
        addAll(ownerIds)

        if (messagingAccess.isNotEmpty()) {
            add("ADMIN_SUPPORT")
        }
    }.distinct()

    val categories = listOf(
        Triple(
            "Rejection information",
            "High priority",
            Icons.Outlined.Info
        ),
        Triple(
            "Lottery results",
            "Weekly updates",
            Icons.Outlined.ConfirmationNumber
        ),
        Triple(
            "Cancellations",
            "Weekly updates",
            Icons.Outlined.Cancel
        ),
        Triple(
            "Notifications Sent",
            "Weekly updates",
            Icons.Outlined.Info
        )
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {

        item {
            Text(
                text = "Manage alerts from specific people",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = 4.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
            )

            NotificationSectionHeader("People")
        }

        errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (isSaving) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (contactIds.isEmpty()) {
            item {
                Text(
                    text = "No contacts available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Display contacts and their notification preferences.
        items(
            items = contactIds,
            key = { "person_$it" }
        ) { contactId ->

            val contactName =
                if (contactId == "ADMIN_SUPPORT") {
                    "Admin"
                } else {
                    profileNames[contactId] ?: "Campsite Owner"
                }

            val preference =
                preferenceController.getPreference(contactId)

            val status = when {
                !preference.isMuted() -> "Unmuted"

                preference.mutedUntil == null ->
                    "Muted indefinitely"

                else -> "Temporarily muted"
            }

            NotificationRow(
                contactName,
                status,
                onClick = {
                    if (!isSaving) {
                        errorMessage = null
                        selectedContactId = contactId
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            if (contactId != contactIds.lastOrNull()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        item {
            Spacer(
                modifier = Modifier.height(20.dp)
            )

            NotificationSectionHeader("Categories")
        }

        // Display notification categories.
        items(categories.size) { index ->

            val category = categories[index]

            NotificationRow(
                category.first,
                category.second,
                onClick = {
                    // TODO: Manage category notification preferences.
                },
                leadingIcon = {
                    Icon(
                        imageVector = category.third,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            if (index < categories.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        item {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }

    // Display the notification settings dialog.
    selectedContactId?.let { contactId ->

        val contactName =
            if (contactId == "ADMIN_SUPPORT") {
                "Admin"
            } else {
                profileNames[contactId] ?: "Campsite Owner"
            }

        NotificationMuteDialog(
            contactName = contactName,
            preference = preferenceController.getPreference(contactId),
            onDismiss = {
                selectedContactId = null
            },
            onConfirm = { option ->

                if (!isSaving) {
                    isSaving = true
                    errorMessage = null
                    selectedContactId = null

                    scope.launch {
                        try {
                            when (option) {

                                MuteOption.UNMUTED ->
                                    preferenceController.unmute(contactId)

                                MuteOption.ONE_HOUR ->
                                    preferenceController.muteForHours(
                                        contactId, 1
                                    )

                                MuteOption.TWENTY_FOUR_HOURS ->
                                    preferenceController.muteForHours(
                                        contactId, 24
                                    )

                                MuteOption.INDEFINITE ->
                                    preferenceController.mute(contactId)
                            }

                        } catch (e: CancellationException) {
                            throw e

                        } catch (e: Exception) {
                            errorMessage =
                                "Failed to update notification settings"

                        } finally {
                            isSaving = false
                        }
                    }
                }
            }
        )
    }
}
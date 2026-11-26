package com.example.lambda_bbb_events.view.ui.user_screens.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.NotificationPreferenceController
import com.example.lambda_bbb_events.model.message.NotificationPreference
import com.example.lambda_bbb_events.view.ui.components.common.ProfilePicture
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
 * notifications from each campsite owner, administrator, or camper.
 *
 * @param contacts People who can message the user, each listed once.
 * @param preferenceController Controller for notification preferences.
 * @param modifier Modifier applied to the settings container.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Changed with the help of this AI tool.
 */
@Composable
fun NotificationSettingsTab(
    contacts: List<MessageContact>,
    preferenceController: NotificationPreferenceController,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var selectedContact by remember {
        mutableStateOf<MessageContact?>(null)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

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
                modifier = Modifier.padding(top = 16.dp)
            )

            NotificationSectionHeader("People")
        }

        errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
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

        if (contacts.isEmpty()) {
            item {
                Text(
                    text = "No contacts available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        items(
            items = contacts,
            key = { "person_${it.contactId}" }
        ) { contact ->

            NotificationRow(
                contact.name,
                muteStatus(preferenceController.getPreference(contact.contactId)),
                onClick = {
                    if (!isSaving) {
                        errorMessage = null
                        selectedContact = contact
                    }
                },
                leadingIcon = {
                    ProfilePicture(imageId = contact.imageId, size = 48.dp)
                }
            )

            if (contact != contacts.last()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        item {
            NotificationSectionHeader("Categories")
        }

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

    selectedContact?.let { contact ->

        val contactId = contact.contactId

        NotificationMuteDialog(
            contactName = contact.name,
            preference = preferenceController.getPreference(contactId),
            onDismiss = {
                selectedContact = null
            },
            onConfirm = { option ->

                if (!isSaving) {
                    isSaving = true
                    errorMessage = null
                    selectedContact = null

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

/**
 * Describes whether notifications from a contact are muted.
 *
 * @param preference The user's notification preference for the contact.
 * @return "Unmuted", "Muted indefinitely", or "Temporarily muted".
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Written with the help of this AI tool.
 */
private fun muteStatus(preference: NotificationPreference): String {
    return when {
        !preference.isMuted() -> "Unmuted"
        preference.mutedUntil == null -> "Muted indefinitely"
        else -> "Temporarily muted"
    }
}

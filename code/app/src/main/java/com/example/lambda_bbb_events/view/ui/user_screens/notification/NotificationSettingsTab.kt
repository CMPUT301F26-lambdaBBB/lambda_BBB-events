package com.example.lambda_bbb_events.view.ui.user_screens.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.NotificationController
import com.example.lambda_bbb_events.controller.NotificationPreferenceController
import com.example.lambda_bbb_events.model.message.NotificationPreference
import com.example.lambda_bbb_events.model.notification.NotificationCategory
import com.example.lambda_bbb_events.model.notification.NotificationMode
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
 * notifications from individual contacts and configure
 * notification delivery modes for system events.
 *
 * @param contacts People who can message the user.
 * @param preferenceController Controller for contact preferences.
 * @param currentUserId Device ID of the current user.
 * @param notificationController Controller for system notifications.
 * @param modifier Modifier applied to the settings container.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Changed with the help of this AI tool.
 * Prompts:
 * - "Now on the notification page as well. Please also remove the profile duplication bug. Also
 *   please fix the way the admins work since it seems all admins share an account. One more thing
 *   is to fix the notification page so that the spacing looks right."
 * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
 *   chats per admin".
 */
@Composable
fun NotificationSettingsTab(
    contacts: List<MessageContact>,
    preferenceController: NotificationPreferenceController,
    currentUserId: String,
    notificationController: NotificationController,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var selectedContact by remember(currentUserId) {
        mutableStateOf<MessageContact?>(null)
    }

    var selectedCategory by remember(currentUserId) {
        mutableStateOf<NotificationCategory?>(null)
    }

    var selectedMode by remember(currentUserId) {
        mutableStateOf(NotificationMode.IMMEDIATE)
    }

    var isSaving by remember(currentUserId) {
        mutableStateOf(false)
    }

    var errorMessage by remember(currentUserId) {
        mutableStateOf<String?>(null)
    }

    // Load saved notification category preferences.
    var categoryModes by remember(
        currentUserId,
        notificationController
    ) {
        mutableStateOf(
            NotificationCategory.entries.associateWith { category ->
                notificationController.getCategoryMode(
                    currentUserId,
                    category
                )
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {

        // People section.
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
                muteStatus(
                    preferenceController.getPreference(
                        contact.contactId
                    )
                ),
                onClick = {
                    if (!isSaving) {
                        errorMessage = null
                        selectedContact = contact
                    }
                },
                leadingIcon = {
                    ProfilePicture(
                        imageId = contact.imageId,
                        size = 48.dp
                    )
                }
            )

            if (contact != contacts.last()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        // Categories section.
        item {
            NotificationSectionHeader("Categories")
        }

        items(
            items = NotificationCategory.entries,
            key = { "category_${it.name}" }
        ) { category ->

            val categoryIcon = when (category) {

                NotificationCategory.REJECTION ->
                    Icons.Outlined.Info

                NotificationCategory.LOTTERY ->
                    Icons.Outlined.ConfirmationNumber

                NotificationCategory.CANCELLATION ->
                    Icons.Outlined.Cancel

                NotificationCategory.GENERAL ->
                    Icons.Outlined.Info
            }

            val currentMode = categoryModes[category]
                ?: NotificationMode.IMMEDIATE

            NotificationRow(
                category.title,
                currentMode.title,
                onClick = {
                    selectedMode = currentMode
                    selectedCategory = category
                },
                leadingIcon = {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            if (category != NotificationCategory.entries.last()) {
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

    // Individual contact notification dialog.
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

    // Notification category settings dialog.
    selectedCategory?.let { category ->

        NotificationCategoryDialog(
            category = category,
            selectedMode = selectedMode,
            onModeSelected = { mode ->
                selectedMode = mode
            },
            onDismiss = {
                selectedCategory = null
            },
            onConfirm = {

                notificationController.setCategoryMode(
                    category = category,
                    mode = selectedMode
                )

                categoryModes = categoryModes + (
                        category to notificationController.getCategoryMode(
                            currentUserId,
                            category
                        )
                        )

                selectedCategory = null
            }
        )
    }
}

/**
 * Displays available notification delivery modes
 * for a selected category.
 */
@Composable
private fun NotificationCategoryDialog(
    category: NotificationCategory,
    selectedMode: NotificationMode,
    onModeSelected: (NotificationMode) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(category.title)
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                NotificationMode.entries.forEach { mode ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onModeSelected(mode)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        RadioButton(
                            selected = selectedMode == mode,
                            onClick = null
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = modeDescription(mode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = onConfirm
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

/**
 * Describes the behaviour of a notification delivery mode.
 */
private fun modeDescription(
    mode: NotificationMode
): String {

    return when (mode) {

        NotificationMode.HIGH_PRIORITY ->
            "Receive important alerts immediately."

        NotificationMode.IMMEDIATE ->
            "Receive notifications as they occur."

        NotificationMode.WEEKLY ->
            "Receive a weekly summary of updates when the app is active."

        NotificationMode.MUTED ->
            "Disable Android alerts for this category."
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
 * Changed with the help of this AI tool.
 * Prompts:
 * - "Now on the notification page as well. Please also remove the profile duplication bug. Also
 *   please fix the way the admins work since it seems all admins share an account. One more thing
 *   is to fix the notification page so that the spacing looks right."
 * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
 *   chats per admin".
 */
private fun muteStatus(
    preference: NotificationPreference
): String {

    return when {

        !preference.isMuted() ->
            "Unmuted"

        preference.mutedUntil == null ->
            "Muted indefinitely"

        else ->
            "Temporarily muted"
    }
}
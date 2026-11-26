package com.example.lambda_bbb_events.view.ui.user_screens.notification

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.MessageController
import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.message.ConversationType
import com.example.lambda_bbb_events.model.message.MessagingAccess
import com.example.lambda_bbb_events.view.ui.components.messaging.ConversationCard
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationRow
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationSectionHeader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Displays existing conversations and available messaging contacts.
 *
 * Users can contact campsite owners and administrators after
 * successfully enrolling in a campsite.
 *
 * @param conversations List of existing conversations.
 * @param messagingAccess List of campsite messaging access records.
 * @param currentUserId Unique identifier of the current user.
 * @param profileNames Map of profile IDs to display names.
 * @param campsiteNames Map of campsite IDs to campsite names.
 * @param messageController Controller responsible for messaging.
 * @param navController Handles navigation to the chat screen.
 * @param modifier Modifier applied to the tab container.
 */
@Composable
fun MessagesTab(
    conversations: List<Conversation>,
    messagingAccess: List<MessagingAccess>,
    currentUserId: String,
    profileNames: Map<String, String>,
    campsiteNames: Map<String, String>,
    messageController: MessageController,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var isOpening by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // Determine available contacts based on campsite enrollment.
    val availableContacts = messagingAccess.flatMap { access ->

        buildList {

            if (
                access.active &&
                access.ownerId.isNotBlank() &&
                access.ownerId != currentUserId
            ) {
                add(access to ConversationType.OWNER)
            }

            add(access to ConversationType.ADMIN)
        }

    }.filter { (access, type) ->

        // Exclude contacts that already have a conversation.
        conversations.none {
            it.campsiteId == access.campsiteId &&
                    it.userId == currentUserId &&
                    it.type == type.name
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {

        errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Display empty state.
        if (
            conversations.isEmpty() &&
            availableContacts.isEmpty()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No conversations available yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Display existing conversations.
        if (conversations.isNotEmpty()) {

            item {
                NotificationSectionHeader("Conversations")
            }

            items(
                items = conversations,
                key = {
                    "conversation_${it.conversationId}"
                }
            ) { conversation ->

                val contactName = when {

                    conversation.userId != currentUserId -> {
                        profileNames[conversation.userId]
                            ?: "Camper"
                    }

                    conversation.type ==
                            ConversationType.ADMIN.name -> {
                        "Admin Support"
                    }

                    else -> {
                        profileNames[conversation.ownerId]
                            ?: "Campsite Owner"
                    }
                }

                val campsiteName =
                    campsiteNames[conversation.campsiteId]
                        ?: "Campsite"

                ConversationCard(
                    conversation = conversation,
                    contactName = contactName,
                    campsiteName = campsiteName,
                    onClick = {
                        if (!isOpening) {
                            val conversationId = Uri.encode(
                                conversation.conversationId
                            )

                            navController.navigate(
                                "chat/$conversationId"
                            )
                        }
                    }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        // Display available contacts without existing conversations.
        if (availableContacts.isNotEmpty()) {

            item {
                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                NotificationSectionHeader("Available Contacts")
            }

            items(
                items = availableContacts,
                key = { (access, type) ->
                    "contact_${access.campsiteId}_${type.name}"
                }
            ) { (access, type) ->

                val contactName =
                    if (type == ConversationType.ADMIN) {
                        "Admin Support"
                    } else {
                        profileNames[access.ownerId]
                            ?: "Campsite Owner"
                    }

                val campsiteName =
                    campsiteNames[access.campsiteId]
                        ?: "Campsite"

                NotificationRow(
                    contactName,
                    campsiteName,
                    onClick = {

                        if (!isOpening) {

                            isOpening = true
                            errorMessage = null

                            scope.launch {
                                try {
                                    val conversationId =
                                        messageController.createConversation(
                                            campsiteId = access.campsiteId,
                                            type = type
                                        )

                                    navController.navigate(
                                        "chat/${Uri.encode(conversationId)}"
                                    )

                                } catch (e: CancellationException) {
                                    throw e

                                } catch (e: Exception) {
                                    errorMessage =
                                        "Failed to open conversation"

                                } finally {
                                    isOpening = false
                                }
                            }
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
}
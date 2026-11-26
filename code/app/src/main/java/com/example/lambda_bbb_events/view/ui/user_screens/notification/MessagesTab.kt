package com.example.lambda_bbb_events.view.ui.user_screens.notification

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.MessageController
import com.example.lambda_bbb_events.view.ui.components.common.ProfilePicture
import com.example.lambda_bbb_events.view.ui.components.messaging.ConversationCard
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationRow
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationSectionHeader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Displays existing conversations and the people the user can start one with, each
 * person listed once.
 *
 * @param conversations People the user already has conversations with.
 * @param available People the user can start a conversation with.
 * @param messageController Controller responsible for messaging.
 * @param navController Handles navigation to the chat screen.
 * @param modifier Modifier applied to the tab container.
 */
@Composable
fun MessagesTab(
    conversations: List<MessageContact>,
    available: List<MessageContact>,
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

    fun openChat(conversationId: String) {
        navController.navigate("chat/${Uri.encode(conversationId)}")
    }

    fun startChat(contact: MessageContact) {
        if (isOpening) return

        isOpening = true
        errorMessage = null

        scope.launch {
            try {
                openChat(messageController.createConversation(contact.contactId, contact.type))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = "Failed to open conversation"
            } finally {
                isOpening = false
            }
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
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        if (conversations.isEmpty() && available.isEmpty()) {
            item {
                EmptyMessages()
            }
        }

        conversationSection(conversations) { contact ->
            contact.conversation?.let { openChat(it.conversationId) }
        }

        availableSection(available, onClick = ::startChat)
    }
}

/**
 * Adds the "Conversations" section listing [contacts], if there are any.
 *
 * @param onClick Called with the contact whose conversation was tapped.
 */
private fun LazyListScope.conversationSection(
    contacts: List<MessageContact>,
    onClick: (MessageContact) -> Unit
) {
    if (contacts.isEmpty()) return

    item {
        NotificationSectionHeader("Conversations")
    }

    items(
        items = contacts,
        key = { "conversation_${it.conversation?.conversationId}" }
    ) { contact ->
        val conversation = contact.conversation ?: return@items

        ConversationCard(
            conversation = conversation,
            contactName = contact.name,
            subtitle = contact.subtitle,
            contactImageId = contact.imageId,
            onClick = { onClick(contact) }
        )

        if (contact != contacts.last()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * Adds the "Available Contacts" section listing [contacts], if there are any.
 *
 * @param onClick Called with the contact that was tapped.
 */
private fun LazyListScope.availableSection(
    contacts: List<MessageContact>,
    onClick: (MessageContact) -> Unit
) {
    if (contacts.isEmpty()) return

    item {
        NotificationSectionHeader("Available Contacts")
    }

    items(
        items = contacts,
        key = { "contact_${it.type.name}_${it.contactId}" }
    ) { contact ->
        NotificationRow(
            contact.name,
            contact.subtitle,
            onClick = { onClick(contact) },
            leadingIcon = {
                ProfilePicture(imageId = contact.imageId, size = 48.dp)
            }
        )

        if (contact != contacts.last()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * Displays the message shown when the user has nobody to message yet.
 */
@Composable
private fun EmptyMessages() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No conversations available yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

package com.example.lambda_bbb_events.view.ui.user_screens.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.MessageController
import com.example.lambda_bbb_events.model.Message
import com.example.lambda_bbb_events.view.ui.components.profile.ShowAlertDialog
import androidx.navigation.NavController
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.messaging.ChatContactCard
import com.example.lambda_bbb_events.view.ui.components.messaging.MessageBubble
import com.example.lambda_bbb_events.view.ui.components.messaging.MessageInputField
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Displays messages belonging to a conversation and allows
 * authorized users to send new messages.
 *
 * Supports private owner conversations and shared administrator
 * conversations. The message listener is started when the screen
 * opens and removed when the screen leaves composition.
 *
 * Long-pressing a message the user may delete asks them to confirm,
 * then deletes it for both people.
 *
 * @param conversationId Unique identifier of the conversation.
 * @param title Display name of the contact, shown on the contact card.
 * @param subtitle Additional information about the conversation.
 * @param currentUserId Unique identifier of the current user.
 * @param messageController Controller responsible for messaging operations.
 * @param navController Controller used for back navigation.
 * @param canSend Whether the current user can send messages.
 * @param contactImageId Profile picture ID of the contact, blank to show the default icon.
 * @param senderNames Map containing sender IDs and display names.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Changed with the help of this AI tool.
 */
@Composable
fun ChatScreen(
    conversationId: String,
    title: String,
    subtitle: String,
    currentUserId: String,
    messageController: MessageController,
    navController: NavController,
    canSend: Boolean = true,
    contactImageId: String = "",
    senderNames: Map<String, String> = emptyMap()
) {

    var content by rememberSaveable(conversationId) {
        mutableStateOf("")
    }

    var isSending by remember(conversationId) {
        mutableStateOf(false)
    }

    var errorMessage by remember(conversationId) {
        mutableStateOf<String?>(null)
    }

    var messageToDelete by remember(conversationId) {
        mutableStateOf<Message?>(null)
    }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    DisposableEffect(conversationId, messageController) {

        messageController.openConversation(conversationId)

        onDispose {
            messageController.closeConversation()
        }
    }

    val messages = messageController.messages

    LaunchedEffect(
        conversationId,
        messages.firstOrNull()?.messageId
    ) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            ScreenTitle(
                title = "Messages",
                navController = navController,
                showBack = true,
                showNotifications = false
            )

            ChatContactCard(
                name = title,
                subtitle = subtitle,
                contactImageId = contactImageId
            )

            if (messages.isEmpty()) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(
                        vertical = 12.dp
                    )
                ) {

                    items(
                        items = messages,
                        key = { it.messageId }
                    ) { message ->

                        MessageBubble(
                            message = message,
                            currentUserId = currentUserId,
                            senderName = senderNames[message.senderId] ?: "",
                            onLongClick = if (messageController.canDelete(message)) {
                                { messageToDelete = message }
                            } else {
                                null
                            }
                        )
                    }
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (canSend) {

                MessageInputField(
                    content = content,
                    onContentChange = {
                        content = it
                    },
                    enabled = !isSending,
                    onSend = {

                        if (!isSending && content.isNotBlank()) {

                            val messageContent = content.trim()

                            isSending = true
                            errorMessage = null

                            scope.launch {

                                try {

                                    messageController.sendMessage(
                                        messageContent
                                    )

                                    content = ""

                                } catch (e: CancellationException) {

                                    throw e

                                } catch (e: Exception) {

                                    errorMessage =
                                        "Failed to send message. Please try again."

                                } finally {

                                    isSending = false
                                }
                            }
                        }
                    }
                )

            } else {

                Text(
                    text = "This conversation is read-only.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    messageToDelete?.let { message ->
        DeleteMessageDialog(
            onConfirm = {
                messageToDelete = null
                errorMessage = null

                scope.launch {
                    errorMessage = deleteMessage(messageController, message)
                }
            },
            onDismiss = { messageToDelete = null }
        )
    }
}

/**
 * Asks the user to confirm deleting a message.
 *
 * @param onConfirm Called when the user confirms.
 * @param onDismiss Called when the user cancels.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Written with the help of this AI tool.
 */
@Composable
private fun DeleteMessageDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ShowAlertDialog(
        dialogLabel = "Delete message?",
        dialogCaption = "This message will be deleted for everyone in the conversation. " +
                "\"${Message.DELETED_TEXT}\" will be shown in its place.",
        confirmButton = onConfirm,
        confirmText = "Delete",
        dismissState = onDismiss,
        cancelText = "Cancel"
    )
}

/**
 * Deletes [message] from the open conversation.
 *
 * @return An error to show the user, or `null` if the message was deleted.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Written with the help of this AI tool.
 */
private suspend fun deleteMessage(
    messageController: MessageController,
    message: Message
): String? {
    return try {
        messageController.deleteMessage(message.messageId)
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        "Failed to delete message. Please try again."
    }
}

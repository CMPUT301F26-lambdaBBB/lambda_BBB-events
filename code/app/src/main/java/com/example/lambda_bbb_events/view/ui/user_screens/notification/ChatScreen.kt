package com.example.lambda_bbb_events.view.ui.user_screens.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.MessageController
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
 * @param conversationId Unique identifier of the conversation.
 * @param title Display name of the conversation.
 * @param subtitle Additional information about the conversation.
 * @param currentUserId Unique identifier of the current user.
 * @param messageController Controller responsible for messaging operations.
 * @param onBack Callback invoked when the back button is pressed.
 * @param canSend Whether the current user can send messages.
 * @param senderNames Map containing sender IDs and display names.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    title: String,
    subtitle: String,
    currentUserId: String,
    messageController: MessageController,
    onBack: () -> Unit,
    canSend: Boolean = true,
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
        modifier = Modifier.imePadding(),

        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                        }
                    }
                },

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

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
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
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
                            senderName = senderNames[message.senderId] ?: ""
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

            HorizontalDivider()

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
}
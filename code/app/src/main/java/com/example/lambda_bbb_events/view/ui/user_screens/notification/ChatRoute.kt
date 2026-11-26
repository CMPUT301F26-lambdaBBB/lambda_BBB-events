package com.example.lambda_bbb_events.view.ui.user_screens.notification

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.message.ConversationType
import com.example.lambda_bbb_events.model.repository.MessageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Loads conversation information and displays the chat screen.
 *
 * Only the two people in the conversation can open it. In owner
 * conversations, reservation and lottery eligibility is checked
 * periodically to determine whether messages can be sent.
 *
 * @param navController Handles screen navigation.
 * @param controllers Shared application controllers.
 * @param conversationId Unique identifier of the conversation.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Changed with the help of this AI tool.
 * Prompts:
 * - "Hey could you fix the view for the messages. It seems a bit weird with a fully non rounded
 *   top area and the fact you can't press enter to send a message and also because the profile
 *   photo isn't shown."
 * - "Now on the notification page as well. Please also remove the profile duplication bug. Also
 *   please fix the way the admins work since it seems all admins share an account. One more thing
 *   is to fix the notification page so that the spacing looks right."
 * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
 *   chats per admin".
 */
@Composable
fun ChatRoute(
    navController: NavController,
    controllers: AppControllers,
    conversationId: String
) {
    val currentUser = controllers.users.currentUser

    if (currentUser == null) {
        Text("Please sign in to continue.")
        return
    }

    val currentUserId = currentUser.deviceId

    var conversation by remember(
        conversationId,
        currentUserId
    ) {
        mutableStateOf<Conversation?>(null)
    }

    var isLoading by remember(
        conversationId,
        currentUserId
    ) {
        mutableStateOf(true)
    }

    LaunchedEffect(conversationId, currentUserId) {
        try {
            conversation = controllers.messages.getConversation(
                conversationId
            )

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            conversation = null

            Log.e(
                "ChatRoute",
                "Failed to load conversation",
                e
            )

        } finally {
            isLoading = false
        }
    }

    val chat = conversation

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        chat == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Unable to load conversation.")
            }
        }

        else -> {
            OpenChat(
                chat = chat,
                currentUserId = currentUserId,
                controllers = controllers,
                navController = navController
            )
        }
    }
}

/**
 * Displays a loaded conversation with the other participant's name and picture.
 *
 * @param chat The conversation to display.
 * @param currentUserId Device ID of the current user.
 * @param controllers Shared application controllers.
 * @param navController Handles screen navigation.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Written with the help of this AI tool.
 * Prompts:
 * - "Hey could you fix the view for the messages. It seems a bit weird with a fully non rounded
 *   top area and the fact you can't press enter to send a message and also because the profile
 *   photo isn't shown."
 * - "Now on the notification page as well. Please also remove the profile duplication bug. Also
 *   please fix the way the admins work since it seems all admins share an account. One more thing
 *   is to fix the notification page so that the spacing looks right."
 * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
 *   chats per admin".
 */
@Composable
private fun OpenChat(
    chat: Conversation,
    currentUserId: String,
    controllers: AppControllers,
    navController: NavController
) {
    val type = ConversationType.valueOf(chat.type)
    val isCamperSide = chat.userId == currentUserId
    val roleLabel = contactRoleLabel(type, isCamperSide)
    val contact = controllers.users.getCachedUser(chat.contactIdFor(currentUserId))

    ChatScreen(
        conversationId = chat.conversationId,
        title = contact?.name?.takeIf { it.isNotBlank() } ?: roleLabel,
        subtitle = roleLabel,
        currentUserId = currentUserId,
        messageController = controllers.messages,
        navController = navController,
        canSend = rememberCanSend(chat, currentUserId),
        contactImageId = contact?.profilePictureImageId.orEmpty()
    )
}

/**
 * Remembers whether [currentUserId] can send messages in [chat].
 *
 * Both people in an admin conversation can always send. In an owner conversation the camper
 * must still have an eligible reservation or lottery enrollment at one of the owner's
 * campsites, which is checked again every minute.
 *
 * @param chat The open conversation.
 * @param currentUserId Device ID of the current user.
 * @return Whether sending is allowed right now.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Written with the help of this AI tool.
 * Prompts:
 * - "Now on the notification page as well. Please also remove the profile duplication bug. Also
 *   please fix the way the admins work since it seems all admins share an account. One more thing
 *   is to fix the notification page so that the spacing looks right."
 * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
 *   chats per admin".
 */
@Composable
private fun rememberCanSend(chat: Conversation, currentUserId: String): Boolean {
    val isParticipant = chat.hasParticipant(currentUserId)
    val isOwnerChat = chat.type == ConversationType.OWNER.name

    var isEligible by remember(chat.conversationId, currentUserId) {
        mutableStateOf(false)
    }

    LaunchedEffect(chat.conversationId, currentUserId) {
        if (!isParticipant || !isOwnerChat) return@LaunchedEffect

        val messageRepository = MessageRepository(chat.conversationId)
        while (true) {
            isEligible = checkOwnerEligibility(messageRepository, chat)
            delay(60_000L)
        }
    }

    return isParticipant && (!isOwnerChat || isEligible)
}

/**
 * Checks whether the camper in [chat] can still message its campsite owner.
 *
 * @return `false` if they cannot, or if the check failed.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Written with the help of this AI tool.
 * Prompts:
 * - "Now on the notification page as well. Please also remove the profile duplication bug. Also
 *   please fix the way the admins work since it seems all admins share an account. One more thing
 *   is to fix the notification page so that the spacing looks right."
 * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
 *   chats per admin".
 */
private suspend fun checkOwnerEligibility(
    messageRepository: MessageRepository,
    chat: Conversation
): Boolean {
    return try {
        messageRepository.hasEligibleOwnerAccess(
            entrantId = chat.userId,
            ownerId = chat.ownerId
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("ChatRoute", "Failed to check messaging eligibility", e)
        false
    }
}

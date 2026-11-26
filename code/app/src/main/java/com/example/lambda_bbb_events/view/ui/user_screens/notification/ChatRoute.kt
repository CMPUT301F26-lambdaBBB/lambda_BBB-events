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
import com.example.lambda_bbb_events.model.repository.MessagingAccessRepository
import com.example.lambda_bbb_events.model.user.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Loads conversation information and displays the chat screen.
 *
 * Observes messaging access and periodically checks reservation
 * and lottery eligibility to determine whether the current
 * user can send messages within the conversation.
 *
 * @param navController Handles screen navigation.
 * @param controllers Shared application controllers.
 * @param conversationId Unique identifier of the conversation.
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
    val isAdmin = currentUser.role == UserRole.ADMIN

    var conversation by remember(
        conversationId,
        currentUserId,
        isAdmin
    ) {
        mutableStateOf<Conversation?>(null)
    }

    var isLoading by remember(
        conversationId,
        currentUserId,
        isAdmin
    ) {
        mutableStateOf(true)
    }

    LaunchedEffect(conversationId, currentUserId, isAdmin) {
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

            val accessRepository = remember(
                chat.userId,
                chat.campsiteId,
                chat.type
            ) {
                if (chat.type == ConversationType.OWNER.name) {
                    MessagingAccessRepository(chat.userId)
                } else {
                    null
                }
            }

            DisposableEffect(accessRepository) {
                accessRepository?.startListening()

                onDispose {
                    accessRepository?.stopListening()
                }
            }

            // Check whether the current user participates
            // in the owner conversation.
            val isParticipant =
                currentUserId == chat.userId ||
                        currentUserId == chat.ownerId

            // Check the stored messaging access.
            val hasOwnerAccess =
                chat.type == ConversationType.OWNER.name &&
                        isParticipant &&
                        accessRepository?.messagingAccess?.any {
                            it.campsiteId == chat.campsiteId &&
                                    it.ownerId == chat.ownerId &&
                                    it.active
                        } == true

            val messageRepository = remember(conversationId) {
                MessageRepository(conversationId)
            }

            var isEligible by remember(
                conversationId,
                currentUserId,
                hasOwnerAccess
            ) {
                mutableStateOf(false)
            }

            // Periodically verify that the entrant still has
            // an eligible reservation or lottery enrollment.
            LaunchedEffect(
                conversationId,
                currentUserId,
                hasOwnerAccess
            ) {
                isEligible = false

                if (hasOwnerAccess) {
                    while (true) {
                        try {
                            isEligible =
                                messageRepository.hasEligibleEnrollment(
                                    entrantId = chat.userId,
                                    campsiteId = chat.campsiteId,
                                    ownerId = chat.ownerId
                                )

                        } catch (e: CancellationException) {
                            throw e

                        } catch (e: Exception) {
                            isEligible = false

                            Log.e(
                                "ChatRoute",
                                "Failed to check messaging eligibility",
                                e
                            )
                        }

                        delay(60_000L)
                    }
                }
            }

            // Determine whether sending messages is allowed.
            val canSend = when (chat.type) {

                ConversationType.OWNER.name -> {
                    hasOwnerAccess && isEligible
                }

                ConversationType.ADMIN.name -> {
                    currentUserId == chat.userId || isAdmin
                }

                else -> false
            }

            val title = when {

                chat.userId != currentUserId -> {
                    controllers.users
                        .getCachedUser(chat.userId)?.name
                        ?: "Camper"
                }

                chat.type == ConversationType.ADMIN.name -> {
                    "Admin Support"
                }

                else -> {
                    controllers.users
                        .getCachedUser(chat.ownerId)?.name
                        ?: "Campsite Owner"
                }
            }

            ChatScreen(
                conversationId = conversationId,
                title = title,
                subtitle = "Campsite conversation",
                currentUserId = currentUserId,
                messageController = controllers.messages,
                onBack = { navController.popBackStack() },
                canSend = canSend
            )
        }
    }
}
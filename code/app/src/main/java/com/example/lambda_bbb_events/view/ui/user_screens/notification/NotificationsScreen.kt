package com.example.lambda_bbb_events.view.ui.user_screens.notification

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.MessageController
import com.example.lambda_bbb_events.controller.NotificationPreferenceController
import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.repository.MessagingAccessRepository
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

/**
 * Displays the user's conversations and notification settings.
 *
 * Supports communication between entrants, campsite owners,
 * and administrators.
 *
 * @param navController Handles screen navigation.
 * @param currentUserId Device ID of the current user.
 * @param messageController Controller for messaging operations.
 * @param preferenceController Controller for notification preferences.
 * @param profiles Every known user by device ID, for names and profile pictures.
 * @param campsiteNames Map of campsite IDs to campsite names.
 * @param isAdmin Whether the current user is an administrator.
 *
 * Reference:
 * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
 * https://claude.com/claude-code
 * Changed with the help of this AI tool.
 */
@Composable
fun NotificationsScreen(
    navController: NavController,
    currentUserId: String,
    messageController: MessageController,
    preferenceController: NotificationPreferenceController,
    profiles: Map<String, User> = emptyMap(),
    campsiteNames: Map<String, String> = emptyMap(),
    isAdmin: Boolean = false
) {

    var selectedTab by rememberSaveable(currentUserId) {
        mutableIntStateOf(0)
    }

    val tabs = listOf("Messages", "Settings")

    var conversations by remember(currentUserId, isAdmin) {
        mutableStateOf<List<Conversation>>(emptyList())
    }

    var isLoading by remember(currentUserId, isAdmin) {
        mutableStateOf(true)
    }

    var errorMessage by remember(currentUserId, isAdmin) {
        mutableStateOf<String?>(null)
    }

    // Repository for the current user's messaging access.
    val messagingAccessRepository = remember(currentUserId) {
        MessagingAccessRepository(currentUserId)
    }

    val messagingAccess =
        messagingAccessRepository.messagingAccess

    // Synchronize messaging access with Firestore.
    DisposableEffect(messagingAccessRepository) {

        messagingAccessRepository.startListening()

        onDispose {
            messagingAccessRepository.stopListening()
        }
    }

    // Synchronize notification preferences with Firestore.
    DisposableEffect(preferenceController, currentUserId) {

        preferenceController.startListening()

        onDispose {
            preferenceController.stopListening()
        }
    }

    // Observe conversations associated with the current user.
    LaunchedEffect(currentUserId, isAdmin, messageController) {

        isLoading = true
        errorMessage = null

        try {

            val userConversations =
                messageController.observeUserConversations()

            val ownerConversations =
                messageController.observeOwnerConversations()

            val adminConversations =
                if (isAdmin) {
                    messageController.observeAdminConversations()
                } else {
                    flowOf(emptyList<Conversation>())
                }

            combine(
                userConversations,
                ownerConversations,
                adminConversations
            ) { userChats, ownerChats, adminChats ->

                (userChats + ownerChats + adminChats)
                    .distinctBy { it.conversationId }
                    .sortedByDescending { it.updatedAt }

            }.collect { updatedConversations ->

                conversations = updatedConversations
                isLoading = false
                errorMessage = null
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            errorMessage =
                e.message ?: "Failed to load conversations"

            isLoading = false

            Log.e(
                "NotificationsScreen",
                "Failed to load conversations",
                e
            )
        }
    }

    val conversationContacts = conversationContacts(
        conversations, messagingAccess, currentUserId, profiles, campsiteNames
    )

    val availableContacts = availableContacts(
        conversations, messagingAccess, currentUserId, profiles, campsiteNames
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            ScreenTitle(
                title = "Notification",
                navController = navController,
                showNotifications = false,
                showBack = true
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor =
                    MaterialTheme.colorScheme.background,
                contentColor =
                    MaterialTheme.colorScheme.primary
            ) {

                tabs.forEachIndexed { index, title ->

                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        },
                        text = {
                            Text(
                                text = title,
                                style =
                                    MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }
            }

            when (selectedTab) {

                // Messages tab.
                0 -> {

                    when {

                        isLoading -> {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        errorMessage != null -> {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color =
                                        MaterialTheme.colorScheme.error,
                                    style =
                                        MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        else -> {

                            MessagesTab(
                                conversations = conversationContacts,
                                available = availableContacts,
                                messageController = messageController,
                                navController = navController,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Notification settings tab.
                1 -> {

                    NotificationSettingsTab(
                        contacts = (conversationContacts + availableContacts)
                            .distinctBy { it.contactId },
                        preferenceController = preferenceController,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
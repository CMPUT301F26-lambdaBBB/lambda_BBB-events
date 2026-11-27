package com.example.lambda_bbb_events.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lambda_bbb_events.model.Message
import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.message.ConversationType
import com.example.lambda_bbb_events.model.repository.MessageRepository
import com.example.lambda_bbb_events.model.repository.ConversationRepository
import com.example.lambda_bbb_events.model.user.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Controls messaging operations between the user interface
 * and messaging repositories.
 *
 * Manages conversation creation, message sending,
 * conversation retrieval, and message synchronization.
 *
 * Uses the existing user controller to identify the
 * current user and their role.
 *
 * @param userController Controller responsible for user operations.
 * @param conversationRepository Repository responsible
 * for managing conversations.
 */
class MessageController(
    private val userController: UserController,
    private val conversationRepository: ConversationRepository =
        ConversationRepository()
) {

    private var messageRepository by
    mutableStateOf<MessageRepository?>(null)

    /**
     * Device ID of the currently signed-in user.
     *
     * @throws IllegalStateException If no user is signed in.
     */
    private val currentUserId: String
        get() = userController.user.deviceId

    /**
     * Whether the currently signed-in user is an administrator.
     */
    private val isAdmin: Boolean
        get() = userController.currentUser?.role == UserRole.ADMIN

    /**
     * Messages belonging to the currently opened conversation,
     * ordered from newest to oldest.
     */
    val messages: List<Message>
        get() = messageRepository?.messages ?: emptyList()

    /**
     * Creates or retrieves the current user's conversation with a contact.
     *
     * @param contactId Device ID of the campsite owner or administrator.
     * @param type Type of conversation.
     * @return Unique identifier of the conversation.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Changed with the help of this AI tool.
     * Prompts:
     * - "Now on the notification page as well. Please also remove the profile duplication bug.
     *   Also please fix the way the admins work since it seems all admins share an account. One
     *   more thing is to fix the notification page so that the spacing looks right."
     * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
     *   chats per admin".
     */
    suspend fun createConversation(
        contactId: String,
        type: ConversationType
    ): String {
        return conversationRepository.createConversation(
            contactId = contactId,
            type = type,
            userId = currentUserId
        )
    }

    /**
     * Creates or retrieves the current owner's conversation with a camper, so owners can
     * message campers instead of only replying to them (US 02.12.01). It is the same
     * conversation the camper would start with the owner.
     *
     * @param camperId Device ID of the camper.
     * @return Unique identifier of the conversation.
     * @throws IllegalStateException If the camper can no longer message the current owner.
     */
    suspend fun startConversationAsOwner(camperId: String): String {
        return conversationRepository.createConversation(
            contactId = currentUserId,
            type = ConversationType.OWNER,
            userId = camperId
        )
    }

    /**
     * Creates or retrieves the current administrator's conversation with a user,
     * so the administrator can message them from their profile.
     *
     * @param userId Device ID of the user to message.
     * @return Unique identifier of the conversation.
     * @throws IllegalStateException If the current user is not an administrator.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Okay so there are no conversations yet so can I have admins be able to message users
     *   from the profile admin screen"
     */
    suspend fun startConversationWithUser(userId: String): String {
        check(isAdmin) {
            "Administrator access required"
        }

        return conversationRepository.startAdminConversation(
            userId = userId,
            adminId = currentUserId
        )
    }

    /**
     * Opens a conversation and starts synchronizing its messages.
     *
     * Stops the listener associated with the previous conversation.
     *
     * Should only be called after conversation access is checked.
     *
     * @param conversationId Unique identifier of the conversation.
     */
    fun openConversation(conversationId: String) {
        check(userController.currentUser != null) {
            "No signed-in user"
        }

        closeConversation()

        messageRepository = MessageRepository(conversationId)

        messageRepository?.startListening()
    }

    /**
     * Closes the current conversation and removes its listener.
     */
    fun closeConversation() {
        messageRepository?.stopListening()

        messageRepository = null
    }

    /**
     * Sends a message to the currently opened conversation.
     *
     * @param content Text content of the message.
     * @throws IllegalStateException If no conversation is open
     * or no user is signed in.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Changed with the help of this AI tool.
     * Prompts:
     * - "Now on the notification page as well. Please also remove the profile duplication bug.
     *   Also please fix the way the admins work since it seems all admins share an account. One
     *   more thing is to fix the notification page so that the spacing looks right."
     * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
     *   chats per admin".
     */
    suspend fun sendMessage(content: String) {
        val repository = messageRepository
            ?: error("No conversation is currently open")

        repository.sendMessage(
            content = content,
            senderId = currentUserId
        )
    }

    /**
     * Deletes a message from the currently opened conversation for both people.
     *
     * Users can delete their own messages, and administrators can delete any
     * message in their conversations.
     *
     * @param messageId Unique identifier of the message.
     * @throws IllegalStateException If no conversation is open
     * or the current user may not delete the message.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Also please clean up a database and offer a way for users to delete messages with a
     *   message deleted plus. Show also that messages are active."
     * - "So to clarify I want to allow users and admins to remove messages."
     */
    suspend fun deleteMessage(messageId: String) {
        val repository = messageRepository
            ?: error("No conversation is currently open")

        repository.deleteMessage(
            messageId = messageId,
            requesterId = currentUserId,
            isAdmin = isAdmin
        )
    }

    /**
     * Whether the current user may delete [message].
     *
     * @param message The message to check.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     * Prompts:
     * - "Also please clean up a database and offer a way for users to delete messages with a
     *   message deleted plus. Show also that messages are active."
     * - "So to clarify I want to allow users and admins to remove messages."
     */
    fun canDelete(message: Message): Boolean {
        val user = userController.currentUser ?: return false
        return message.canBeDeletedBy(user.deviceId, isAdmin)
    }

    /**
     * Retrieves information about an accessible conversation.
     *
     * @param conversationId Unique identifier of the conversation.
     * @return Conversation object or null if unavailable.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Changed with the help of this AI tool.
     * Prompts:
     * - "Now on the notification page as well. Please also remove the profile duplication bug.
     *   Also please fix the way the admins work since it seems all admins share an account. One
     *   more thing is to fix the notification page so that the spacing looks right."
     * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
     *   chats per admin".
     */
    suspend fun getConversation(
        conversationId: String
    ): Conversation? {
        return conversationRepository.getConversation(
            conversationId = conversationId,
            userId = currentUserId
        )
    }

    /**
     * Retrieves conversations belonging to the current camper.
     *
     * @return List of camper conversations.
     */
    suspend fun getUserConversations(): List<Conversation> {
        return conversationRepository.getUserConversations(
            userId = currentUserId
        )
    }

    /**
     * Retrieves conversations addressed to the current owner.
     *
     * @return List of owner conversations.
     */
    suspend fun getOwnerConversations(): List<Conversation> {
        return conversationRepository.getOwnerConversations(
            ownerId = currentUserId
        )
    }

    /**
     * Retrieves conversations addressed to the current administrator.
     *
     * @return The administrator's own conversations, or none if
     * the current user is not an administrator.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Changed with the help of this AI tool.
     * Prompts:
     * - "Now on the notification page as well. Please also remove the profile duplication bug.
     *   Also please fix the way the admins work since it seems all admins share an account. One
     *   more thing is to fix the notification page so that the spacing looks right."
     * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
     *   chats per admin".
     */
    suspend fun getAdminConversations(): List<Conversation> {
        if (!isAdmin) return emptyList()

        return conversationRepository.getAdminConversations(
            adminId = currentUserId
        )
    }

    /**
     * Observes camper conversations in real time.
     *
     * @return Flow of camper conversations.
     */
    fun observeUserConversations() =
        conversationRepository.observeUserConversations(
            userId = currentUserId
        )

    /**
     * Observes campsite owner conversations in real time.
     *
     * @return Flow of owner conversations.
     */
    fun observeOwnerConversations() =
        conversationRepository.observeOwnerConversations(
            ownerId = currentUserId
        )

    /**
     * Observes conversations addressed to the current administrator in real time.
     *
     * @return Flow of the administrator's own conversations.
     * @throws IllegalStateException If the current user is not an administrator.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Changed with the help of this AI tool.
     * Prompts:
     * - "Now on the notification page as well. Please also remove the profile duplication bug.
     *   Also please fix the way the admins work since it seems all admins share an account. One
     *   more thing is to fix the notification page so that the spacing looks right."
     * - Answers chosen when the AI asked how to do this: "Merge repeated contacts" and "Separate
     *   chats per admin".
     */
    fun observeAdminConversations(): Flow<List<Conversation>> {
        check(isAdmin) {
            "Administrator access required"
        }

        return conversationRepository.observeAdminConversations(
            adminId = currentUserId
        )
    }
}
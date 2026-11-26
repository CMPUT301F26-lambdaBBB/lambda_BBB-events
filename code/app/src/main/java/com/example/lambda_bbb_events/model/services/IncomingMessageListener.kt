package com.example.lambda_bbb_events.model.services

import android.content.Context
import android.util.Log
import com.example.lambda_bbb_events.controller.MessageController
import com.example.lambda_bbb_events.controller.MessageNotificationController
import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.Message
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Listens for incoming chat messages while the application
 * is running and triggers Android notifications.
 *
 * @param messageController Controller for messaging operations.
 * @param notificationController Controller for message notifications.
 * @param db Firestore database instance.
 */
class IncomingMessageListener(
    private val messageController: MessageController,
    private val notificationController: MessageNotificationController,
    private val db: FirebaseFirestore = Firebase.firestore
) {

    /**
     * Observes conversations associated with the current user
     * and starts listening for new messages.
     *
     * Listeners are removed when the coroutine is cancelled.
     *
     * @param context Android application context.
     * @param isAdmin Whether the current user is an administrator.
     */
    suspend fun listen(
        context: Context,
        isAdmin: Boolean
    ) = coroutineScope {

        val startedAt = Timestamp.now()
        val listeners = mutableMapOf<String, Job>()

        val notifiedMessages = mutableSetOf<String>()

        val adminConversations =
            if (isAdmin) {
                messageController.observeAdminConversations()
            } else {
                flowOf(emptyList<Conversation>())
            }

        combine(
            messageController.observeUserConversations(),
            messageController.observeOwnerConversations(),
            adminConversations
        ) { userChats, ownerChats, adminChats ->

            (userChats + ownerChats + adminChats)
                .distinctBy { it.conversationId }

        }.collect { conversations ->

            val activeIds = conversations
                .map { it.conversationId }
                .toSet()

            // Remove listeners for unavailable conversations.
            val removedIds = listeners.keys - activeIds

            removedIds.forEach { conversationId ->
                listeners.remove(conversationId)?.cancel()
            }

            // Start listeners for newly available conversations.
            conversations.forEach { conversation ->

                val conversationId = conversation.conversationId

                if (conversationId !in listeners) {

                    listeners[conversationId] = launch {

                        try {

                            observeMessages(
                                conversationId,
                                startedAt
                            ).collect { snapshot ->

                                if (snapshot.metadata.isFromCache) {
                                    return@collect
                                }

                                snapshot.documents.forEach { document ->

                                    if (document.metadata.hasPendingWrites()) {
                                        return@forEach
                                    }

                                    val message = document.toObject(
                                        Message::class.java
                                    ) ?: return@forEach

                                    val messageKey =
                                        "$conversationId/${document.id}"

                                    if (notifiedMessages.add(messageKey)) {

                                        notificationController.showNotification(
                                            context,
                                            conversation,
                                            message
                                        )
                                    }
                                }
                            }

                        } catch (e: CancellationException) {
                            throw e

                        } catch (e: Exception) {

                            Log.e(
                                "IncomingMessageListener",
                                "Failed to observe incoming messages",
                                e
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Observes messages created after the listener started.
     *
     * @param conversationId Unique conversation identifier.
     * @param startedAt Time when the listener started.
     * @return Flow of Firestore message snapshots.
     */
    private fun observeMessages(
        conversationId: String,
        startedAt: Timestamp
    ) = callbackFlow<QuerySnapshot> {

        val registration = db
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .whereGreaterThanOrEqualTo("timestamp", startedAt)
            .addSnapshotListener(
                MetadataChanges.INCLUDE
            ) { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    trySend(snapshot)
                }
            }

        awaitClose {
            registration.remove()
        }
    }
}
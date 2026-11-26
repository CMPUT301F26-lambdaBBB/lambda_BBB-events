package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.message.ConversationType
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Manages conversations stored in Firebase Firestore.
 *
 * Provides functionality for creating and retrieving
 * conversations for campers, campsite owners, and administrators.
 *
 * @param db Firestore database instance.
 */
class ConversationRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {

    private val conversationsRef =
        db.collection("conversations")

    private val messagingAccessRef =
        db.collection("messagingAccess")

    /**
     * Creates a conversation associated with a campsite.
     *
     * A deterministic conversation ID prevents duplicate
     * conversations for the same camper, campsite, and type.
     * Existing conversations are preserved.
     *
     * @param campsiteId Unique identifier of the campsite.
     * @param type Type of conversation.
     * @param userId Device ID of the camper.
     * @return Unique identifier of the conversation.
     * @throws IllegalStateException If enrollment is not found
     * or owner messaging is unavailable.
     */
    suspend fun createConversation(
        campsiteId: String,
        type: ConversationType,
        userId: String
    ): String {

        require(userId.isNotBlank()) {
            "User ID cannot be empty"
        }

        require(campsiteId.isNotBlank()) {
            "Campsite ID cannot be empty"
        }

        val accessRef = messagingAccessRef
            .document(userId)
            .collection("campsites")
            .document(campsiteId)

        val conversationId =
            "${campsiteId}_${userId}_${type.name}"

        val conversationRef =
            conversationsRef.document(conversationId)

        db.runTransaction { transaction ->

            val access = transaction.get(accessRef)

            check(access.exists()) {
                "User has not enrolled in this campsite"
            }

            val ownerId = if (type == ConversationType.OWNER) {

                check(access.getBoolean("active") == true) {
                    "Owner messaging is unavailable"
                }

                val id = access.getString("ownerId")
                    ?: error("Campsite owner not found")

                check(id.isNotBlank() && id != userId) {
                    "Invalid campsite owner"
                }

                id

            } else {
                ""
            }

            val existing = transaction.get(conversationRef)

            if (!existing.exists()) {

                val conversation = mapOf(
                    "conversationId" to conversationId,
                    "campsiteId" to campsiteId,
                    "userId" to userId,
                    "ownerId" to ownerId,
                    "type" to type.name,
                    "lastMessage" to "",
                    "lastMessageId" to "",
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                transaction.set(
                    conversationRef,
                    conversation
                )

            } else {
                check(existing.getString("ownerId") == ownerId) {
                    "Conversation owner has changed"
                }
            }

            conversationId

        }.await()

        return conversationId
    }

    /**
     * Retrieves a conversation available to the current user.
     *
     * @param conversationId Unique identifier of the conversation.
     * @param userId Device ID of the current user.
     * @param isAdmin Whether the current user is an administrator.
     * @return Conversation object or null if unavailable.
     */
    suspend fun getConversation(
        conversationId: String,
        userId: String,
        isAdmin: Boolean = false
    ): Conversation? {

        require(userId.isNotBlank())

        val conversation = conversationsRef
            .document(conversationId)
            .get()
            .await()
            .toObject(Conversation::class.java)
            ?: return null

        val canAccess = when (conversation.type) {

            ConversationType.OWNER.name -> {
                conversation.userId == userId ||
                        conversation.ownerId == userId
            }

            ConversationType.ADMIN.name -> {
                conversation.userId == userId || isAdmin
            }

            else -> false
        }

        return if (canAccess) {
            conversation
        } else {
            null
        }
    }

    /**
     * Retrieves conversations belonging to a camper.
     *
     * @param userId Device ID of the camper.
     * @return List of the camper's conversations.
     */
    suspend fun getUserConversations(
        userId: String
    ): List<Conversation> {

        require(userId.isNotBlank())

        return conversationsRef
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(Conversation::class.java)
            .sortedByDescending { it.updatedAt }
    }

    /**
     * Retrieves conversations addressed to a campsite owner.
     *
     * @param ownerId Device ID of the campsite owner.
     * @return List of the owner's conversations.
     */
    suspend fun getOwnerConversations(
        ownerId: String
    ): List<Conversation> {

        require(ownerId.isNotBlank())

        return conversationsRef
            .whereEqualTo("ownerId", ownerId)
            .whereEqualTo("type", ConversationType.OWNER.name)
            .get()
            .await()
            .toObjects(Conversation::class.java)
            .sortedByDescending { it.updatedAt }
    }

    /**
     * Retrieves all shared administrator conversations.
     *
     * @param isAdmin Whether the current user is an administrator.
     * @return List of shared administrator conversations.
     * @throws IllegalStateException If the user is not an administrator.
     */
    suspend fun getAdminConversations(
        isAdmin: Boolean
    ): List<Conversation> {

        check(isAdmin) {
            "Administrator access required"
        }

        return conversationsRef
            .whereEqualTo("type", ConversationType.ADMIN.name)
            .get()
            .await()
            .toObjects(Conversation::class.java)
            .sortedByDescending { it.updatedAt }
    }

    /**
     * Observes a camper's conversations in real time.
     *
     * @param userId Device ID of the camper.
     * @return Flow containing the camper's conversations.
     */
    fun observeUserConversations(
        userId: String
    ): Flow<List<Conversation>> {

        require(userId.isNotBlank())

        return observeConversations(
            conversationsRef
                .whereEqualTo("userId", userId)
        )
    }

    /**
     * Observes conversations addressed to a campsite owner.
     *
     * @param ownerId Device ID of the campsite owner.
     * @return Flow containing the owner's conversations.
     */
    fun observeOwnerConversations(
        ownerId: String
    ): Flow<List<Conversation>> {

        require(ownerId.isNotBlank())

        return observeConversations(
            conversationsRef
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo(
                    "type",
                    ConversationType.OWNER.name
                )
        )
    }

    /**
     * Observes shared administrator conversations in real time.
     *
     * @param isAdmin Whether the current user is an administrator.
     * @return Flow containing administrator conversations.
     * @throws IllegalStateException If the user is not an administrator.
     */
    fun observeAdminConversations(
        isAdmin: Boolean
    ): Flow<List<Conversation>> {

        check(isAdmin) {
            "Administrator access required"
        }

        return observeConversations(
            conversationsRef
                .whereEqualTo(
                    "type",
                    ConversationType.ADMIN.name
                )
        )
    }

    /**
     * Observes conversations matching a Firestore query.
     *
     * Removes the snapshot listener when the Flow
     * collection is cancelled.
     *
     * @param query Firestore query defining conversations.
     * @return Flow containing conversations ordered by recent activity.
     */
    private fun observeConversations(
        query: Query
    ): Flow<List<Conversation>> = callbackFlow {

        val listener = query.addSnapshotListener { snapshot, error ->

            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            try {

                val conversations = snapshot
                    ?.toObjects(Conversation::class.java)
                    ?.sortedByDescending { it.updatedAt }
                    ?: emptyList()

                trySend(conversations)

            } catch (e: Exception) {
                close(e)
            }
        }

        awaitClose {
            listener.remove()
        }
    }
}
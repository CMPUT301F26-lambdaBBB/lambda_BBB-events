package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.message.ConversationType
import com.example.lambda_bbb_events.model.user.UserRole
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Manages conversations stored in Firebase Firestore.
 *
 * Provides functionality for creating and retrieving private conversations between
 * campers and campsite owners or individual administrators. Only conversations that use
 * the one-per-pair ID scheme are returned, see [Conversation.hasCurrentId].
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

    private val usersRef =
        db.collection("users")

    /**
     * Creates or retrieves the conversation between a camper and a contact.
     *
     * The conversation ID is built from the two people, so each pair has exactly one
     * conversation and existing conversations are preserved.
     *
     * @param contactId Device ID of the campsite owner or administrator.
     * @param type Type of conversation.
     * @param userId Device ID of the camper.
     * @return Unique identifier of the conversation.
     * @throws IllegalStateException If the camper may not message the contact.
     */
    suspend fun createConversation(
        contactId: String,
        type: ConversationType,
        userId: String
    ): String {

        require(userId.isNotBlank()) {
            "User ID cannot be empty"
        }

        require(contactId.isNotBlank() && contactId != userId) {
            "Invalid contact"
        }

        val campsiteId = when (type) {
            ConversationType.OWNER -> findOwnerCampsite(userId, contactId)
            ConversationType.ADMIN -> findAdminCampsite(userId, contactId)
        }

        val conversationId = when (type) {
            ConversationType.OWNER -> Conversation.ownerConversationId(userId, contactId)
            ConversationType.ADMIN -> Conversation.adminConversationId(userId, contactId)
        }

        val conversation = mapOf(
            "conversationId" to conversationId,
            "campsiteId" to campsiteId,
            "userId" to userId,
            "ownerId" to if (type == ConversationType.OWNER) contactId else "",
            "adminId" to if (type == ConversationType.ADMIN) contactId else "",
            "type" to type.name,
            "lastMessage" to "",
            "lastMessageId" to "",
            "updatedAt" to FieldValue.serverTimestamp()
        )

        createIfMissing(conversationsRef.document(conversationId), conversation)

        return conversationId
    }

    /**
     * Creates or retrieves the conversation an administrator starts with a user from
     * their profile. Unlike a camper, an administrator does not need the user to have
     * enrolled in a campsite first.
     *
     * @param userId Device ID of the user being messaged.
     * @param adminId Device ID of the administrator.
     * @return Unique identifier of the conversation.
     * @throws IllegalStateException If [adminId] is not an administrator or the user does not exist.
     */
    suspend fun startAdminConversation(
        userId: String,
        adminId: String
    ): String {

        require(userId.isNotBlank() && adminId.isNotBlank() && userId != adminId) {
            "Invalid conversation participants"
        }

        val admin = usersRef.document(adminId).get(Source.SERVER).await()

        check(admin.getString("role") == UserRole.ADMIN.name) {
            "Administrator access required"
        }

        check(usersRef.document(userId).get(Source.SERVER).await().exists()) {
            "User not found"
        }

        val conversationId = Conversation.adminConversationId(userId, adminId)

        val conversation = mapOf(
            "conversationId" to conversationId,
            "campsiteId" to "",
            "userId" to userId,
            "ownerId" to "",
            "adminId" to adminId,
            "type" to ConversationType.ADMIN.name,
            "lastMessage" to "",
            "lastMessageId" to "",
            "updatedAt" to FieldValue.serverTimestamp()
        )

        createIfMissing(conversationsRef.document(conversationId), conversation)

        return conversationId
    }

    /**
     * Finds a campsite of [ownerId]'s that [userId] can currently message them about.
     *
     * @return The campsite's ID.
     * @throws IllegalStateException If the camper has no active access to the owner.
     */
    private suspend fun findOwnerCampsite(userId: String, ownerId: String): String {
        val access = messagingAccessRef
            .document(userId)
            .collection("campsites")
            .whereEqualTo("ownerId", ownerId)
            .whereEqualTo("active", true)
            .get(Source.SERVER)
            .await()

        return access.documents.firstOrNull()?.id
            ?: error("Owner messaging is unavailable")
    }

    /**
     * Checks that [adminId] is an administrator and finds a campsite [userId] has enrolled
     * in, since campers can contact administrators once they have enrolled anywhere.
     *
     * @return The campsite's ID.
     * @throws IllegalStateException If the contact is not an administrator or the camper has
     * never enrolled in a campsite.
     */
    private suspend fun findAdminCampsite(userId: String, adminId: String): String {
        val admin = usersRef.document(adminId).get(Source.SERVER).await()

        check(admin.getString("role") == UserRole.ADMIN.name) {
            "Contact is not an administrator"
        }

        val access = messagingAccessRef
            .document(userId)
            .collection("campsites")
            .limit(1)
            .get(Source.SERVER)
            .await()

        return access.documents.firstOrNull()?.id
            ?: error("User has not enrolled in a campsite")
    }

    /**
     * Writes [conversation] to [conversationRef] unless the conversation already exists.
     */
    private suspend fun createIfMissing(
        conversationRef: DocumentReference,
        conversation: Map<String, Any>
    ) {
        db.runTransaction { transaction ->
            if (!transaction.get(conversationRef).exists()) {
                transaction.set(conversationRef, conversation)
            }
        }.await()
    }

    /**
     * Retrieves a conversation the current user takes part in.
     *
     * @param conversationId Unique identifier of the conversation.
     * @param userId Device ID of the current user.
     * @return Conversation object or null if unavailable.
     */
    suspend fun getConversation(
        conversationId: String,
        userId: String
    ): Conversation? {

        require(userId.isNotBlank())

        val conversation = conversationsRef
            .document(conversationId)
            .get()
            .await()
            .toObject(Conversation::class.java)
            ?: return null

        return conversation.takeIf {
            it.hasCurrentId() && it.hasParticipant(userId)
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

        return getConversations(
            conversationsRef.whereEqualTo("userId", userId)
        )
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

        return getConversations(ownerQuery(ownerId))
    }

    /**
     * Retrieves conversations addressed to an administrator.
     *
     * @param adminId Device ID of the administrator.
     * @return List of the administrator's own conversations.
     */
    suspend fun getAdminConversations(
        adminId: String
    ): List<Conversation> {

        require(adminId.isNotBlank())

        return getConversations(adminQuery(adminId))
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
            conversationsRef.whereEqualTo("userId", userId)
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

        return observeConversations(ownerQuery(ownerId))
    }

    /**
     * Observes conversations addressed to an administrator in real time.
     *
     * @param adminId Device ID of the administrator.
     * @return Flow containing the administrator's own conversations.
     */
    fun observeAdminConversations(
        adminId: String
    ): Flow<List<Conversation>> {

        require(adminId.isNotBlank())

        return observeConversations(adminQuery(adminId))
    }

    /**
     * Gets the query for owner conversations addressed to [ownerId].
     */
    private fun ownerQuery(ownerId: String): Query =
        conversationsRef
            .whereEqualTo("ownerId", ownerId)
            .whereEqualTo("type", ConversationType.OWNER.name)

    /**
     * Gets the query for administrator conversations addressed to [adminId].
     */
    private fun adminQuery(adminId: String): Query =
        conversationsRef
            .whereEqualTo("adminId", adminId)
            .whereEqualTo("type", ConversationType.ADMIN.name)

    /**
     * Retrieves the current conversations matching [query], most recent first.
     */
    private suspend fun getConversations(query: Query): List<Conversation> =
        currentConversations(
            query.get().await().toObjects(Conversation::class.java)
        )

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
                trySend(
                    currentConversations(
                        snapshot?.toObjects(Conversation::class.java).orEmpty()
                    )
                )

            } catch (e: Exception) {
                close(e)
            }
        }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Drops conversations from before the one-per-pair ID scheme and orders the rest by
     * recent activity.
     */
    private fun currentConversations(conversations: List<Conversation>): List<Conversation> =
        conversations
            .filter { it.hasCurrentId() }
            .sortedByDescending { it.updatedAt }
}

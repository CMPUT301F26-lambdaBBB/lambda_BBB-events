package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.Conversation
import com.example.lambda_bbb_events.model.Message
import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.LotteryStatus
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.model.message.ConversationType
import com.example.lambda_bbb_events.model.reservation.Reservation
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Handles message data stored within a conversation
 * in the Firestore database.
 *
 * Synchronizes messages with Firestore and maintains
 * a local cache of conversation messages.
 *
 * @param conversationId Unique identifier of the conversation.
 * @param db Firestore database instance.
 */
class MessageRepository(
    private val conversationId: String,
    db: FirebaseFirestore = Firebase.firestore
) : Repository<Message>(
    "conversations/$conversationId/messages", db
) {

    private val conversationRef = db
        .collection("conversations")
        .document(conversationId)

    /**
     * Retrieves the unique identifier of a message.
     *
     * @param item Message object.
     * @return Unique identifier of the message.
     */
    override fun idOf(item: Message) = item.messageId

    /**
     * Converts a Firestore document into a Message object.
     *
     * @param document Firestore document containing message data.
     * @return Deserialized message or null.
     */
    override fun fromDocument(
        document: DocumentSnapshot
    ): Message? =
        document.toObject(Message::class.java)

    /**
     * Cached messages synchronized with Firestore,
     * ordered from newest to oldest.
     */
    val messages: List<Message>
        get() = cached.sortedByDescending {
            it.timestamp
        }

    /**
     * Sends a message within the current conversation.
     *
     * Validates that the sender takes part in the conversation and, for owner
     * conversations, that the camper is still eligible to message the owner.
     * Stores the message and updates the conversation summary in an atomic batch operation.
     *
     * @param content Text content of the message.
     * @param senderId Device ID of the sender.
     * @throws IllegalArgumentException If the message is invalid.
     * @throws IllegalStateException If the sender is not permitted
     * to send messages or the conversation does not exist.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Changed with the help of this AI tool.
     */
    suspend fun sendMessage(
        content: String,
        senderId: String
    ) {
        require(senderId.isNotBlank()) {
            "Sender ID cannot be empty"
        }

        val trimmedContent = content.trim()

        require(trimmedContent.isNotEmpty()) {
            "Message cannot be empty"
        }

        require(trimmedContent.length <= 2000) {
            "Message is too long"
        }

        val conversation = conversationRef
            .get()
            .await()
            .toObject(Conversation::class.java)
            ?: error("Conversation does not exist")

        check(conversation.hasCurrentId() && conversation.hasParticipant(senderId)) {
            "Unauthorized sender"
        }

        if (conversation.type == ConversationType.OWNER.name) {
            check(hasEligibleOwnerAccess(conversation.userId, conversation.ownerId)) {
                "The lottery enrollment or reservation has ended"
            }
        }

        val messageRef = collectionRef.document()

        val message = mapOf(
            "messageId" to messageRef.id,
            "content" to trimmedContent,
            "senderId" to senderId,
            "recipientId" to conversation.contactIdFor(senderId),
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.runBatch { batch ->

            batch.set(messageRef, message)

            batch.update(
                conversationRef,
                mapOf(
                    "lastMessage" to trimmedContent,
                    "lastMessageId" to messageRef.id,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

        }.await()
    }

    /**
     * Deletes a message for both people in the conversation.
     *
     * The message keeps its place but its content is removed from the database, and it is
     * marked as deleted so it shows as [Message.DELETED_TEXT]. If it was the conversation's
     * most recent message, the conversation preview shows that text too.
     *
     * @param messageId Unique identifier of the message.
     * @param requesterId Device ID of the person deleting it.
     * @param isAdmin Whether that person is an administrator.
     * @throws IllegalStateException If the message does not exist or the person may not delete it.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     */
    suspend fun deleteMessage(
        messageId: String,
        requesterId: String,
        isAdmin: Boolean = false
    ) {
        val conversationSnapshot = conversationRef
            .get()
            .await()

        val conversation = conversationSnapshot
            .toObject(Conversation::class.java)
            ?: error("Conversation does not exist")

        check(conversation.hasParticipant(requesterId)) {
            "Unauthorized user"
        }

        val messageRef = collectionRef.document(messageId)

        val message = messageRef
            .get()
            .await()
            .toObject(Message::class.java)
            ?: error("Message does not exist")

        check(message.canBeDeletedBy(requesterId, isAdmin)) {
            "You cannot delete this message"
        }

        val lastMessageId = conversationSnapshot.getString("lastMessageId")

        db.runBatch { batch ->

            batch.update(
                messageRef,
                mapOf(
                    "content" to "",
                    "deleted" to true,
                    "deletedAt" to FieldValue.serverTimestamp()
                )
            )

            if (lastMessageId == messageId) {
                batch.update(conversationRef, "lastMessage", Message.DELETED_TEXT)
            }

        }.await()
    }

    /**
     * Checks whether the entrant can still message a campsite owner: they must have active
     * messaging access and an eligible reservation or lottery enrollment at any of the
     * owner's campsites.
     *
     * @param entrantId Device ID of the entrant.
     * @param ownerId Device ID of the campsite owner.
     * @return True if the entrant remains eligible for owner messaging.
     *
     * Reference:
     * Anthropic. (2026). Claude Code (Claude Opus 5.5) [Large language model].
     * https://claude.com/claude-code
     * Written with the help of this AI tool.
     */
    suspend fun hasEligibleOwnerAccess(
        entrantId: String,
        ownerId: String
    ): Boolean {
        val campsiteIds = db
            .collection("messagingAccess")
            .document(entrantId)
            .collection("campsites")
            .whereEqualTo("ownerId", ownerId)
            .whereEqualTo("active", true)
            .get(Source.SERVER)
            .await()
            .documents
            .map { it.id }

        return campsiteIds.any { campsiteId ->
            hasEligibleEnrollment(entrantId, campsiteId, ownerId)
        }
    }

    /**
     * Checks whether the entrant has an eligible reservation
     * or lottery enrollment at the campsite.
     *
     * Accepted lottery entries are handled through their
     * associated reservations.
     *
     * This function can also be used by the chat screen
     * to determine whether messaging should be enabled.
     *
     * @param entrantId Device ID of the entrant.
     * @param campsiteId ID of the campsite.
     * @param ownerId Device ID of the campsite owner.
     * @return True if the entrant remains eligible for owner messaging.
     */
    suspend fun hasEligibleEnrollment(
        entrantId: String,
        campsiteId: String,
        ownerId: String
    ): Boolean {

        // Check eligible reservations.
        val reservations = db.collection("reservations")
            .whereEqualTo("entrantId", entrantId)
            .get(Source.SERVER)
            .await()
            .toObjects(Reservation::class.java)

        val hasActiveReservation = reservations.any { reservation ->
            reservation.campsiteId == campsiteId &&
                    reservation.ownerId == ownerId &&
                    reservation.isMessagingEligible()
        }

        if (hasActiveReservation) return true

        // Check eligible lottery entries.
        val entries = db.collection("waitlistEntries")
            .whereEqualTo("entrantId", entrantId)
            .get(Source.SERVER)
            .await()
            .toObjects(WaitlistEntry::class.java)

        return entries.any { entry ->

            if (
                entry.status != WaitlistStatus.IN_WAITLIST &&
                entry.status != WaitlistStatus.WON
            ) {
                return@any false
            }

            val lottery = db.collection("lotteries")
                .document(entry.lotteryId)
                .get(Source.SERVER)
                .await()
                .toObject(Lottery::class.java)
                ?: return@any false

            campsiteId in lottery.campsiteIds &&
                    lottery.ownerId == ownerId &&
                    lottery.status != LotteryStatus.CANCELLED &&
                    !Date().after(lottery.bookingEnd)
        }
    }
}
package com.example.lambda_bbb_events.model.repository

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
     * Validates sender permissions and messaging eligibility.
     * Stores the message and updates the conversation
     * summary in an atomic batch operation.
     *
     * @param content Text content of the message.
     * @param senderId Device ID of the sender.
     * @param isAdmin Whether the sender is an administrator.
     * @throws IllegalArgumentException If the message is invalid.
     * @throws IllegalStateException If the sender is not permitted
     * to send messages or the conversation does not exist.
     */
    suspend fun sendMessage(
        content: String,
        senderId: String,
        isAdmin: Boolean = false
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

        check(conversation.exists()) {
            "Conversation does not exist"
        }

        val userId = conversation.getString("userId")
            ?: error("User ID not found")

        val ownerId = conversation.getString("ownerId")
            ?: ""

        val campsiteId = conversation.getString("campsiteId")
            ?: error("Campsite ID not found")

        val type = conversation.getString("type")

        val recipientId = when (type) {

            ConversationType.OWNER.name -> {

                check(senderId == userId || senderId == ownerId) {
                    "Unauthorized sender"
                }

                val access = db
                    .collection("messagingAccess")
                    .document(userId)
                    .collection("campsites")
                    .document(campsiteId)
                    .get(Source.SERVER)
                    .await()

                check(
                    access.getBoolean("active") == true &&
                            access.getString("ownerId") == ownerId
                ) {
                    "Messaging access is no longer available"
                }

                check(
                    hasEligibleEnrollment(
                        entrantId = userId,
                        campsiteId = campsiteId,
                        ownerId = ownerId
                    )
                ) {
                    "The lottery enrollment or reservation has ended"
                }

                if (senderId == userId) {
                    ownerId
                } else {
                    userId
                }
            }

            ConversationType.ADMIN.name -> {

                when {
                    senderId == userId -> "ADMIN_SUPPORT"
                    isAdmin -> userId
                    else -> error("Unauthorized sender")
                }
            }

            else -> error("Invalid conversation type")
        }

        val messageRef = collectionRef.document()

        val message = mapOf(
            "messageId" to messageRef.id,
            "content" to trimmedContent,
            "senderId" to senderId,
            "recipientId" to recipientId,
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
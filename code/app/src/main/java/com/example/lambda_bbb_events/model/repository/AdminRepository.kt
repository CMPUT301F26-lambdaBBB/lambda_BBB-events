package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.AdminLog
import com.example.lambda_bbb_events.model.LogType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository responsible for retrieving notification and message
 * logs for the administrator.
 *
 * Existing messages are retrieved from the conversations collection
 * without creating duplicate message records.
 *
 * @property db The Firebase Firestore database instance.
 */
class AdminLogRepository(
    private val db: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private val conversationsRef =
        db.collection("conversations")

    private val notificationLogsRef =
        db.collection("notificationLogs")

    /**
     * Retrieves all stored notification logs.
     *
     * The logs are sorted by timestamp in descending order,
     * with the most recent notification displayed first.
     *
     * @return A list of notification logs.
     */
    suspend fun getNotificationLogs(): List<AdminLog> {
        // TODO: Retrieve notification logs once implemented.
        return emptyList()
    }

    /**
     * Retrieves messages sent by campsite owners to entrants.
     *
     * Messages sent by entrants and administrative support
     * conversations are excluded.
     *
     * The logs are sorted from newest to oldest.
     *
     * @return A list of owner-to-entrant message logs.
     */
    suspend fun getMessageLogs(): List<AdminLog> {

        val logs = mutableListOf<AdminLog>()

        val conversations = conversationsRef
            .get()
            .await()
            .documents

        for (conversation in conversations) {

            val ownerId =
                conversation.getString("ownerId") ?: continue

            val userId =
                conversation.getString("userId") ?: continue

            val campsiteId =
                conversation.getString("campsiteId") ?: continue

            if (
                ownerId == "ADMIN_SUPPORT" ||
                userId == "ADMIN_SUPPORT" ||
                campsiteId.isBlank()
            ) {
                continue
            }

            val messages = conversation.reference
                .collection("messages")
                .get()
                .await()
                .documents

            for (message in messages) {

                val senderId =
                    message.getString("senderId") ?: continue

                val recipientId =
                    message.getString("recipientId") ?: continue

                // Only include owner-to-entrant messages.
                if (
                    senderId != ownerId ||
                    recipientId != userId
                ) {
                    continue
                }

                logs.add(
                    AdminLog(
                        id = message.reference.path,
                        type = LogType.MESSAGE,
                        title = "Message from owner",
                        senderId = senderId,
                        recipientIds = listOf(recipientId),
                        content =
                            message.getString("content") ?: "",
                        campsiteId = campsiteId,
                        timestamp = getTimestamp(message)
                    )
                )
            }
        }

        return logs.sortedByDescending { it.timestamp }
    }

    /**
     * Converts a stored Firestore timestamp into milliseconds
     * since the Unix epoch.
     *
     * Supports both Firestore Timestamp objects and numeric
     * timestamps.
     *
     * @param document The Firestore document containing the timestamp.
     * @return The timestamp in milliseconds, or zero if unavailable.
     */
    private fun getTimestamp(
        document: DocumentSnapshot
    ): Long {

        return when (val timestamp = document.get("timestamp")) {

            is Timestamp -> timestamp.toDate().time

            is Number -> timestamp.toLong()

            else -> 0L
        }
    }
}
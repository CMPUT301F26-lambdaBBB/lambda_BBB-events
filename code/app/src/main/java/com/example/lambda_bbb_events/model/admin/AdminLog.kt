package com.example.lambda_bbb_events.model

/**
 * Represents the types of logs available to an administrator.
 */
enum class LogType {
    NOTIFICATION,
    MESSAGE
}

/**
 * Represents a notification or message displayed on the admin logs screen.
 *
 * @property id The unique identifier of the log.
 * @property type The type of log.
 * @property title The title of the notification or message.
 * @property senderId The unique identifier of the sender.
 * @property recipientIds The unique identifiers of the recipients.
 * @property content The content of the log.
 * @property campsiteId The unique identifier of the associated campsite.
 * @property timestamp The time the log was created in milliseconds.
 * @property senderName The display name of the sender.
 * @property recipientNames Maps recipient IDs to their display names.
 * @property campsiteName The name of the associated campsite.
 */
data class AdminLog(
    val id: String = "",
    val type: LogType = LogType.NOTIFICATION,
    val title: String = "",
    val senderId: String = "",
    val recipientIds: List<String> = emptyList(),
    val content: String = "",
    val campsiteId: String = "",
    val timestamp: Long = 0L,
    val senderName: String = "",
    val recipientNames: Map<String, String> = emptyMap(),
    val campsiteName: String = ""
) {

    /**
     * Returns the total number of recipients.
     */
    val recipientCount: Int
        get() = recipientIds.size

    /**
     * Returns the sender's display name.
     */
    val senderText: String
        get() = senderName.ifBlank {
            if (senderId == "System") {
                "System"
            } else {
                "Unknown sender"
            }
        }

    /**
     * Returns a formatted description of the recipients.
     */
    val recipientText: String
        get() = when (recipientCount) {
            0 -> "Unknown recipient"

            1 -> recipientNames[recipientIds.first()]
                ?.takeIf { it.isNotBlank() }
                ?: "Unknown recipient"

            else -> "$recipientCount users"
        }

    /**
     * Returns the campsite's display name.
     */
    val campsiteText: String
        get() = campsiteName.ifBlank {
            "Unknown campsite"
        }
}
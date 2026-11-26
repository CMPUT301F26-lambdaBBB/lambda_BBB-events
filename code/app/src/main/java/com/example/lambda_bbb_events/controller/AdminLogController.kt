package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.AdminLog
import com.example.lambda_bbb_events.model.LogType
import com.example.lambda_bbb_events.model.repository.AdminLogRepository

/**
 * Controller responsible for managing notification and message
 * logs displayed on the admin logs screen.
 *
 * Provides access to stored logs through the repository,
 * resolves user and campsite identifiers into display names,
 * and supports filtering logs based on a search query.
 *
 * @property repository The repository used to retrieve admin logs.
 */
class AdminLogController(
    private val repository: AdminLogRepository =
        AdminLogRepository()
) {

    /**
     * Retrieves all notification logs.
     *
     * @return A list of notification logs sorted by timestamp
     * in descending order.
     */
    suspend fun getNotificationLogs(): List<AdminLog> {
        return repository.getNotificationLogs()
    }

    /**
     * Retrieves all owner-to-entrant message logs.
     *
     * @return A list of message logs sorted by timestamp
     * in descending order.
     */
    suspend fun getMessageLogs(): List<AdminLog> {
        return repository.getMessageLogs()
    }

    /**
     * Retrieves logs based on the specified log type.
     *
     * @param type The type of logs to retrieve.
     * @return A list of logs matching the specified type.
     */
    suspend fun getLogs(type: LogType): List<AdminLog> {
        return when (type) {
            LogType.NOTIFICATION -> getNotificationLogs()
            LogType.MESSAGE -> getMessageLogs()
        }
    }

    /**
     * Resolves user and campsite identifiers into display names.
     *
     * Uses existing user and campsite information to display
     * readable names instead of database identifiers.
     *
     * The original identifiers are preserved for internal use.
     *
     * @param logs The logs containing user and campsite identifiers.
     * @param profileNames A map of user IDs to user names.
     * @param campsiteNames A map of campsite IDs to campsite names.
     * @return A list of logs containing resolved display names.
     */
    fun resolveLogNames(
        logs: List<AdminLog>,
        profileNames: Map<String, String>,
        campsiteNames: Map<String, String>
    ): List<AdminLog> {

        return logs.map { log ->

            log.copy(
                senderName = profileNames[log.senderId]
                    ?: log.senderName,

                recipientNames = log.recipientIds.associateWith {
                        recipientId ->

                    profileNames[recipientId]
                        ?: log.recipientNames[recipientId]
                        ?: "Unknown recipient"
                },

                campsiteName = campsiteNames[log.campsiteId]
                    ?: log.campsiteName
            )
        }
    }

    /**
     * Filters a list of logs based on a search query.
     *
     * The search is case-insensitive and checks the title,
     * content, sender name, recipient names, campsite name,
     * and their corresponding identifiers.
     *
     * Returns all logs if the search query is empty.
     *
     * @param logs The list of logs to filter.
     * @param query The search query entered by the administrator.
     * @return A list of logs matching the search query.
     */
    fun searchLogs(
        logs: List<AdminLog>,
        query: String
    ): List<AdminLog> {

        val searchQuery = query.trim()

        if (searchQuery.isEmpty()) {
            return logs
        }

        return logs.filter { log ->

            log.title.contains(
                searchQuery, ignoreCase = true
            ) ||

                    log.content.contains(
                        searchQuery, ignoreCase = true
                    ) ||

                    log.senderText.contains(
                        searchQuery, ignoreCase = true
                    ) ||

                    log.senderId.contains(
                        searchQuery, ignoreCase = true
                    ) ||

                    log.campsiteText.contains(
                        searchQuery, ignoreCase = true
                    ) ||

                    log.campsiteId.contains(
                        searchQuery, ignoreCase = true
                    ) ||

                    log.recipientText.contains(
                        searchQuery, ignoreCase = true
                    ) ||

                    log.recipientNames.values.any {
                        it.contains(searchQuery, ignoreCase = true)
                    } ||

                    log.recipientIds.any {
                        it.contains(searchQuery, ignoreCase = true)
                    }
        }
    }
}
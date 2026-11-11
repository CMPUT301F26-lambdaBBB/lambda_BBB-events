package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.model.repository.WaitlistEntryRepository

/**
 * Controller for [WaitlistEntry]-related operations.
 *
 * @property waitlistEntryRepository The repository for [WaitlistEntry].
 */
class WaitlistEntryController(
    val waitlistEntryRepository: WaitlistEntryRepository
) {
    /**
     * Gets all the waitlist entries joined by an entrant sorted by newest.
     *
     * @param entrantId The entrant device ID.
     * @return The list of entries joined by the entrant.
     */
    fun getEntriesForEntrant(entrantId: String): List<WaitlistEntry> {
        val entries = waitlistEntryRepository.getEntriesForEntrant(entrantId)
        return entries.sortedByDescending { it.joinedAt }
    }
}
package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.Lottery
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
     * Gets all the entries on the waitlist of a lottery, sorted by join date.
     *
     * @param lotteryId The ID of the lottery.
     * @return The entries of the lottery.
     */
    fun getEntriesForLottery(lotteryId: String): List<WaitlistEntry> {
        return waitlistEntryRepository.getEntries(lotteryId).sortedByDescending { it.joinedAt }
    }

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

    /**
     * Gets a waitlist entry by its unique entry ID.
     *
     * @param entryId The ID of the waitlist entry.
     * @return The matching entry, or null if not found.
     */
    fun getEntryById(entryId: String): WaitlistEntry? {
        return waitlistEntryRepository.waitlistEntries
            .firstOrNull { it.entryId == entryId }
    }

    /**
     * Removes an entrant from a lottery waitlist.
     *
     * @param entry The waitlist entry to remove.
     */
    suspend fun leaveWaitlist(entry: WaitlistEntry) {
        waitlistEntryRepository.deleteWaitlistEntry(entry)
    }
}
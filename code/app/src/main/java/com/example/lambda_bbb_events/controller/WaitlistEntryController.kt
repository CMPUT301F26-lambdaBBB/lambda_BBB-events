package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.model.lottery.LotteryStatus
import com.example.lambda_bbb_events.model.repository.WaitlistEntryRepository
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import com.example.lambda_bbb_events.model.repository.MessagingAccessRepository
import com.example.lambda_bbb_events.model.repository.ReservationRepository
import java.util.Date

/**
 * Controller for [WaitlistEntry]-related operations.
 *
 * @property waitlistEntryRepository Repository for waitlist entries.
 * @property lotteryRepository Repository for lottery data.
 * @property reservationRepository Repository for reservation data.
 */
class WaitlistEntryController(
    val waitlistEntryRepository: WaitlistEntryRepository,
    private val lotteryRepository: LotteryRepository,
    private val reservationRepository: ReservationRepository
) {

    /**
     * Gets all the entries on the waitlist of a lottery,
     * sorted by join date.
     *
     * @param lotteryId The ID of the lottery.
     * @return The entries of the lottery.
     */
    fun getEntriesForLottery(lotteryId: String): List<WaitlistEntry> {
        return waitlistEntryRepository.getEntries(lotteryId)
            .sortedByDescending { it.joinedAt }
    }

    /**
     * Gets all the waitlist entries joined by an entrant,
     * sorted by newest.
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
     * Revokes owner messaging access if no other eligible
     * lottery entry remains at the associated campsite.
     *
     * @param entry The waitlist entry to remove.
     */
    suspend fun leaveWaitlist(entry: WaitlistEntry) {
        val lottery = lotteryRepository.getLottery(entry.lotteryId)

        waitlistEntryRepository.deleteWaitlistEntry(entry)

        lottery?.campsiteIds?.forEach { campsiteId ->
            updateMessagingAccess(
                entrantId = entry.entrantId,
                campsiteId = campsiteId,
                excludedEntryId = entry.entryId
            )
        }
    }

    /**
     * Changes a user's waitlist status to declined when they
     * win the lottery. If the user has not won, does nothing.
     *
     * @param entry The waitlist entry to decline.
     */
    suspend fun declineWin(entry: WaitlistEntry) {
        if (entry.status == WaitlistStatus.WON) {

            val lottery = lotteryRepository.getLottery(entry.lotteryId)

            waitlistEntryRepository.updateEntryStatus(
                entry,
                WaitlistStatus.DECLINED
            )

            lottery?.campsiteIds?.forEach { campsiteId ->
                updateMessagingAccess(
                    entrantId = entry.entrantId,
                    campsiteId = campsiteId,
                    excludedEntryId = entry.entryId
                )
            }
        }
    }

    /**
     * Revokes owner messaging access when the entrant has
     * no eligible reservation or other eligible lottery entry
     * at the campsite.
     *
     * Existing conversations and administrator access
     * are preserved.
     *
     * @param entrantId The entrant device ID.
     * @param campsiteId The ID of the campsite.
     * @param excludedEntryId The entry that was removed or declined.
     */
    private suspend fun updateMessagingAccess(
        entrantId: String,
        campsiteId: String,
        excludedEntryId: String
    ) {
        // Check whether the entrant has an eligible reservation.
        val hasActiveReservation = reservationRepository
            .getFreshReservationsForEntrant(entrantId)
            .any { reservation ->
                reservation.campsiteId == campsiteId &&
                        reservation.isMessagingEligible()
            }

        if (hasActiveReservation) return

        // Check remaining lottery entries.
        val remainingEntries = waitlistEntryRepository
            .getFreshEntriesForEntrant(entrantId)

        val hasActiveEnrollment = remainingEntries.any { entry ->

            if (entry.entryId == excludedEntryId) {
                return@any false
            }

            if (
                entry.status != WaitlistStatus.IN_WAITLIST &&
                entry.status != WaitlistStatus.WON
            ) {
                return@any false
            }

            val lottery = lotteryRepository.getLottery(entry.lotteryId)
                ?: return@any false

            campsiteId in lottery.campsiteIds &&
                    lottery.status != LotteryStatus.CANCELLED &&
                    !Date().after(lottery.bookingEnd)
        }

        if (!hasActiveEnrollment) {
            MessagingAccessRepository(entrantId)
                .revokeAccess(campsiteId)
        }
    }
}
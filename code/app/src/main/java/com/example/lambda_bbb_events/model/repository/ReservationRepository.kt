package com.example.lambda_bbb_events.model.repository

import android.util.Log
import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.LotteryStatus
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.model.reservation.Reservation
import com.example.lambda_bbb_events.model.reservation.ReservationStatus
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * Handles reservation data and cached records using Firestore.
 *
 * Manages reservation creation, lottery confirmation,
 * reservation updates, deletion, and messaging eligibility.
 *
 * @param db The Firestore database instance.
 */
class ReservationRepository(
    db: FirebaseFirestore = Firebase.firestore
) : Repository<Reservation>("reservations", db) {

    init {
        startListening()
    }

    override fun idOf(item: Reservation) =
        item.reservationId

    override fun fromDocument(
        document: DocumentSnapshot
    ): Reservation? =
        document.toObject(Reservation::class.java)

    /** Cached reservations synchronized with Firestore. */
    val reservations: List<Reservation>
        get() = cached

    /**
     * Adds a new reservation to the Firestore database
     * and updates messaging access.
     *
     * @param reservation The reservation to add.
     */
    suspend fun addReservation(reservation: Reservation) {
        add(reservation)

        updateMessagingAccess(reservation)
    }

    /**
     * Confirms a reservation for an entrant who has won a lottery.
     *
     * Creates a confirmed reservation and updates the waitlist
     * entry status to ACCEPTED within a single Firestore transaction.
     *
     * Uses the waitlist entry ID to prevent duplicate reservations.
     *
     * @param entryId Unique identifier of the waitlist entry.
     * @param entrantId Device ID of the entrant.
     * @param campsiteId Identifier of the selected campsite.
     * @param fromDate Starting date of the reservation.
     * @param endDate Ending date of the reservation.
     * @param numberOfGuests Number of guests in the reservation.
     */
    suspend fun confirmLotteryReservation(
        entryId: String,
        entrantId: String,
        campsiteId: String,
        fromDate: String,
        endDate: String,
        numberOfGuests: Int
    ) {
        val entryRef = db.collection("waitlistEntries")
            .document(entryId)

        val reservationId = "lottery_$entryId"

        val reservationRef = collectionRef
            .document(reservationId)

        val reservation = db.runTransaction { transaction ->

            // Retrieve the latest waitlist entry.
            val entry = transaction.get(entryRef)
                .toObject(WaitlistEntry::class.java)
                ?: throw IllegalStateException(
                    "Waitlist entry not found."
                )

            // Verify that the entry belongs to the entrant.
            require(entry.entrantId == entrantId) {
                "This waitlist entry does not belong to you."
            }

            // Validate the number of guests.
            require(
                numberOfGuests == entry.numberOfGuests &&
                        numberOfGuests > 0
            ) {
                "Invalid number of guests."
            }

            // Check whether the reservation already exists.
            val existingReservation = transaction
                .get(reservationRef)
                .toObject(Reservation::class.java)

            // Prevent duplicate bookings and allow safe retries.
            if (existingReservation != null) {
                check(
                    entry.status == WaitlistStatus.ACCEPTED &&
                            existingReservation.status ==
                            ReservationStatus.CONFIRMED &&
                            existingReservation.entrantId == entrantId &&
                            existingReservation.campsiteId == campsiteId &&
                            existingReservation.startDate == fromDate &&
                            existingReservation.endDate == endDate &&
                            existingReservation.guests == numberOfGuests
                ) {
                    "This lottery entry already has a reservation."
                }

                return@runTransaction existingReservation
            }

            // Only lottery winners may confirm reservations.
            check(entry.status == WaitlistStatus.WON) {
                "You must win the lottery before confirming a reservation."
            }

            // Retrieve the associated lottery.
            val lotteryRef = db.collection("lotteries")
                .document(entry.lotteryId)

            val lottery = transaction.get(lotteryRef)
                .toObject(Lottery::class.java)
                ?: throw IllegalStateException(
                    "Lottery not found."
                )

            // Prevent reservations for cancelled lotteries.
            check(lottery.status != LotteryStatus.CANCELLED) {
                "This lottery has been cancelled."
            }

            // Prevent reservations after the booking period ends.
            check(!Date().after(lottery.bookingEnd)) {
                "The booking period has already ended."
            }

            // Verify that the selected campsite matches the lottery.
            check(lottery.campsiteIds.singleOrNull() == campsiteId) {
                "Invalid campsite or campsite selection required."
            }

            // Convert the lottery booking dates to LocalDate.
            val bookingStart = lottery.bookingStart
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val bookingEnd = lottery.bookingEnd
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            // Validate the requested reservation dates.
            require(
                LocalDate.parse(fromDate) == bookingStart &&
                        LocalDate.parse(endDate) == bookingEnd
            ) {
                "Invalid reservation dates."
            }

            // Create the confirmed reservation.
            val newReservation = Reservation(
                reservationId = reservationId,
                campsiteId = campsiteId,
                entrantId = entrantId,
                ownerId = lottery.ownerId,
                startDate = fromDate,
                endDate = endDate,
                guests = numberOfGuests,
                status = ReservationStatus.CONFIRMED,
                ruleConfirmed = true
            )

            // Save the reservation.
            transaction.set(
                reservationRef,
                newReservation
            )

            // Mark the lottery entry as accepted.
            transaction.update(
                entryRef,
                "status",
                WaitlistStatus.ACCEPTED.name
            )

            newReservation

        }.await()

        // Update the locally cached reservations.
        putInCache(reservation)

        // Grant messaging access after successful confirmation.
        updateMessagingAccess(reservation)
    }

    /**
     * Deletes a reservation from the Firestore database
     * and rechecks messaging eligibility.
     *
     * @param reservation The reservation to be deleted.
     */
    suspend fun deleteReservation(reservation: Reservation) {
        delete(reservation)

        try {
            revokeMessagingAccessIfIneligible(
                reservation.entrantId,
                reservation.campsiteId
            )

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Log.e(
                "ReservationRepository",
                "Failed to update messaging access",
                e
            )
        }
    }

    /**
     * Saves a reservation to Firestore, creating it if absent.
     *
     * Updates messaging access according to its current status.
     *
     * @param updatedReservation The reservation data to save.
     */
    suspend fun updateReservation(
        updatedReservation: Reservation
    ) {
        update(updatedReservation)

        updateMessagingAccess(updatedReservation)
    }

    /**
     * Gets a reservation from the cache using its reservation ID.
     *
     * @param reservationId The reservation ID to search for.
     * @return The matching reservation or null.
     */
    fun getReservation(
        reservationId: String
    ): Reservation? =
        getCached(reservationId)

    /**
     * Gets all reservations associated with a specific campsite.
     *
     * @param campsiteId The campsite ID to search for.
     * @return The list of matching reservations.
     */
    fun getReservationsForCampsite(
        campsiteId: String
    ): List<Reservation> =
        cached.filter {
            it.campsiteId == campsiteId
        }

    /**
     * Gets all reservations associated with a specific entrant.
     *
     * @param entrantId The entrant ID to search for.
     * @return The list of matching reservations.
     */
    fun getReservationsForEntrant(
        entrantId: String
    ): List<Reservation> =
        cached.filter {
            it.entrantId == entrantId
        }

    /**
     * Retrieves the latest reservations for an entrant
     * directly from the Firestore server.
     *
     * @param entrantId Device ID of the entrant.
     * @return The list of reservations belonging to the entrant.
     */
    suspend fun getFreshReservationsForEntrant(
        entrantId: String
    ): List<Reservation> {
        return collectionRef
            .whereEqualTo("entrantId", entrantId)
            .get(Source.SERVER)
            .await()
            .toObjects(Reservation::class.java)
    }

    /**
     * Updates messaging access based on reservation status.
     *
     * Active reservations grant messaging access.
     * Inactive reservations trigger an eligibility check.
     *
     * @param reservation The reservation being updated.
     */
    private suspend fun updateMessagingAccess(
        reservation: Reservation
    ) {
        try {
            if (reservation.isMessagingEligible()) {

                MessagingAccessRepository(
                    reservation.entrantId,
                    db
                ).grantAccess(
                    campsiteId = reservation.campsiteId,
                    ownerId = reservation.ownerId
                )

            } else {

                revokeMessagingAccessIfIneligible(
                    reservation.entrantId,
                    reservation.campsiteId
                )
            }

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Log.e(
                "ReservationRepository",
                "Failed to update messaging access",
                e
            )
        }
    }

    /**
     * Revokes owner messaging access if the entrant has
     * no active reservation or eligible lottery enrollment
     * at the campsite.
     *
     * Existing conversations and administrator access
     * are preserved.
     *
     * @param entrantId Device ID of the entrant.
     * @param campsiteId ID of the campsite.
     */
    private suspend fun revokeMessagingAccessIfIneligible(
        entrantId: String,
        campsiteId: String
    ) {
        val hasActiveReservation =
            getFreshReservationsForEntrant(entrantId)
                .any { reservation ->
                    reservation.campsiteId == campsiteId &&
                            reservation.isMessagingEligible()
                }

        if (hasActiveReservation) return

        val entries = db.collection("waitlistEntries")
            .whereEqualTo("entrantId", entrantId)
            .get(Source.SERVER)
            .await()
            .toObjects(WaitlistEntry::class.java)

        val hasActiveEnrollment = entries.any { entry ->

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
                    lottery.status != LotteryStatus.CANCELLED &&
                    !Date().after(lottery.bookingEnd)
        }

        if (!hasActiveEnrollment) {
            MessagingAccessRepository(
                entrantId,
                db
            ).revokeAccess(campsiteId)
        }
    }

    /**
     * Restores messaging access for existing eligible reservations
     * belonging to the entrant.
     *
     * Rechecks each reservation inside a Firestore transaction
     * before granting messaging access.
     *
     * @param entrantId Device ID of the entrant.
     */
    suspend fun restoreMessagingAccess(
        entrantId: String
    ) {
        val reservations =
            getFreshReservationsForEntrant(entrantId)

        for (reservation in reservations) {

            if (!reservation.isMessagingEligible()) continue

            val reservationRef = collectionRef
                .document(reservation.reservationId)

            db.runTransaction { transaction ->

                val currentReservation = transaction
                    .get(reservationRef)
                    .toObject(Reservation::class.java)
                    ?: return@runTransaction Unit

                if (
                    currentReservation.entrantId != entrantId ||
                    !currentReservation.isMessagingEligible() ||
                    currentReservation.campsiteId.isBlank() ||
                    currentReservation.ownerId.isBlank()
                ) {
                    return@runTransaction Unit
                }

                val accessRef = db
                    .collection("messagingAccess")
                    .document(entrantId)
                    .collection("campsites")
                    .document(currentReservation.campsiteId)

                val access = transaction.get(accessRef)

                if (
                    !access.exists() ||
                    access.getBoolean("active") != true ||
                    access.getString("ownerId") !=
                    currentReservation.ownerId
                ) {
                    transaction.set(
                        accessRef,
                        mapOf(
                            "campsiteId" to
                                    currentReservation.campsiteId,
                            "ownerId" to
                                    currentReservation.ownerId,
                            "active" to true
                        )
                    )
                }

                Unit

            }.await()
        }
    }
}
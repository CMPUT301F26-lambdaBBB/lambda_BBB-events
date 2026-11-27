package com.example.lambda_bbb_events.model.services

import android.util.Log
import com.example.lambda_bbb_events.controller.NotificationController
import com.example.lambda_bbb_events.model.lottery.WaitlistStatus
import com.example.lambda_bbb_events.model.reservation.ReservationStatus
import com.example.lambda_bbb_events.model.notification.Notification
import com.example.lambda_bbb_events.model.notification.NotificationCategory
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Listens for changes to waitlist entries and reservations
 * associated with the current user.
 *
 * Displays Android notifications according to the user's
 * notification category preferences.
 *
 * @param notificationController Controller responsible for
 * displaying system notifications.
 * @param db Firestore database instance.
 */
class SystemNotificationListener(
    private val notificationController: NotificationController,
    private val db: FirebaseFirestore = Firebase.firestore
) {

    /**
     * Starts listening for system notification events.
     *
     * Listeners are removed when the coroutine is cancelled.
     *
     * @param userId Device ID of the current user.
     */
    suspend fun listen(userId: String) = coroutineScope {

        launch {
            listenToWaitlistEntries(userId)
        }

        launch {
            listenToReservations(userId)
        }
    }

    /**
     * Listens for lottery results and waitlist invitations.
     *
     * @param userId Device ID of the entrant.
     */
    private suspend fun listenToWaitlistEntries(
        userId: String
    ) {
        observeStatusChanges(
            collection = "waitlistEntries",
            userId = userId
        ) { status ->

            when (status) {

                WaitlistStatus.WON.name -> {
                    notificationController.showNotification(
                        notification = Notification(
                            title = "Lottery Winner",
                            message = "Congratulations! You have been selected."
                        ),
                        category = NotificationCategory.LOTTERY
                    )
                }

                WaitlistStatus.LOST.name -> {
                    notificationController.showNotification(
                        notification = Notification(
                            title = "Lottery Result",
                            message = "Unfortunately, you were not selected."
                        ),
                        category = NotificationCategory.REJECTION
                    )
                }

                else -> Unit
            }
        }
    }

    /**
     * Listens for changes to reservation statuses.
     *
     * @param userId Device ID of the entrant.
     */
    private suspend fun listenToReservations(
        userId: String
    ) {
        observeStatusChanges(
            collection = "reservations",
            userId = userId
        ) { status ->

            when (status) {

                ReservationStatus.CONFIRMED.name -> {
                    notificationController.showNotification(
                        notification = Notification(
                            title = "Reservation Confirmed",
                            message = "Your campsite reservation has been confirmed."
                        ),
                        category = NotificationCategory.GENERAL
                    )
                }

                ReservationStatus.REJECTED.name -> {
                    notificationController.showNotification(
                        notification = Notification(
                            title = "Reservation Rejected",
                            message = "Your reservation has been rejected."
                        ),
                        category = NotificationCategory.REJECTION
                    )
                }

                ReservationStatus.CANCELLED.name -> {
                    notificationController.showNotification(
                        notification = Notification(
                            title = "Reservation Cancelled",
                            message = "Your campsite reservation has been cancelled."
                        ),
                        category = NotificationCategory.CANCELLATION
                    )
                }

                else -> Unit
            }
        }
    }

    /**
     * Observes status changes within a Firestore collection.
     *
     * The initial server snapshot is used as a baseline
     * to prevent notifications for existing records.
     *
     * @param collection Firestore collection name.
     * @param userId Device ID of the entrant.
     * @param onStatusChanged Called when a status changes.
     */
    private suspend fun observeStatusChanges(
        collection: String,
        userId: String,
        onStatusChanged: (String) -> Unit
    ) {

        val previousStatuses = mutableMapOf<String, String>()

        var initialized = false

        try {

            observeCollection(
                collection,
                userId
            ).collect { snapshot ->

                // Ignore cached data and uncommitted writes.
                if (snapshot.metadata.isFromCache) {
                    return@collect
                }

                if (snapshot.metadata.hasPendingWrites()) {
                    return@collect
                }

                val currentStatuses =
                    mutableMapOf<String, String>()

                snapshot.documents.forEach { document ->

                    val status = document.getString("status")
                        ?: return@forEach

                    currentStatuses[document.id] = status
                }

                // Store existing statuses without notifying.
                if (!initialized) {

                    previousStatuses.putAll(currentStatuses)

                    initialized = true

                    Log.d(
                        "SystemNotificationListener",
                        "Listening to $collection: ${currentStatuses.size} records"
                    )

                    return@collect
                }

                // Detect changed or newly created statuses.
                currentStatuses.forEach { (id, status) ->

                    val previousStatus = previousStatuses[id]

                    if (previousStatus != status) {

                        Log.d(
                            "SystemNotificationListener",
                            "$collection/$id: $previousStatus -> $status"
                        )

                        onStatusChanged(status)
                    }
                }

                previousStatuses.clear()
                previousStatuses.putAll(currentStatuses)
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(
                "SystemNotificationListener",
                "Failed to observe $collection",
                e
            )
        }
    }

    /**
     * Observes Firestore documents belonging to the entrant.
     *
     * @param collection Firestore collection name.
     * @param userId Device ID of the entrant.
     * @return Flow of Firestore query snapshots.
     */
    private fun observeCollection(
        collection: String,
        userId: String
    ) = callbackFlow<QuerySnapshot> {

        val registration = db
            .collection(collection)
            .whereEqualTo("entrantId", userId)
            .addSnapshotListener(
                MetadataChanges.INCLUDE
            ) { snapshot, error ->

                if (error != null) {

                    close(error)

                    return@addSnapshotListener
                }

                if (snapshot != null) {

                    trySend(snapshot)
                }
            }

        awaitClose {
            registration.remove()
        }
    }
}
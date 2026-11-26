package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.services.FallbackPlaceSearch
import com.example.lambda_bbb_events.model.services.ImageProcessor
import com.example.lambda_bbb_events.model.services.PlaceSearch

/**
 * Groups the application's controllers into a single container.
 *
 * Simplifies passing controllers between screens.
 *
 * @property users Controller for user operations.
 * @property campsites Controller for campsite operations.
 * @property lotteries Controller for lottery operations.
 * @property reservations Controller for reservation operations.
 * @property waitlistEntries Controller for waitlist operations.
 * @property images Service for image processing.
 * @property reviews Controller for campsite reviews.
 * @property places Searches for places by name.
 * @property messages Controller for messaging operations.
 * @property notificationPreferences Controller for notification preferences.
 * @property messageNotifications Controller for incoming message alerts.
 * @property adminLogs Controller for administrative notification and message logs.
 */
class AppControllers(
    val users: UserController,
    val campsites: CampsiteController,
    val lotteries: LotteryController,
    val reservations: ReservationController,
    val waitlistEntries: WaitlistEntryController,
    val images: ImageProcessor,
    val reviews: ReviewController,
    val places: PlaceSearch = FallbackPlaceSearch(),
    val messages: MessageController = MessageController(users),
    val notificationPreferences: NotificationPreferenceController =
        NotificationPreferenceController(users),
    val messageNotifications: MessageNotificationController =
        MessageNotificationController(users),
    val adminLogs: AdminLogController = AdminLogController()
)
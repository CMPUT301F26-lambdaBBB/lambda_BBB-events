package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.services.ImageProcessor
import com.example.lambda_bbb_events.model.lottery.WaitlistEntry

/**
 * Groups the application's controllers into a single container.
 *
 * Simplifies passing controllers between screens.
 *
 * @property users Controller for user operations.
 * @property campsites Controller for campsite operations.
 * @property lotteries Controller for lottery operations.
 */
class AppControllers (
    val users: UserController,
    val campsites: CampsiteController,
    val lotteries: LotteryController,
    val reservations: ReservationController,
    val waitlistEntries: WaitlistEntryController,
    val images: ImageProcessor
)
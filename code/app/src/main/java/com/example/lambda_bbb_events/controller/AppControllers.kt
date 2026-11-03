package com.example.lambda_bbb_events.controller

/**
 * Has every controller used for the app so that each screen only
 * needs to take in one controller.
 *
 * @param users Controller for User class.
 * @param campsites Controller for Campsite class.
 */
class AppControllers (
    val users: UserController,
    val campsites: CampsiteController
)
package com.example.lambda_bbb_events.model.roles

import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.UserRole
//import com.example.lambda_bbb_events.model.Image maybe used later

/*
 * Defines entrant within the user model.
 */

/**
 * Entrants are users who search campsites, joins campsite lotteries and make reservations.
 *
 * @param deviceId The user's device ID.
 * @param name The user's name.
 * @param email The user's email address.
 * @param phone The user's phone number.
 * @param profilePictureImageId The profile image ID.
 * @param notificationsEnabled Whether notifications are enabled.
 * @property waitlistEntryIds IDs of the entrant's waitlist entries.
 * @property reservationIds IDs of the entrant's reservations.
 */
class Entrant (
    deviceId: String = "",
    name: String = "",
    email: String = "",
    phone: String = "",
    profilePictureImageId: String = "",
    notificationsEnabled: Boolean = true,
    val waitlistEntryIds: List<String> = emptyList(),
    val reservationIds: List<String> = emptyList(),
) : User(deviceId, name, email, phone, profilePictureImageId, UserRole.ENTRANT, notificationsEnabled)
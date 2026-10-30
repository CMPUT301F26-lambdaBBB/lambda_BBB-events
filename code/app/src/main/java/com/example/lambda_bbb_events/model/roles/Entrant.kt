package com.example.lambda_bbb_events.model.roles

import com.example.lambda_bbb_events.model.Image
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.UserRole

/**
 * Entrants are users who search campsites, joins campsite lotteries and make reservations.
 *
 * @property deviceId The unique ID of the user's device.
 * @property name The user's real name, may be visible to other users.
 * @property email The user's email address, not visible to others.
 * @property phone The user's phone number, not visible to others.
 * @property profilePictureImageId The user's profile picture imageId.
 * @property notificationsEnabled Whether the user wants to receive notifications.
 * @property waitlistEntryIds IDs of the waitlist entries the entrant has.
 * @property reservationIds IDs of this entrant's reservations.
 */
class Entrant (
    deviceId: String,
    name: String,
    email: String,
    phone: String,
    profilePictureImageId: String = "",
    notificationsEnabled: Boolean = true,
    val waitlistEntryIds: List<String> = emptyList(),
    val reservationIds: List<String> = emptyList(),
) : User(deviceId, name, email, phone, profilePictureImageId, UserRole.ENTRANT, notificationsEnabled)
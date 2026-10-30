package com.example.lambda_bbb_events.model.roles

import com.example.lambda_bbb_events.model.Image
import com.example.lambda_bbb_events.model.User

/**
 * Moderators are users who can review logs and remove users, campsites, and images.
 *
 * @property deviceId The unique ID of the user's device.
 * @property name The user's real name, may be visible to other users.
 * @property email The user's email address, not visible to others.
 * @property phone The user's phone number, not visible to others.
 * @property profilePicture Optional profile picture, visible to other users. Defaults to null (empty).
 * @property notificationsEnabled Whether the user wants to receive notifications.
 */
class Admin(
    deviceId: String,
    name: String,
    email: String,
    phone: String,
    profilePicture: Image? = null,
    notificationsEnabled: Boolean = true,
) : User(deviceId, name, email, phone, profilePicture, notificationsEnabled)
package com.example.lambda_bbb_events.model.roles

import com.example.lambda_bbb_events.model.Image
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.UserRole

/**
 * Owners are users who create campsite listings and runs lotteries and waitlist for them.
 *
 * @property deviceId The unique ID of the user's device.
 * @property name The user's real name, may be visible to other users.
 * @property email The user's email address, not visible to others.
 * @property phone The user's phone number, not visible to others.
 * @property profilePictureImageId The user's profile picture imageId.
 * @property notificationsEnabled Whether the user wants to receive notifications.
 * @property ownedCampsiteIds IDs of campsites owned by the user.
 * @property lotteryIds IDs of lotteries this owner has created.
 */
class CampsiteOwner(
    deviceId: String = "",
    name: String = "",
    email: String = "",
    phone: String = "",
    profilePictureImageId: String = "",
    notificationsEnabled: Boolean = true,
    val ownedCampsiteIds: List<String> = emptyList(),
    val lotteryIds: List<String> = emptyList(),
) : User(deviceId, name, email, phone, profilePictureImageId, UserRole.OWNER, notificationsEnabled)
package com.example.lambda_bbb_events.model.roles


import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.UserRole
//import com.example.lambda_bbb_events.model.Image maybe used later

/*
 * Defines owner within the user model.
 */

/**
 * Owners are users who create campsite listings and runs lotteries and waitlist for them.
 *
 * @param deviceId The user's device ID.
 * @param name The user's name.
 * @param email The user's email address.
 * @param phone The user's phone number.
 * @param profilePictureImageId The profile image ID.
 * @param notificationsEnabled Whether notifications are enabled.
 * @property ownedCampsiteIds IDs of owned campsites.
 * @property lotteryIds IDs of created lotteries.
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
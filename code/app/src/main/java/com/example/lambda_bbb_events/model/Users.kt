package com.example.lambda_bbb_events.model

/**
 * Represents every user that uses the app.
 *
 * @property deviceId The unique ID of the user's device.
 * @property name The user's real name, may be visible to other users.
 * @property email The user's email address, not visible to others.
 * @property phone The user's phone number, not visible to others.
 * @property profilePicture Optional profile picture, visible to other users. Defaults to null (empty).
 * @property notificationsEnabled Whether the user wants to receive notifications.
 */
abstract class User (
    private val deviceId: String,
    var name: String,
    private var email: String,
    private var phone: String,
    var profilePicture: Image? = null, // allow user to have default or no profile picture, or set like a default image so we dont need a null here
    private var notificationsEnabled: Boolean = true
) {
    /**
     * Updates the user's profile details.
     *
     * Only the specified properties are updated. If unspecified, properties keeps
     * their old (current) values.
     *
     * @param name New display name
     * @param email New email address
     * @param phone New phone number
     * @param picture New profile picture
     * @throws IllegalArgumentException if at least one of [name], [email],
     * or [phone] is blank.
     */
    fun updateProfile(
        name: String = this.name,
        email: String = this.email,
        phone: String = this.phone,
        picture: Image? = this.profilePicture
    ) {
        require(name.isNotBlank()) { "Name must not be empty" }
        require(email.isNotBlank()) { "Email must not be empty" }
        require(phone.isNotBlank()) { "Phone number must not be empty" }

        this.name = name
        this.email = email
        this.phone = phone
        this.profilePicture = picture
    }



    /**
     * Turns notifications on or off for the user.
     *
     * @param allow `true` to receive notifications, `false` to opt out.
     */
    fun setAllowNotifications(allow: Boolean) {
        notificationsEnabled = allow
    }
}

/**
 * Entrants are users who search campsites, joins campsite lotteries and make reservations.
 *
 * @property deviceId The unique ID of the user's device.
 * @property name The user's real name, may be visible to other users.
 * @property email The user's email address, not visible to others.
 * @property phone The user's phone number, not visible to others.
 * @property profilePicture Optional profile picture, visible to other users. Defaults to null (empty).
 * @property notificationsEnabled Whether the user wants to receive notifications.
 * @property waitlistEntryIds IDs of the waitlist entries the entrant has.
 * @property reservationIds IDs of this entrant's reservations.
 */
class Entrant (
    deviceId: String,
    name: String,
    email: String,
    phone: String,
    profilePicture: Image? = null,
    notificationsEnabled: Boolean = true,
    val waitlistEntryIds: List<String> = emptyList(),
    val reservationIds: List<String> = emptyList(),
) : User(deviceId, name, email, phone, profilePicture, notificationsEnabled)

/**
 * Owners are users who create campsite listings and runs lotteries and waitlist for them.
 *
 * @property deviceId The unique ID of the user's device.
 * @property name The user's real name, may be visible to other users.
 * @property email The user's email address, not visible to others.
 * @property phone The user's phone number, not visible to others.
 * @property profilePicture Optional profile picture, visible to other users. Defaults to null (empty).
 * @property notificationsEnabled Whether the user wants to receive notifications.
 * @property ownedCampsiteIds IDs of campsites owned by the user.
 * @property lotteryIds IDs of lotteries this owner has created.
 */
class CampsiteOwner(
    deviceId: String,
    name: String,
    email: String,
    phone: String,
    profilePicture: Image? = null,
    notificationsEnabled: Boolean = true,
    val ownedCampsiteIds: List<String> = emptyList(),
    val lotteryIds: List<String> = emptyList(),
) : User(deviceId, name, email, phone, profilePicture, notificationsEnabled)

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
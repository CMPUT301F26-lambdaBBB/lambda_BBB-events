package com.example.lambda_bbb_events.model

enum class UserRole {
    ENTRANT,
    OWNER,
    ADMIN
}

/**
 * Represents every user that uses the app.
 *
 * @property deviceId The unique ID of the user's device.
 * @property name The user's real name, may be visible to other users.
 * @property email The user's email address, not visible to others.
 * @property phone The user's phone number, not visible to others.
 * @property profilePictureImageId The user's profile picture imageId.
 * @property notificationsEnabled Whether the user wants to receive notifications.
 */
open class User (
    val deviceId: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var profilePictureImageId: String = "", // allow user to have default or no profile picture, or set like a default image for a certain image
    var role: UserRole = UserRole.ENTRANT,
    var notificationsEnabled: Boolean = true
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
     * @param imageId New profile picture
     * @throws IllegalArgumentException if at least one of [name], [email],
     * or [phone] is blank.
     */
    fun updateProfile(
        name: String = this.name,
        email: String = this.email,
        phone: String = this.phone,
        imageId: String = this.profilePictureImageId
    ) {
        require(name.isNotBlank()) { "Name must not be empty" }
        require(email.isNotBlank()) { "Email must not be empty" }
        require(phone.isNotBlank()) { "Phone number must not be empty" }

        this.name = name
        this.email = email
        this.phone = phone
        this.profilePictureImageId = imageId
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
package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.roles.CampsiteOwner
import com.example.lambda_bbb_events.model.roles.Entrant

/**
 * Controller for user-related operations.
 *
 * @property userRepository The repository for users.
 * @property deviceId The device ID of the user.
 *
 */
class UserController(
    val userRepository: UserRepository,
    val deviceId: String
) {
    val currentUser: User? // null if user not found in database
        get() = userRepository.getUser(deviceId)

    // use this var for signed in user. only use inside the main app when a user exists
    // so we dont need to consider null exceptions
    val user: User
        get() = currentUser ?: error("No signed-in user")

    val usersFullyLoaded: Boolean
        get() = userRepository.usersFullyLoaded

    /**
     * Creates an Entrant user.
     *
     * @return `true` if the profile was created, `false` if any one of `name`, `email`,
     * `phone` is blank.
     */
    fun createEntrant(name: String, email: String, phone: String): Boolean {
        if (currentUser != null || !isValidProfile(name, email, phone)) return false

        userRepository.addUser(
            Entrant(
                deviceId = deviceId,
                name = name,
                email = email,
                phone = phone
            )
        )
        return true
    }

    /**
     * Updates an Entrant user information.
     *
     * @return `true` if the profile was created, `false` if any one of `name`, `email`,
     * `phone` is blank.
     */
    fun updateEntrant(name: String, email: String, phone: String): Boolean {
        if (currentUser == null || !isValidProfile(name, email, phone)) return false

        userRepository.updateUser(
            Entrant(
                deviceId = deviceId,
                name = name,
                email = email,
                phone = phone
            )
        )
        return true
    }

    /**
     * Turns notifications on or off for the signed-in user (US 01.10.01).
     *
     * @param allow `true` if user wants notifications on, `false` otherwise.
     */
    fun setAllowNotifications(allow: Boolean) {
        val user = user
        userRepository.setAllowNotifications(user, allow)
    }

    /**
     * Checks if the name, email, and phone fields are blank.
     *
     * @return `true` if they are not blank (valid), `false` otherwise.
     */
    private fun isValidProfile(name: String, email: String, phone: String): Boolean {
            return name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
    }

    /**
     * Makes the user a campsite owner. An entrant is converted to a [CampsiteOwner].
     * Does not affect if user is owner or admin roles.
     *
     * @return `true` if the user can now own campsites, `false` if there is no signed-in user.
     */
    fun becomeOwner(): Boolean {
        val user = currentUser

        if (user == null) {
            return false
        }

        // only entrants need converting
        if (user is Entrant) {
            userRepository.updateUser(
                CampsiteOwner(
                    deviceId = user.deviceId,
                    name = user.name,
                    email = user.email,
                    phone = user.phone,
                    profilePictureImageId = user.profilePictureImageId,
                    notificationsEnabled = user.notificationsEnabled
                )
            )
        }

        return true
    }
}
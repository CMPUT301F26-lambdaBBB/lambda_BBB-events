package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.roles.Entrant

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

    /**
     * Creates an Entrant user.
     *
     * @return `true` if the profile was created, `false` if any one of `name`, `email`,
     * `phone` is blank.
     */
    fun createEntrant(name: String, email: String, phone: String): Boolean {
        if (currentUser == null || !isValidProfile(name, email, phone)) return false

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
}
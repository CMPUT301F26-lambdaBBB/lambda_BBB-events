package com.example.lambda_bbb_events.controller

import android.provider.Settings
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.roles.Entrant

class UserController (val userRepository: UserRepository, val deviceId: String) {
    val currentUser: User? // null if user not found in database
        get() = userRepository.getUser(deviceId)

    /**
     * Creates an Entrant user.
     *
     * @return `true` if the profile was created, `false` if any one of `name`, `email`,
     * `phone` is blank.
     * */
    fun createEntrant(name: String, email: String, phone: String): Boolean {
        if (name.isBlank() || email.isBlank() || phone.isBlank()) return false
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
     * */
    fun updateEntrant(name: String, email: String, phone: String): Boolean {
        if (name.isBlank() || email.isBlank() || phone.isBlank()) return false
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
}
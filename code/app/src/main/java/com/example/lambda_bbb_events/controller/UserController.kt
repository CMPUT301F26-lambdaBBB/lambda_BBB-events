package com.example.lambda_bbb_events.controller

import android.provider.Settings
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.roles.Entrant

class UserController (val userRepository: UserRepository, val deviceId: String) {
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
}
package com.example.lambda_bbb_events.controller

import android.util.Patterns
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.roles.CampsiteOwner
import com.example.lambda_bbb_events.model.roles.Entrant

/*
 * Manages user profiles, roles, and notification settings
 * through the repository.
 *
 */

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
    /** The current user, or null if not found. */
    val currentUser: User? // null if user not found in database
        get() = userRepository.getCachedUser(deviceId)

    // use this var for signed in user. only use inside the main app when a user exists
    // so we dont need to consider null exceptions
    /** The signed-in user; throws if none exists. */
    val user: User
        get() = currentUser ?: error("No signed-in user")

    /** Whether user data has finished loading. */
    val usersFullyLoaded: Boolean
        get() = userRepository.usersFullyLoaded


    /**
     * Creates an entrant profile if none exists.
     *
     * @param name The user's name.
     * @param email The user's email address.
     * @param phone The user's phone number.
     * @return True if creation was submitted; false if invalid
     * or a user already exists.
     */
    suspend fun createEntrant(name: String, email: String, phone: String): Boolean {
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
     * Updates the current user's profile information.
     *
     * @param name The updated name.
     * @param email The updated email address.
     * @param phone The updated phone number.
     * @return True if the update was submitted; false if
     * the input is invalid or no user exists.
     */
    suspend fun updateEntrant(name: String, email: String, phone: String): Boolean {
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
    suspend fun setAllowNotifications(allow: Boolean) {
        val user = user
        userRepository.setAllowNotifications(user, allow)
    }

    /**
     * Checks if a provided string matches a phone number.
     *
     * Utilizes [Patterns] to check if it matches a phone number.
     *
     * @param phone A string that is supposed to match a phone number.
     */
    private fun isValidPhone(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }

    /**
     * Checks if a provided string resembles an email.
     *
     * Utilizes [Patterns] to check if it matches an email.
     *
     * @param email A string that is supposed to match an email.
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Checks if the name, email, and phone fields are blank.
     *
     * @return `true` if they are not blank (valid), `false` otherwise.
     */
    private fun isValidProfile(name: String, email: String, phone: String): Boolean {
        val standardPhoneNumber = phone.replace(Regex("[^0-9]"), "")
        return name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() &&
                isValidEmail(email) && isValidPhone(standardPhoneNumber)
    }

    /**
     * Makes the user a campsite owner. An entrant is converted to a [CampsiteOwner].
     * Does not affect if user is owner or admin roles.
     *
     * @return `true` if the user can now own campsites, `false` if there is no signed-in user.
     */
    suspend fun becomeOwner(): Boolean {
        val user = currentUser ?: return false

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

    /**
     * Gets the name of a user.
     *
     * @param deviceId The device ID of the user.
     * @return The user's name. Returns "Unknown user" if the user does not exist.
     */
    suspend fun getUserName(deviceId: String): String {
        val user = userRepository.getUser(deviceId) ?: return "Unknown user"

        return user.name
    }


    /**
     * Deletes the current user's account.
     *
     * @return True if deleted, false if no user exists.
     */
    suspend fun deleteAccount(): Boolean {
        val user = currentUser ?: return false
        userRepository.deleteUser(user)
        return true
    }
}
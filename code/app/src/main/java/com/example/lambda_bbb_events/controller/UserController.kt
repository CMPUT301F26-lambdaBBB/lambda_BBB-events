package com.example.lambda_bbb_events.controller

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lambda_bbb_events.model.formatting.parsePhoneNumber
import com.example.lambda_bbb_events.model.local.InMemorySessionStore
import com.example.lambda_bbb_events.model.local.SessionStore
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.user.CampsiteOwner
import com.example.lambda_bbb_events.model.user.Entrant

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
 * @param sessionStore Remembers whether the user has logged out.
 *
 */
class UserController(
    val userRepository: UserRepository,
    val deviceId: String,
    private val sessionStore: SessionStore = InMemorySessionStore()
) {
    /** Whether the user has logged out. Kept as Compose state so the screens update. */
    private var loggedOut by mutableStateOf(sessionStore.loggedOut)

    /** The current user, or null if not found or logged out. */
    val currentUser: User? // null if user not found in database
        get() = if (loggedOut) null else userRepository.getCachedUser(deviceId)

    /** Whether this device has an account, even if the user has logged out of it. */
    val hasAccountOnDevice: Boolean
        get() = userRepository.getCachedUser(deviceId) != null

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
        // A logged out account still owns this device ID, so signing up would replace it
        if (hasAccountOnDevice || !isValidProfile(name, email, phone)) return false

        userRepository.addUser(
            Entrant(
                deviceId = deviceId,
                name = name,
                email = email,
                phone = phone
            )
        )
        saveLoggedOut(false)
        return true
    }

    /**
     * Logs back in to this device's account. Accounts cannot move to other devices, so
     * the email and phone number are checked against the account made on this device.
     *
     * The email is compared ignoring case. Phone numbers are compared by country code and
     * digits, so formatting differences and numbers saved before country codes were added
     * still match.
     *
     * @param email The account's email address.
     * @param phone The account's phone number, including its country code.
     * @return `true` if logged in, `false` if this device has no account or the email or
     * phone number does not match it.
     */
    fun login(email: String, phone: String): Boolean {
        val account = userRepository.getCachedUser(deviceId) ?: return false
        val emailMatches = account.email.trim().equals(email.trim(), ignoreCase = true)
        if (!emailMatches || normalizePhone(account.phone) != normalizePhone(phone)) {
            return false
        }

        saveLoggedOut(false)
        return true
    }

    /**
     * Logs out of this device's account. The account is kept, and the user can log back in
     * with its email and phone number.
     */
    fun logOut() {
        saveLoggedOut(true)
    }

    private fun saveLoggedOut(value: Boolean) {
        loggedOut = value
        sessionStore.loggedOut = value
    }

    /** Turns a phone number into its country code and digits, such as "+17805551234". */
    private fun normalizePhone(phone: String): String {
        val parsed = parsePhoneNumber(phone)
        return parsed.country.dialCode + parsed.localNumber
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
        val user = currentUser
        if (user == null || !isValidProfile(name, email, phone)) return false

        // Change only these details, so the user keeps their role, profile picture,
        // settings, campsites, and waitlist entries
        user.updateProfile(name = name, email = email, phone = phone)
        userRepository.updateUser(user)
        return true
    }

    /**
     * Sets the current user's profile picture.
     *
     * @param imageId The ID of the uploaded picture, or an empty string to remove it.
     * @return The ID of the picture it replaced, empty if there was none, or `null` if
     * there is no signed-in user.
     */
    suspend fun updateProfilePicture(imageId: String): String? {
        val user = currentUser ?: return null

        val oldImageId = user.profilePictureImageId
        user.profilePictureImageId = imageId
        userRepository.updateUser(user)
        return oldImageId
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

    /**
     * Gets all users from the database.
     *
     * @return List of every user.
     */
    suspend fun getAllUsers(): List<User> {
        userRepository.refreshUsers()
        return userRepository.users
    }
}
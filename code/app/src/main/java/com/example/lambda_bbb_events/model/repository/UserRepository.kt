package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.roles.Admin
import com.example.lambda_bbb_events.model.roles.CampsiteOwner
import com.example.lambda_bbb_events.model.roles.Entrant
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Handles the data in the Firestore database for the [User] class.
 */
class UserRepository {
    private val db = Firebase.firestore
    private val usersRef = db.collection("users")

    private val _users = mutableStateListOf<User>()

    var usersFullyLoaded by mutableStateOf(false)
    init {
        usersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _users.clear()

            snapshot?.documents?.forEach { document ->
                val role = document.getString("role")

                val user = when (role) {
                    "ENTRANT" -> document.toObject(Entrant::class.java)
                    "OWNER" -> document.toObject(CampsiteOwner::class.java)
                    "ADMIN" -> document.toObject(Admin::class.java)
                    else -> null
                }

                if (user != null) {
                    _users.add(user)
                }
            }
            usersFullyLoaded = true
        }
    }

    val users: List<User>
        get() = _users

    /**
     * Adds a new user to the Firestore database.
     *
     * @param user The user to add.
     */
    fun addUser(user: User) {
        usersRef.document(user.deviceId).set(user)
    }

    /**
     * Deletes a user from the Firestore database.
     *
     * @param user The user to be deleted.
     */
    fun deleteUser(user: User) {
        usersRef.document(user.deviceId).delete()
    }

    /**
     * Overwrites an existing user in the Firestore database with updated data.
     *
     * @param updatedUser The user to be updated. Must have the same `userId`
     * as the original document.
     */
    fun updateUser(updatedUser: User) {
        usersRef.document(updatedUser.deviceId).set(updatedUser)
    }

    /**
     * Gets a [User] object from the database given its deviceId.
     *
     * @param deviceId The `deviceId` to search for.
     * @return The [User] object with the specified deviceId if it exists. `null` otherwise.
     */
    fun getUser(deviceId: String): User? {
        // there's probably a faster way to do this but i dont understand all the stuff i read in google
        // someone pls rewrite this, this is temporary
        for (user in _users) {
            if (user.deviceId == deviceId) {
                return user
            }
        }
        return null
    }

    /**
     * Changes the user's notification setting to on or off.
     *
     * @param user The user to search for.
     * @param allow `true` to set notifications on, `false` otherwise.
     */
    fun setAllowNotifications(user: User, allow: Boolean) {
        usersRef.document(user.deviceId).update("notificationsEnabled", allow)
    }
}
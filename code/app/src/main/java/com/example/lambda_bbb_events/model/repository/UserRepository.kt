package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.model.roles.Admin
import com.example.lambda_bbb_events.model.roles.CampsiteOwner
import com.example.lambda_bbb_events.model.roles.Entrant
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * Handles the data in the Firestore database for the [User] class.
 */
class UserRepository {
    private val db = Firebase.firestore
    private val usersRef = db.collection("users")

    private val userMutex = Mutex()
    private val _users = mutableStateMapOf<String, User>()


    var usersFullyLoaded by mutableStateOf(false)

    val users: List<User>
        get() = _users.values.toList()

    /**
     * Adds a new user to the Firestore database.
     *
     * @param user The user to add.
     */
    suspend fun addUser(user: User) {
        usersRef.document(user.deviceId)
            .set(user)
            .await()
        userMutex.withLock {
            _users[user.deviceId] = user
        }
    }

    /**
     * Deletes a user from the Firestore database.
     *
     * @param user The user to be deleted.
     */
    suspend fun deleteUser(user: User) {
        usersRef.document(user.deviceId)
            .delete()
            .await()
        userMutex.withLock {
            _users.remove(user.deviceId)
        }
    }


    /**
     * Overwrites an existing user in the Firestore database with updated data.
     *
     * @param user The user to be updated. Must have the same `userId`
     * as the original document.
     */
    suspend fun updateUser(user: User) {
        usersRef.document(user.deviceId)
            .set(user)
            .await()
        userMutex.withLock {
            _users[user.deviceId] = user
        }
    }

    /**
     * Converts a Firestore document into the appropriate User subtype
     * based on its role field.
     *
     * @return The deserialized User, or null if the role is unknown.
     */
    private fun DocumentSnapshot.toUser(): User? =
        when (getString("role")) {
            "ENTRANT" -> toObject(Entrant::class.java)
            "OWNER" -> toObject(CampsiteOwner::class.java)
            "ADMIN" -> toObject(Admin::class.java)
            else -> null
        }

    /**
     * [getCachedUser] gets users saved in the cache instead of going through db read again.
     *
     * @param deviceId The `deviceId` of the user to retrieve.
     * @return The [User] object with the specified deviceId if it exists in the cache. `null` otherwise.
     */
    fun getCachedUser(deviceId: String): User? = _users[deviceId]

    /**
     * Gets a [User] object from the database given its deviceId.
     *
     * @param deviceId The `deviceId` to search for.
     * @return The [User] object with the specified deviceId if it exists. `null` otherwise.
     */
    suspend fun getUser(deviceId: String): User? {
        val document = usersRef.document(deviceId)
            .get()
            .await()

        return document.toUser()
    }

    /**
     * Changes the user's notification setting to on or off.
     *
     * @param user The user to search for.
     * @param allow `true` to set notifications on, `false` otherwise.
     */
    suspend fun setAllowNotifications(
        user: User,
        allow: Boolean
    ) {
        usersRef.document(user.deviceId)
            .update("notificationsEnabled", allow)
            .await()
        userMutex.withLock {
            _users[user.deviceId]?.let {
                it.notificationsEnabled = allow
            }
        }
    }

    /**
     * Refreshes the list of users from the database.
     */
    suspend fun refreshUsers() = userMutex.withLock {
        val snapshot = usersRef.get().await()

        val loadedUsers = snapshot.documents.mapNotNull {
            it.toUser()
        }

        _users.clear()
        loadedUsers.forEach {
            _users[it.deviceId] = it
        }

        usersFullyLoaded = true
    }

}
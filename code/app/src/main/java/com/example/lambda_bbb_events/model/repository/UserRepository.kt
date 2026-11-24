package com.example.lambda_bbb_events.model.repository

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.model.user.Admin
import com.example.lambda_bbb_events.model.user.CampsiteOwner
import com.example.lambda_bbb_events.model.user.Entrant
import com.example.lambda_bbb_events.model.user.UserRole
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/*
 * Manages user data and cached profiles using Firestore.
 */

/**
 * Thrown when the database did not accept the admin password.
 */
class WrongAdminPasswordException : Exception("The admin password was not accepted")

/**
 * Handles the data in the Firestore database for the [User] class.
 */
class UserRepository(
    db: FirebaseFirestore = Firebase.firestore
) : Repository<User>("users", db) {

    /** Whether the initial user data has finished loading successfully. */
    var usersFullyLoaded by mutableStateOf(false)

    /** Cached user profiles. */
    val users: List<User>
        get() = cached

    override fun idOf(item: User) = item.deviceId

    /**
     * Converts a Firestore document into the appropriate User subtype
     * based on its role field.
     *
     * @return The deserialized User, or null if the role is unknown.
     */
    override fun fromDocument(document: DocumentSnapshot): User? =
        when (document.getString("role")) {
            "ENTRANT" -> document.toObject(Entrant::class.java)
            "OWNER" -> document.toObject(CampsiteOwner::class.java)
            "ADMIN" -> document.toObject(Admin::class.java)
            else -> null
        }

    override fun onListenError(error: FirebaseFirestoreException) {
        Log.e("UserRepository", "listener error", error)
    }

    /**
     * Adds a new user to the Firestore database.
     *
     * @param user The user to add.
     */
    suspend fun addUser(user: User) = add(user)

    /**
     * Deletes a user from the Firestore database and the cache.
     *
     * Only the user's own document is deleted. Their campsites, lotteries, and pictures
     * are stored elsewhere and are left as they are.
     *
     * @param user The user to be deleted.
     */
    suspend fun deleteUser(user: User) = delete(user)

    /**
     * Saves user data to Firestore, creating the document if absent.
     *
     * @param user The user data to save, identified by deviceId.
     */
    suspend fun updateUser(user: User) = update(user)

    /**
     * [getCachedUser] gets users saved in the cache instead of going through db read again.
     *
     * @param deviceId The `deviceId` of the user to retrieve.
     * @return The [User] object with the specified deviceId if it exists in the cache. `null` otherwise.
     */
    fun getCachedUser(deviceId: String): User? = getCached(deviceId)

    /**
     * Reads a user from Firestore.
     *
     * @param deviceId The user's device ID.
     * @return The user, or null if they do not exist.
     */
    suspend fun getUser(deviceId: String): User? = get(deviceId)

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
        collectionRef.document(user.deviceId)
            .update("notificationsEnabled", allow)
            .await()
        getCached(user.deviceId)?.setAllowNotifications(allow)
    }

    /**
     * Changes the user's role to admin, if the admin password is correct.
     *
     * The password is checked by the database's security rules (`firestore.rules`), never by
     * the app. It is sent in a grant that nobody can read, written together with the new role.
     * This is a transaction, so it is sent straight to the database instead of being saved on
     * this device, and either both are written or neither is.
     *
     * Only the role is written, so everything else stored for the user is kept, including
     * details that [Admin] does not have, such as an entrant's waitlist entries. Fails if
     * the user no longer exists, rather than creating them again.
     *
     * @param user The user to make an admin.
     * @param password The admin password.
     * @return The user as an [Admin].
     * @throws WrongAdminPasswordException if the database did not accept the password.
     */
    suspend fun makeAdmin(user: User, password: String): Admin {
        val grantRef = db.collection("adminGrants").document(user.deviceId)
        val userRef = collectionRef.document(user.deviceId)
        val grant = mapOf(
            "code" to password,
            "grantedAt" to FieldValue.serverTimestamp()
        )

        try {
            db.runTransaction { transaction ->
                transaction.set(grantRef, grant)
                transaction.update(userRef, "role", UserRole.ADMIN.name)
            }.await()
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                throw WrongAdminPasswordException()
            }
            throw e
        }

        val admin = Admin(
            deviceId = user.deviceId,
            name = user.name,
            email = user.email,
            phone = user.phone,
            profilePictureImageId = user.profilePictureImageId,
            notificationsEnabled = user.notificationsEnabled
        )
        putInCache(admin)
        return admin
    }

    /**
     * Refreshes cached users from Firestore once. Use [startListening] to keep them up to date.
     */
    suspend fun refreshUsers() {
        refresh()
        usersFullyLoaded = true
    }
}

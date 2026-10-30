package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Handles the data in the Firestore database for the [User] class.
 */
class UserRepository {
    private val db = Firebase.firestore
    private val usersRef = db.collection("users")

    private val _users = mutableStateListOf<User>()

    init {
        usersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _users.clear()

            snapshot?.documents?.forEach { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _users.add(user)
                }
            }
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
}
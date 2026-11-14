package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserRepository {
    private val db = Firebase.firestore
    private val usersRef = db.collection("users")

    suspend fun addUser(user: User) {

    }

    suspend fun deleteUser(user: User) {

    }

    suspend fun updateUser(user: User) {

    }

    suspend fun getUser(deviceId: String): User? {
        return null
    }

    suspend fun setAllowNotifications(
        user: User,
        allow: Boolean
    ) {

    }
}
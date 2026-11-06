package com.example.lambda_bbb_events

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.roles.Entrant
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryTests {
    private lateinit var repository: UserRepository
    @Before
    fun setup() {
        repository = UserRepository()
    }

    private fun testDeviceId() =
        "test-device-${System.currentTimeMillis()}"

    @Test
    fun addUser_addsUserToCache() = runTest {
        val user = Entrant(
            deviceId = testDeviceId()
        )
        try {
            repository.addUser(user)

            assertEquals(
                user,
                repository.getCachedUser(user.deviceId)
            )
        } finally {
            repository.deleteUser(user)
        }
    }

    @Test
    fun updateUser_updatesCachedUser() = runTest {
        val user = Entrant(
            deviceId = testDeviceId()
        )

        repository.addUser(user)

        try {
            user.notificationsEnabled = false

            repository.updateUser(user)

            assertFalse(
                repository.getCachedUser(user.deviceId)!!
                    .notificationsEnabled
            )
        } finally {
            repository.deleteUser(user)
        }
    }

    @Test
    fun deleteUser_removesUserFromCache() = runTest {
        val user = Entrant(
            deviceId = testDeviceId()
        )

        repository.addUser(user)
        repository.deleteUser(user)

        assertNull(
            repository.getCachedUser(user.deviceId)
        )
    }

    @Test
    fun refreshUsers_loadsUsers() = runTest {
        val user = Entrant(
            deviceId = testDeviceId()
        )

        repository.addUser(user)

        try {
            repository.refreshUsers()

            assertTrue(repository.usersFullyLoaded)
            assertNotNull(
                repository.getCachedUser(user.deviceId)
            )
        } finally {
            repository.deleteUser(user)
        }
    }

    @Test
    fun getUser_returnsUserFromFirestore() = runTest {
        val user = Entrant(
            deviceId = testDeviceId()
        )

        repository.addUser(user)

        try {
            val retrieved =
                repository.getUser(user.deviceId)

            assertNotNull(retrieved)

            assertEquals(
                user.deviceId,
                retrieved!!.deviceId
            )
        } finally {
            repository.deleteUser(user)
        }
    }

    @Test
    fun getCachedUser_returnsNullWhenAbsent() {
        val result =
            repository.getCachedUser("does-not-exist")

        assertNull(result)
    }

    @Test
    fun getCachedUser_returnsUserWhenPresent() = runTest {
        val user = Entrant(
            deviceId = testDeviceId()
        )
        repository.addUser(user)

        try {
            val result =
                repository.getCachedUser(user.deviceId)

            assertEquals(
                user,
                result
            )
        } finally {
            repository.deleteUser(user)
        }
    }

    @Test
    fun setAllowNotifications_updatesCache() = runTest {
        val user = Entrant(
            deviceId = testDeviceId()
        )

        repository.addUser(user)

        try {
            repository.setAllowNotifications(
                user,
                false
            )

            assertFalse(
                repository.getCachedUser(user.deviceId)!!
                    .notificationsEnabled
            )
        } finally {
            repository.deleteUser(user)
        }
    }
}
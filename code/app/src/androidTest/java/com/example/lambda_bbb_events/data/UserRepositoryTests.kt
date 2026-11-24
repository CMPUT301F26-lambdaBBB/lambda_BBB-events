package com.example.lambda_bbb_events.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.user.Entrant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests [UserRepository] against the real Firestore database.
 *
 * Every user uses a unique device ID and is deleted after each test in [cleanUp].
 */
@RunWith(AndroidJUnit4::class)
class UserRepositoryTests {
    private val testData = FirestoreTestData()
    private lateinit var repository: UserRepository

    @Before
    fun setUp() {
        repository = UserRepository()
    }

    @After
    fun cleanUp() {
        repository.stopListening()
        testData.cleanUp()
    }

    private fun newUser() = Entrant(deviceId = testData.newId("users", prefix = "test-device"))

    @Test
    fun addUser_addsUserToCache() = runTest {
        val user = newUser()

        repository.addUser(user)

        assertEquals(user, repository.getCachedUser(user.deviceId))
    }

    @Test
    fun updateUser_updatesCachedUser() = runTest {
        val user = newUser()
        repository.addUser(user)

        user.notificationsEnabled = false
        repository.updateUser(user)

        assertFalse(repository.getCachedUser(user.deviceId)!!.notificationsEnabled)
    }

    @Test
    fun deleteUser_removesUserFromCache() = runTest {
        val user = newUser()

        repository.addUser(user)
        repository.deleteUser(user)

        assertNull(repository.getCachedUser(user.deviceId))
    }

    @Test
    fun refreshUsers_loadsUsers() = runTest {
        val user = newUser()
        repository.addUser(user)

        repository.refreshUsers()

        assertTrue(repository.usersFullyLoaded)
        assertNotNull(repository.getCachedUser(user.deviceId))
    }

    @Test
    fun getUser_returnsUserFromFirestore() = runTest {
        val user = newUser()
        repository.addUser(user)

        val retrieved = repository.getUser(user.deviceId)

        assertNotNull(retrieved)
        assertEquals(user.deviceId, retrieved!!.deviceId)
    }

    @Test
    fun getCachedUser_returnsNullWhenAbsent() {
        assertNull(repository.getCachedUser("does-not-exist"))
    }

    @Test
    fun getCachedUser_returnsUserWhenPresent() = runTest {
        val user = newUser()
        repository.addUser(user)

        assertEquals(user, repository.getCachedUser(user.deviceId))
    }

    @Test
    fun setAllowNotifications_updatesCache() = runTest {
        val user = newUser()
        repository.addUser(user)

        repository.setAllowNotifications(user, false)

        assertFalse(repository.getCachedUser(user.deviceId)!!.notificationsEnabled)
    }

    @Test
    fun deleteUser_removesUserFromFirestore() = runTest {
        val user = newUser()
        repository.addUser(user)

        repository.deleteUser(user)

        assertNull(repository.getUser(user.deviceId))
    }
}

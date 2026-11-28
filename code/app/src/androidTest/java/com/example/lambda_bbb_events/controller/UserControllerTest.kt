package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.user.CampsiteOwner
import com.example.lambda_bbb_events.model.user.Entrant
import com.example.lambda_bbb_events.model.user.User
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Verifies user controller workflows against their Android dependencies. */
class UserControllerTest {

    private lateinit var repository: UserRepository
    private lateinit var controller: UserController

    private val deviceId = "test-device"

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        controller = UserController(repository, deviceId)
    }

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun currentUserCachedUser() {
        val user = Entrant(deviceId = deviceId)

        every {
            repository.getCachedUser(deviceId)
        } returns user

        assertEquals(user, controller.currentUser)
    }

    @Test
    fun currentUserNullWhenMissing() {
        every {
            repository.getCachedUser(deviceId)
        } returns null

        assertNull(controller.currentUser)
    }

    @Test
    fun usersFullyLoadedRepositoryValue() {
        every {
            repository.usersFullyLoaded
        } returns true

        assertTrue(controller.usersFullyLoaded)
    }

    @Test
    fun createEntrantFalseWhenUserAlreadyExists() = runTest {
        every {
            repository.getCachedUser(deviceId)
        } returns Entrant(deviceId = deviceId)

        val result =
            controller.createEntrant(
                "John",
                "john@test.com",
                "7801234567"
            )

        assertFalse(result)

        coVerify(exactly = 0) {
            repository.addUser(any())
        }
    }

    @Test
    fun createEntrantFalseForInvalidName() = runTest {
        every {
            repository.getCachedUser(deviceId)
        } returns null

        val result =
            controller.createEntrant(
                "",
                "john@test.com",
                "7801234567"
            )

        assertFalse(result)
    }

    @Test
    fun createEntrantFalseForInvalidEmail() = runTest {
        every {
            repository.getCachedUser(deviceId)
        } returns null

        val result =
            controller.createEntrant(
                "John",
                "not-an-email",
                "7801234567"
            )

        assertFalse(result)
    }

    @Test
    fun createEntrantFalseForInvalidPhone() = runTest {
        every {
            repository.getCachedUser(deviceId)
        } returns null

        val result =
            controller.createEntrant(
                "John",
                "john@test.com",
                "abc"
            )

        assertFalse(result)
    }

    @Test
    fun createEntrantWhenValid() = runTest {
        every {
            repository.getCachedUser(deviceId)
        } returns null

        val result =
            controller.createEntrant(
                "John",
                "john@test.com",
                "7801234567"
            )

        assertTrue(result)

        coVerify(exactly = 1) {
            repository.addUser(any())
        }
    }

    @Test
    fun updateEntrantFalseWhenNoUserExists() = runTest {
        every {
            repository.getCachedUser(deviceId)
        } returns null

        val result =
            controller.updateEntrant(
                "John",
                "john@test.com",
                "7801234567"
            )

        assertFalse(result)

        coVerify(exactly = 0) {
            repository.updateUser(any())
        }
    }

    @Test
    fun updateEntrantFalseForInvalidProfile() = runTest {
        every {
            repository.getCachedUser(deviceId)
        } returns Entrant(deviceId = deviceId)

        val result =
            controller.updateEntrant(
                "",
                "bad-email",
                "bad-phone"
            )

        assertFalse(result)

        coVerify(exactly = 0) {
            repository.updateUser(any())
        }
    }

    @Test
    fun updateEntrantUserWhenValid() = runTest {
        every {
            repository.getCachedUser(deviceId)
        } returns Entrant(deviceId = deviceId)

        val result =
            controller.updateEntrant(
                "John",
                "john@test.com",
                "7801234567"
            )

        assertTrue(result)

        coVerify(exactly = 1) {
            repository.updateUser(any())
        }
    }

    @Test
    fun setAllowNotificationsUpdates() = runTest {
        val user = Entrant(deviceId = deviceId)

        every {
            repository.getCachedUser(deviceId)
        } returns user

        controller.setAllowNotifications(true)

        coVerify(exactly = 1) {
            repository.setAllowNotifications(user, true)
        }
    }

    @Test
    fun becomeOwnerFalseWhenNoUserExists() = runTest {
        every {
            repository.getCachedUser(deviceId)
        } returns null

        val result = controller.becomeOwner()

        assertFalse(result)

        coVerify(exactly = 0) {
            repository.updateUser(any())
        }
    }

    @Test
    fun becomeOwnerConvertsEntrantToOwner() = runTest {
        val entrant = Entrant(
            deviceId = deviceId,
            name = "John",
            email = "john@test.com",
            phone = "7801234567"
        )

        every {
            repository.getCachedUser(deviceId)
        } returns entrant

        val result = controller.becomeOwner()

        assertTrue(result)

        coVerify(exactly = 1) {
            repository.updateUser(
                match {
                    it is CampsiteOwner &&
                            it.deviceId == deviceId
                }
            )
        }
    }

    @Test
    fun becomeOwnerTrueForExistingOwner() = runTest {
        val owner = CampsiteOwner(
            deviceId = deviceId,
            name = "John",
            email = "john@test.com",
            phone = "7801234567"
        )

        every {
            repository.getCachedUser(deviceId)
        } returns owner

        val result = controller.becomeOwner()

        assertTrue(result)

        coVerify(exactly = 0) {
            repository.updateUser(any())
        }
    }

    @Test
    fun updateEntrantKeepsRolePictureAndCampsites() = runTest {
        val owner = CampsiteOwner(
            deviceId = deviceId,
            name = "Olive",
            email = "olive@example.com",
            phone = "7805551234",
            profilePictureImageId = "img-1",
            notificationsEnabled = false,
            ownedCampsiteIds = listOf("site-1")
        )
        every { repository.getCachedUser(deviceId) } returns owner
        val saved = slot<User>()
        coEvery { repository.updateUser(capture(saved)) } just runs

        controller.updateEntrant("Olivia", "olivia@example.com", "7805550000")

        val updated = saved.captured
        assertTrue(updated is CampsiteOwner)
        assertEquals("Olivia", updated.name)
        assertEquals("olivia@example.com", updated.email)
        assertEquals("7805550000", updated.phone)
        assertEquals("img-1", updated.profilePictureImageId)
        assertFalse(updated.notificationsEnabled)
        assertEquals(listOf("site-1"), (updated as CampsiteOwner).ownedCampsiteIds)
    }
}

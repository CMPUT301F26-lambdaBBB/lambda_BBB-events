package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.model.UserRole
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.roles.Admin
import com.example.lambda_bbb_events.model.roles.CampsiteOwner
import com.example.lambda_bbb_events.model.roles.Entrant
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests [UserController] logic that does not depend on Android's input validation.
 * Name, email, and phone validation is covered by the instrumented `UserControllerTest`.
 */
class UserControllerUnitTest {
    private val repository = mockk<UserRepository>()
    private lateinit var controller: UserController

    private val entrant = Entrant(
        deviceId = "device-1",
        name = "Ed",
        email = "ed@example.com",
        phone = "7805551234",
        profilePictureImageId = "img-1",
        notificationsEnabled = false
    )

    @Before
    fun setup() {
        every { repository.getCachedUser(any()) } returns null
        every { repository.usersFullyLoaded } returns true
        coEvery { repository.updateUser(any()) } just runs
        coEvery { repository.deleteUser(any()) } just runs
        coEvery { repository.setAllowNotifications(any(), any()) } just runs
        controller = UserController(repository, "device-1")
    }

    private fun signIn(user: com.example.lambda_bbb_events.model.User) {
        every { repository.getCachedUser("device-1") } returns user
    }

    @Test
    fun currentUserComesFromCache() {
        assertNull(controller.currentUser)

        signIn(entrant)

        assertSame(entrant, controller.currentUser)
        assertSame(entrant, controller.user)
        assertTrue(controller.usersFullyLoaded)
    }

    @Test
    fun userThrowsWhenNobodyIsSignedIn() {
        assertThrows(IllegalStateException::class.java) { controller.user }
    }

    @Test
    fun createEntrantFailsWhenUserAlreadyExists() = runTest {
        signIn(entrant)

        assertFalse(controller.createEntrant("Ed", "ed@example.com", "7805551234"))
        coVerify(exactly = 0) { repository.addUser(any()) }
    }

    @Test
    fun updateEntrantFailsWithoutUser() = runTest {
        assertFalse(controller.updateEntrant("Ed", "ed@example.com", "7805551234"))
        coVerify(exactly = 0) { repository.updateUser(any()) }
    }

    @Test
    fun becomeOwnerConvertsEntrantKeepingProfile() = runTest {
        signIn(entrant)
        val saved = slot<com.example.lambda_bbb_events.model.User>()
        coEvery { repository.updateUser(capture(saved)) } just runs

        assertTrue(controller.becomeOwner())

        val owner = saved.captured
        assertTrue(owner is CampsiteOwner)
        assertEquals(UserRole.OWNER, owner.role)
        assertEquals("device-1", owner.deviceId)
        assertEquals("Ed", owner.name)
        assertEquals("ed@example.com", owner.email)
        assertEquals("7805551234", owner.phone)
        assertEquals("img-1", owner.profilePictureImageId)
        assertFalse(owner.notificationsEnabled)
    }

    @Test
    fun becomeOwnerLeavesOwnersAndAdminsUnchanged() = runTest {
        signIn(CampsiteOwner(deviceId = "device-1"))
        assertTrue(controller.becomeOwner())

        signIn(Admin(deviceId = "device-1"))
        assertTrue(controller.becomeOwner())

        coVerify(exactly = 0) { repository.updateUser(any()) }
    }

    @Test
    fun becomeOwnerFailsWithoutUser() = runTest {
        assertFalse(controller.becomeOwner())
    }

    @Test
    fun getUserNameFallsBackForUnknownUsers() = runTest {
        coEvery { repository.getUser("device-1") } returns entrant
        coEvery { repository.getUser("nobody") } returns null

        assertEquals("Ed", controller.getUserName("device-1"))
        assertEquals("Unknown user", controller.getUserName("nobody"))
    }

    @Test
    fun setAllowNotificationsUpdatesSignedInUser() = runTest {
        signIn(entrant)

        controller.setAllowNotifications(true)

        coVerify(exactly = 1) { repository.setAllowNotifications(entrant, true) }
    }

    @Test
    fun deleteAccountDeletesSignedInUser() = runTest {
        assertFalse(controller.deleteAccount())

        signIn(entrant)
        assertTrue(controller.deleteAccount())

        coVerify(exactly = 1) { repository.deleteUser(entrant) }
    }
}

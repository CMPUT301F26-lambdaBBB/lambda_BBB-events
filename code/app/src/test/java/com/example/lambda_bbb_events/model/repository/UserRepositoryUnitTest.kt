package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.user.Admin
import com.example.lambda_bbb_events.model.user.CampsiteOwner
import com.example.lambda_bbb_events.model.user.Entrant
import com.example.lambda_bbb_events.model.user.UserRole
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryUnitTest {
    private val db = mockk<FirebaseFirestore>()
    private lateinit var users: MockCollection
    private lateinit var repository: UserRepository

    private val entrant = Entrant(deviceId = "entrant-1", name = "Ed")
    private val owner = CampsiteOwner(deviceId = "owner-1", name = "Olive")
    private val admin = Admin(deviceId = "admin-1", name = "Ada")

    /** A user document with the given role that deserializes into every role class. */
    private fun userDocument(id: String, role: String?): DocumentSnapshot {
        val document = mockk<DocumentSnapshot>()
        every { document.id } returns id
        every { document.getString("role") } returns role
        every { document.toObject(Entrant::class.java) } returns entrant
        every { document.toObject(CampsiteOwner::class.java) } returns owner
        every { document.toObject(Admin::class.java) } returns admin
        return document
    }

    private fun returnsOnGet(id: String, role: String?) {
        every { users.document(id).get() } returns completedTask(userDocument(id, role))
    }

    @Before
    fun setup() {
        users = MockCollection(db, "users")
        repository = UserRepository(db)
    }

    @Test
    fun getUserDeserializesByRole() = runTest {
        returnsOnGet("entrant-1", "ENTRANT")
        returnsOnGet("owner-1", "OWNER")
        returnsOnGet("admin-1", "ADMIN")

        assertSame(entrant, repository.getUser("entrant-1"))
        assertSame(owner, repository.getUser("owner-1"))
        assertSame(admin, repository.getUser("admin-1"))
    }

    @Test
    fun getUserWithUnknownRoleReturnsNull() = runTest {
        returnsOnGet("someone", "SUPERUSER")
        returnsOnGet("nobody", null)

        assertNull(repository.getUser("someone"))
        assertNull(repository.getUser("nobody"))
    }

    @Test
    fun refreshUsersLoadsCacheAndMarksLoaded() = runTest {
        users.returnsOnGet(userDocument("entrant-1", "ENTRANT"), userDocument("x", "UNKNOWN"))
        assertFalse(repository.usersFullyLoaded)

        repository.refreshUsers()

        assertTrue(repository.usersFullyLoaded)
        assertSame(entrant, repository.users.single())
        assertSame(entrant, repository.getCachedUser("entrant-1"))
    }

    @Test
    fun addAndDeleteUserKeepCacheInSync() = runTest {
        repository.addUser(entrant)
        verify(exactly = 1) { users.document("entrant-1").set(entrant) }
        assertSame(entrant, repository.getCachedUser("entrant-1"))

        repository.deleteUser(entrant)
        verify(exactly = 1) { users.document("entrant-1").delete() }
        assertNull(repository.getCachedUser("entrant-1"))
    }

    @Test
    fun setAllowNotificationsUpdatesFieldAndCachedUser() = runTest {
        every { users.document("entrant-1").update("notificationsEnabled", false) } returns completedTask(null)
        val cachedEntrant = Entrant(deviceId = "entrant-1", notificationsEnabled = true)
        repository.addUser(cachedEntrant)

        repository.setAllowNotifications(cachedEntrant, false)

        verify(exactly = 1) { users.document("entrant-1").update("notificationsEnabled", false) }
        assertFalse(repository.getCachedUser("entrant-1")!!.notificationsEnabled)
    }

    @Test
    fun deleteUserKeepsUserCachedWhenItFails() = runTest {
        repository.addUser(entrant)
        every { users.document("entrant-1").delete() } returns failedTask(RuntimeException("offline"))

        assertTrue(runCatching { repository.deleteUser(entrant) }.isFailure)

        assertSame(entrant, repository.getCachedUser("entrant-1"))
    }

    @Test
    fun makeAdminWritesOnlyTheRoleAndKeepsTheProfile() = runTest {
        val user = CampsiteOwner(
            deviceId = "owner-1",
            name = "Olive",
            email = "olive@example.com",
            phone = "+1 7805551234",
            profilePictureImageId = "img-1",
            notificationsEnabled = false
        )
        val document = users.document("owner-1")
        every { document.update("role", "ADMIN") } returns completedTask(null)
        repository.addUser(user)

        val promoted = repository.makeAdmin(user)

        assertEquals(UserRole.ADMIN, promoted.role)
        assertEquals("owner-1", promoted.deviceId)
        assertEquals("Olive", promoted.name)
        assertEquals("olive@example.com", promoted.email)
        assertEquals("+1 7805551234", promoted.phone)
        assertEquals("img-1", promoted.profilePictureImageId)
        assertFalse(promoted.notificationsEnabled)
        assertSame(promoted, repository.users.single())
        verify(exactly = 1) { document.update("role", "ADMIN") }
        verify(exactly = 1) { document.set(any()) } // only from adding the user
    }

    @Test
    fun makeAdminKeepsOldRoleCachedWhenItFails() = runTest {
        every { users.document("entrant-1").update("role", "ADMIN") } returns
            failedTask(RuntimeException("not found"))
        repository.addUser(entrant)

        assertTrue(runCatching { repository.makeAdmin(entrant) }.isFailure)

        assertSame(entrant, repository.getCachedUser("entrant-1"))
    }
}

package com.example.lambda_bbb_events

import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.roles.Admin
import com.example.lambda_bbb_events.model.roles.CampsiteOwner
import com.example.lambda_bbb_events.model.roles.Entrant
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
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
}

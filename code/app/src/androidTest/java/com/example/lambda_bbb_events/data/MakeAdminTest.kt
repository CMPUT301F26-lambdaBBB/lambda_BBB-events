package com.example.lambda_bbb_events.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.repository.WrongAdminPasswordException
import com.example.lambda_bbb_events.model.user.Entrant
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests how [UserRepository.makeAdmin] reports failures. Firestore's error codes only load on a
 * device, so these are not unit tests. Firestore is mocked, so nothing is written to the database.
 */
@RunWith(AndroidJUnit4::class)
class MakeAdminTest {
    private val db = mockk<FirebaseFirestore>(relaxed = true)
    private val repository = UserRepository(db)
    private val user = Entrant(deviceId = "user-1", name = "Ed")

    @After
    fun cleanUp() {
        unmockkAll()
        clearAllMocks()
    }

    private fun failWith(code: FirebaseFirestoreException.Code): FirebaseFirestoreException {
        val error = FirebaseFirestoreException("failed", code)
        every { db.runTransaction(any<Transaction.Function<Any?>>()) } returns Tasks.forException(error)
        return error
    }

    @Test
    fun deniedWriteIsReportedAsAWrongPassword() = runTest {
        failWith(FirebaseFirestoreException.Code.PERMISSION_DENIED)

        val error = runCatching { repository.makeAdmin(user, "wrong") }.exceptionOrNull()

        assertTrue(error is WrongAdminPasswordException)
        assertNull(repository.getCachedUser("user-1"))
    }

    @Test
    fun otherFailuresAreNotBlamedOnThePassword() = runTest {
        val offline = failWith(FirebaseFirestoreException.Code.UNAVAILABLE)

        val error = runCatching { repository.makeAdmin(user, "password") }.exceptionOrNull()

        assertSame(offline.code, (error as FirebaseFirestoreException).code)
        assertNull(repository.getCachedUser("user-1"))
    }
}

package com.example.lambda_bbb_events.data

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.lambda_bbb_events.LambdaEventsApp
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.user.Entrant
import com.example.lambda_bbb_events.model.user.UserRole
import com.example.lambda_bbb_events.store.CampsiteStore
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests that the open app follows changes made by someone else in the real Firestore database.
 *
 * The changes are made through a second connection to Firestore, like another device or the
 * Firebase console, so they only reach the app through its listeners. All data uses unique IDs
 * and is deleted after each test in [cleanUp].
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class RemoteChangesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testData = FirestoreTestData()
    private val controllers = mockk<AppControllers>(relaxed = true)
    private val userRepository = UserRepository()
    private val campsiteRepository = CampsiteRepository()
    private lateinit var otherApp: FirebaseApp
    private lateinit var otherDevice: FirebaseFirestore

    @Before
    fun connectOtherDevice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        otherApp = FirebaseApp.initializeApp(
            context, FirebaseApp.getInstance().options, testData.hashCode().toString()
        )
        otherDevice = FirebaseFirestore.getInstance(otherApp)
    }

    @After
    fun cleanUp() {
        userRepository.stopListening()
        campsiteRepository.stopListening()
        otherApp.delete()
        unmockkAll()
        clearAllMocks()
        testData.cleanUp()
    }

    /** Opens the app signed in to a new account, loading users like MainActivity does. */
    private fun openApp(): String {
        val deviceId = testData.newId("users", prefix = "test-device")
        runBlocking {
            userRepository.addUser(
                Entrant(
                    deviceId = deviceId,
                    name = "Ed",
                    email = "ed@example.com",
                    phone = "+1 7805551234"
                )
            )
            userRepository.refreshUsers()
            userRepository.startListening()
        }
        every { controllers.users } returns UserController(userRepository, deviceId)

        composeTestRule.setContent {
            LambdaEventsApp(controllers = controllers, darkTheme = false, onDarkThemeChange = {})
        }
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("screen_home"), TIMEOUT)
        return deviceId
    }

    @Test
    fun deletingTheAccountElsewhereReturnsTheOpenAppToWelcome() {
        val deviceId = openApp()

        runBlocking { otherDevice.document("users/$deviceId").delete().await() }

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("welcomeScreen"), TIMEOUT)
        composeTestRule.waitUntilDoesNotExist(hasTestTag("screen_home"), TIMEOUT)
    }

    @Test
    fun promotingTheAccountElsewhereShowsAdminToolsInTheOpenApp() {
        val deviceId = openApp()

        runBlocking {
            otherDevice.document("users/$deviceId").update("role", UserRole.ADMIN.name).await()
        }

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("roleSwitcher"), TIMEOUT)
    }

    @Test
    fun deletingACampsiteElsewhereRemovesItFromTheOpenApp() {
        val campsiteId = testData.newId("campsites", prefix = "test-campsite")
        runBlocking { campsiteRepository.addCampsite(Campsite(campsiteId = campsiteId, campsiteName = "Lakeside")) }
        val store = CampsiteStore(campsiteRepository)
        composeTestRule.waitUntil(TIMEOUT) { campsiteId in store.campsites }

        runBlocking { otherDevice.document("campsites/$campsiteId").delete().await() }

        composeTestRule.waitUntil(TIMEOUT) { campsiteId !in store.campsites }
    }

    private companion object {
        /** How long a change may take to reach the app, in milliseconds. */
        const val TIMEOUT = 15_000L
    }
}

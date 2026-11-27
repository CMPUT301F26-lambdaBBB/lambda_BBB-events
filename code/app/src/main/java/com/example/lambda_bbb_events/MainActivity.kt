package com.example.lambda_bbb_events

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.CampsiteController
import com.example.lambda_bbb_events.controller.LotteryController
import com.example.lambda_bbb_events.controller.ReservationController
import com.example.lambda_bbb_events.controller.ReviewController
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.controller.WaitlistEntryController
import com.example.lambda_bbb_events.controller.NotificationController
import com.example.lambda_bbb_events.model.local.LocalDatabase
import com.example.lambda_bbb_events.model.local.SharedPreferencesSessionStore
import com.example.lambda_bbb_events.model.local.SqliteRecentlyViewedStore
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import com.example.lambda_bbb_events.model.repository.ReservationRepository
import com.example.lambda_bbb_events.model.repository.ReviewRepository
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.model.repository.WaitlistEntryRepository
import com.example.lambda_bbb_events.model.services.FcmTokenRegistrar
import com.example.lambda_bbb_events.model.services.ImageProcessor
import com.example.lambda_bbb_events.model.services.IncomingMessageListener
import com.example.lambda_bbb_events.model.user.UserRole
import com.example.lambda_bbb_events.store.CampsiteStore
import com.example.lambda_bbb_events.view.ui.theme.Lambda_BBBeventsTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.lambda_bbb_events.model.services.FirebaseNotificationService
import kotlinx.coroutines.flow.collect
import com.example.lambda_bbb_events.model.services.SystemNotificationListener
import kotlinx.coroutines.delay

/*
 * Defines the application's main Android activity.
 * Initializes repositories and controllers, retrieves
 * the device identifier, and launches the Compose UI.
 */

/**
 * Initializes the repositories and controllers, retrieves
 * the device identifier, and launches the application
 * using the configured theme.
 */
class MainActivity : ComponentActivity() {
    private lateinit var syncController: AppControllers

    // Launches notification permission prompt upon start up if notification is not yet enabled
    // and syncs user preference in user controller with app granted permission
    private val startupNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            lifecycleScope.launch {
                syncController.users.syncNotificationPermissionFromSystem(granted)
            }
        }

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Reference:
        // Selcuk, B. (2022, October 8).
        // Android device ID in Kotlin language [Video]. YouTube.
        // https://www.youtube.com/watch?v=W5yYiZ45u9I
        val deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val userRepository = UserRepository()
        val lotteryRepository = LotteryRepository()

        val campsiteStore = CampsiteStore(
            lotteryRepository = lotteryRepository
        )

        val reservationRepository = ReservationRepository()
        val waitlistEntryRepository = WaitlistEntryRepository()
        val reviewRepository = ReviewRepository()
        val imageProcessor = ImageProcessor()
        val userController = UserController(userRepository, deviceId,SharedPreferencesSessionStore(applicationContext))
        val notifications = NotificationController(applicationContext,userController)
        notifications.createNotificationChannel()
        FcmTokenRegistrar.register(deviceId)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            userRepository.refreshUsers()

            // Keep users up to date, so the app reacts
            // if this account is removed or promoted.
            userRepository.startListening()
        }

        val controllers = AppControllers(
            users = userController,
            campsites = CampsiteController(
                campsiteStore,
                SqliteRecentlyViewedStore(
                    LocalDatabase(applicationContext)
                )
            ),
            lotteries = LotteryController(lotteryRepository),
            reservations = ReservationController(reservationRepository),
            waitlistEntries = WaitlistEntryController(
                waitlistEntryRepository,
                lotteryRepository,
                reservationRepository
            ),
            images = imageProcessor,
            reviews = ReviewController(reviewRepository),
            notifications = notifications
        )

        syncController = controllers
        requestNotifPermission()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                FirebaseNotificationService
                    .incomingNotifications
                    .collect { notification ->

                        controllers.notifications
                            .showNotification(notification)
                    }
            }
        }
        // Initialize the incoming message listener.
        val incomingMessageListener = IncomingMessageListener(
            messageController = controllers.messages,
            notificationController = controllers.messageNotifications
        )

        val systemNotificationListener = SystemNotificationListener(
            notificationController = controllers.notifications
        )

        setContent {

            val currentUser = controllers.users.currentUser
            val currentUserId = currentUser?.deviceId
            val isAdmin = currentUser?.role == UserRole.ADMIN

            // Listen for lottery and reservation notifications.
            LaunchedEffect(currentUserId) {
                if (currentUserId != null) {
                    systemNotificationListener.listen(currentUserId)
                }
            }

            // Check for pending weekly notification summaries.
            LaunchedEffect(currentUserId) {

                if (currentUserId != null) {

                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                        while (true) {

                            controllers.notifications.showWeeklySummaries()

                            delay(60_000L)
                        }
                    }
                }
            }

            // Restore messaging access for existing lottery enrollments.
            LaunchedEffect(currentUserId) {
                if (currentUserId != null) {
                    try {
                        lotteryRepository.restoreMessagingAccess(
                            currentUserId
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(
                            "MainActivity",
                            "Failed to restore messaging access",
                            e
                        )
                    }
                }
            }

            // Restore messaging access for existing reservations.
            LaunchedEffect(currentUserId) {
                if (currentUserId != null) {
                    try {
                        reservationRepository.restoreMessagingAccess(
                            currentUserId
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(
                            "MainActivity",
                            "Failed to restore reservation messaging access",
                            e
                        )
                    }
                }
            }

            // Listen for incoming messages while a user is signed in.
            LaunchedEffect(currentUserId, isAdmin) {
                if (currentUserId != null) {
                    try {
                        incomingMessageListener.listen(
                            context = applicationContext,
                            isAdmin = isAdmin
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(
                            "MainActivity",
                            "Incoming message listener failed",
                            e
                        )
                    }
                }
            }

            // Follow Android's dark mode setting, unless the
            // user picked a theme in the app.
            // Only their choice is saved, so changing Android's
            // setting still takes effect.
            var chosenDarkTheme by rememberSaveable {
                mutableStateOf<Boolean?>(null)
            }

            val darkTheme =
                chosenDarkTheme ?: isSystemInDarkTheme()

            // Light or dark status bar icons to match
            // the app's theme, not Android's setting.
            LaunchedEffect(darkTheme) {
                val barStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    )
                }

                enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle
                )
            }

            Lambda_BBBeventsTheme(
                darkTheme = darkTheme
            ) {
                LambdaEventsApp(
                    controllers = controllers,
                    darkTheme = darkTheme,
                    onDarkThemeChange = {
                        chosenDarkTheme = it
                    }
                )
            }
        }
    }

    /**
     * Permission prompt for notification permission on app launch.
     *
     * asks for notification app permission if it is not granted already.
     * User can choose to not allow and re-enable from settings at settings -> Notification
     * Note: After two declined permission prompts, the startup prompt will no longer be shown
     */
    private fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
            }
            else -> {
                startupNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

    }
}
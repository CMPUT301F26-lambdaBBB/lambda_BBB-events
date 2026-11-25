package com.example.lambda_bbb_events

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.lambda_bbb_events.model.services.ImageProcessor
import com.example.lambda_bbb_events.store.CampsiteStore
import com.example.lambda_bbb_events.view.ui.theme.Lambda_BBBeventsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/*
 * Defines the application's main Android activity.
 * Initializes repositories and controllers, retrieves
 * the device identifier, and launches the Compose UI.
 *
 * TODO: Handle user data loading failures.
 */

/**
 * Initializes the repositories and controllers, retrieves
 * the device identifier, and launches the application
 * using the configured theme.
 */
class MainActivity : ComponentActivity() {
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Reference:
        // Selcuk, B. (2022, October 8).
        // Android device ID in Kotlin language [Video]. YouTube.
        // https://www.youtube.com/watch?v=W5yYiZ45u9I
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val userRepository = UserRepository()
        val lotteryRepository = LotteryRepository()
        val campsiteStore = CampsiteStore(lotteryRepository = lotteryRepository)
        val reservationRepository = ReservationRepository()
        val waitlistEntryRepository = WaitlistEntryRepository()
        val reviewRepository = ReviewRepository()
        val imageProcessor = ImageProcessor()
        val userController = UserController(userRepository, deviceId,SharedPreferencesSessionStore(applicationContext))
        val notifications = NotificationController(applicationContext,userController)
        notifications.createNotificationChannel()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            userRepository.refreshUsers()
            // Keep users up to date, so the app reacts if this account is removed or promoted
            userRepository.startListening()
        }

        val controllers = AppControllers(
            users = UserController(
                userRepository,
                deviceId,
                SharedPreferencesSessionStore(applicationContext)
            ),
            campsites = CampsiteController(
                campsiteStore,
                SqliteRecentlyViewedStore(LocalDatabase(applicationContext))
            ),
            lotteries = LotteryController(lotteryRepository),
            reservations = ReservationController(reservationRepository),
            waitlistEntries = WaitlistEntryController(waitlistEntryRepository),
            images = imageProcessor,
            reviews = ReviewController(reviewRepository),
            notifications = notifications
        )

        setContent {
            // Follow Android's dark mode setting, unless the user picked a theme in the app.
            // Only their choice is saved, so changing Android's setting still takes effect.
            var chosenDarkTheme by rememberSaveable {
                mutableStateOf<Boolean?>(null)
            }
            val darkTheme = chosenDarkTheme ?: isSystemInDarkTheme()

            // Light or dark status bar icons to match the app's theme, not Android's setting
            LaunchedEffect(darkTheme) {
                val barStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }

            Lambda_BBBeventsTheme(
                darkTheme = darkTheme
            ){
                LambdaEventsApp(
                    controllers = controllers,
                    darkTheme = darkTheme,
                    onDarkThemeChange = { chosenDarkTheme = it }
                )
            }
        }
    }
}
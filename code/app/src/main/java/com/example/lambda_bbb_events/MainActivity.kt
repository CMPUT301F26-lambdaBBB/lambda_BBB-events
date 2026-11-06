package com.example.lambda_bbb_events

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.CampsiteController
import com.example.lambda_bbb_events.controller.LotteryController
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.view.ui.theme.Lambda_BBBeventsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


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
        val campsiteRepository = CampsiteRepository()
        val lotteryRepository = LotteryRepository()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            userRepository.refreshUsers()
        }

        val controllers = AppControllers(
            users = UserController(userRepository, deviceId),
            campsites = CampsiteController(campsiteRepository),
            lotteries = LotteryController(lotteryRepository)
        )

        setContent {
            Lambda_BBBeventsTheme {
                LambdaEventsApp(controllers)
            }
        }
    }
}
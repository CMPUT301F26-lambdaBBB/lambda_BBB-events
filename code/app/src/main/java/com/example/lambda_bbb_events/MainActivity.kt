package com.example.lambda_bbb_events

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.CampsiteController
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.view.ui.theme.Lambda_BBBeventsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) // citation: from https://www.youtube.com/watch?v=W5yYiZ45u9I

        val userRepository = UserRepository()
        val campsiteRepository = CampsiteRepository()

        val controllers = AppControllers(
            users = UserController(userRepository, deviceId),
            campsites = CampsiteController(campsiteRepository)
        )

        setContent {
            Lambda_BBBeventsTheme {
                LambdaEventsApp(controllers)
            }
        }
    }
}

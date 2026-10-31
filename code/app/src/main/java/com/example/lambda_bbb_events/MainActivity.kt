package com.example.lambda_bbb_events

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.model.repository.UserRepository
import com.example.lambda_bbb_events.view.ui.theme.Lambda_BBBeventsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lambda_BBBeventsTheme {
                val deviceId: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) // from https://www.youtube.com/watch?v=W5yYiZ45u9I
                val userRepository = UserRepository()
                LambdaEventsApp(userRepository, deviceId)
            }
        }
    }
}

package com.example.lambda_bbb_events.view.ui.user_screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.profile.ToggleButton
import kotlinx.coroutines.launch

/*
Provides togglable button to turn on or turn off notifications
 */
/**
 * Displays togglable settings to turn on push notifications
 *
 * @param navController Handles navigation.
 * @param controllers Application controllers.
 */
@Composable
fun Notification_Toggle(
    navController: NavController,
    controllers: AppControllers,
) {
    var isToggled by remember { mutableStateOf(controllers.users.user.notificationsEnabled) }
    val scope = rememberCoroutineScope()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("screen_notification_settings"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenTitle(
                title = "Notification Settings",
                navController = navController,
                showNotifications = false,
                showBack = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // use button function, default value set to On
            ToggleButton(
                isOn = isToggled,
                onToggle = { enabled ->
                    scope.launch{
                        controllers.users.setAllowNotifications(enabled)
                        isToggled = enabled
                    }
                },
                onText="Turn On",
                offText = "Turn Off",
                modifier = Modifier.testTag("toggle_notifications")
            )
        }
    }
}

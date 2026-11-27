package com.example.lambda_bbb_events.view.ui.user_screens.profile

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.profile.ToggleButton
import com.example.lambda_bbb_events.view.ui.components.common.quickToast
import kotlinx.coroutines.launch
import com.example.lambda_bbb_events.model.notification.Notification

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
    val context = LocalContext.current
    var isToggled by remember { mutableStateOf(controllers.users.currentUser?.notificationsEnabled == true
            && NotificationManagerCompat.from(context).areNotificationsEnabled())  }
    val scope = rememberCoroutineScope()
    // value that requests permission from user if notification permission had not been enabled yet
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted){ // if user allows notification with the android prompt
            scope.launch{
                controllers.users.setAllowNotifications(true) // update user preference boolean
                isToggled = true // update button state
                quickToast(context, "App notifications enabled!")
            }
        } else {
            quickToast(
                context,
                "Turn on notifications in Android settings and try again!",
                Toast.LENGTH_LONG)
        }
    }

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
            // toggles setAllowNotification for user notif preference
            ToggleButton(
                isOn = isToggled,
                onToggle = { enabled ->
                    // if a button is pressed but notifications are not enabled in settings
                    if (enabled && !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // launches notification prompt
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        // if a button is pressed, swap boolean value to desired boolean of button
                        // ie: Enable = true and Disable = false
                        scope.launch {
                            controllers.users.setAllowNotifications(enabled)
                            isToggled = enabled
                        }
                    }
                    /* if notification permission on app is enabled, display toast message
                    for in-app confirmation message as the variable setAllowNotifications
                     will handle if user preference on notification */
                    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        quickToast(
                            context,
                            if (enabled) "App notifications enabled!" else "App notifications disabled!"
                        )
                    }
                },
                onText = "Enable",
                offText = "Disable",
                modifier = Modifier.testTag("toggle_notifications")
            )
            // FOR TEST ONLY REMOVE AFTER
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        controllers.notifications.showNotification(
                            Notification(title = "Lottery Information", message = "This is a test")
                        )
                    }
                },
                modifier = Modifier.testTag("button_test_notification")
            ) {
                Text("Send test notification")
            }
        }
    }
}

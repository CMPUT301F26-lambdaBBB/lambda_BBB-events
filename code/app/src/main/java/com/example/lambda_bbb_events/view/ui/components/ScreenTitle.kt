package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Displays a customizable title bar for application screens.
 *
 * The back button returns to the previous
 * screen using the navigation back stack. The notification
 * button navigates to the notifications screen.
 *
 * @param title Text displayed as the screen heading.
 * @param navController The [NavController] used to navigate
 * between screens and return to the previous screen.
 * @param modifier Modifier used to customize the title bar's
 * layout and appearance.
 * @param showBack Whether to display the back button.
 * Defaults to false.
 * @param showNotifications Whether to display the
 * notifications button. Defaults to true.
 */
@Composable
fun ScreenTitle(
    title: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    showNotifications: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        if (showBack) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        Text(
            title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center).testTag("screenTitle")
        )
        if (showNotifications) {
            IconButton(
                onClick = { navController.navigate("notifications") }, // change this to notification screen once implemented
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }
        }
    }
}



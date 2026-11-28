package com.example.lambda_bbb_events.view.ui.components.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Displays the role switcher, title and the notifications button.
 *
 * @param navController Handles screen navigation.
 */
@Composable
fun AdminScreenTitleWithRoleSwitcher(
    navController: NavController,
    title: String,
    showNotifications: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        RoleDropdownSwitcher(
            currentView = "Admin",
            onUserSelected = { navController.navigate("home") },
            onAdminSelected = {},
            modifier = Modifier.align(Alignment.CenterStart)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.Center)
                .testTag("screenTitle")
        )

        if (showNotifications) {
            IconButton(
                onClick = { navController.navigate("notifications") },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

    }
}
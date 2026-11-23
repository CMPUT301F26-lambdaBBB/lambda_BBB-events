
package com.example.lambda_bbb_events.view.ui.user_screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationRow
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationSectionHeader
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle

/*
 * Displays notifications organized by people and categories.
 *
 * TODO: Replace placeholder people and categories with actual data.
 * TODO: Implement notification preference management actions.
 */

/**
 * Displays the application's notification management screen.
 *
 * Organizes notification options by people and categories.
 *
 * @param navController Handles screen navigation.
 */
@Composable
fun NotificationsScreen(navController: NavController) {

    // Placeholder people
    val people = listOf(
        Triple("Midterms", "Muted until tomorrow", Icons.Default.AccountCircle),
        Triple("Admin", "Unmuted", Icons.Default.AccountCircle),
        Triple("Yuta", "Unmuted", Icons.Default.AccountCircle)
    )

    // Placeholder notification categories
    val categories = listOf(
        Triple("Rejection information", "High priority", Icons.Outlined.Info),
        Triple("Lottery results", "Weekly updates", Icons.Outlined.ConfirmationNumber),
        Triple("Cancellations", "Weekly updates", Icons.Outlined.Cancel),
        Triple("Notifications Sent", "Weekly updates", Icons.Outlined.Info)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            ScreenTitle(
                title = "Notification",
                navController = navController,
                showNotifications = false,
                showBack = true
            )

            Text(
                text = "Manage alerts from specific people",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = 4.dp,
                    bottom = 16.dp
                )
            )

            NotificationSectionHeader("People")

            people.forEachIndexed { index, person ->
                NotificationRow(
                    person.first,
                    person.second,
                    onClick = {},
                    leadingIcon = {
                        Icon(
                            imageVector = person.third,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                if (index < people.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            NotificationSectionHeader("Categories")

            categories.forEachIndexed { index, category ->
                NotificationRow(
                    category.first,
                    category.second,
                    onClick = {},
                    leadingIcon = {
                        Icon(
                            imageVector = category.third,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                if (index < categories.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

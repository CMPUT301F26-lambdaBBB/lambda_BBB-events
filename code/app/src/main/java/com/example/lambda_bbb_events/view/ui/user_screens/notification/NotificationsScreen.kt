
package com.example.lambda_bbb_events.view.ui.user_screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.NotificationController
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.notification.HeldRowBox
import com.example.lambda_bbb_events.view.ui.components.notification.HeldRowState
import com.example.lambda_bbb_events.view.ui.components.notification.MutableNotificationRow
import com.example.lambda_bbb_events.view.ui.components.notification.NotificationSectionHeader
import kotlinx.coroutines.delay

/*
 * Displays notifications organized by people and categories.
 *
 * TODO: Replace placeholder people and categories with actual data.
 * TODO: Check NotificationController.isMuted before showing a notification.
 */

/**
 * A person or category that notifications come from.
 *
 * @property id Identifies it when it is muted.
 * @property title Its name.
 * @property unmutedText Displayed below the title while it is not muted.
 * @property icon Displayed next to the title.
 */
private data class NotificationSource(
    val id: String,
    val title: String,
    val unmutedText: String,
    val icon: ImageVector
)

// Placeholder people
private val people = listOf(
    NotificationSource("person_midterms", "Midterms", "Unmuted", Icons.Default.AccountCircle),
    NotificationSource("person_admin", "Admin", "Unmuted", Icons.Default.AccountCircle),
    NotificationSource("person_yuta", "Yuta", "Unmuted", Icons.Default.AccountCircle)
)

// Placeholder notification categories
private val categories = listOf(
    NotificationSource(
        "category_rejection", "Rejection information", "High priority", Icons.Outlined.Info
    ),
    NotificationSource(
        "category_lottery", "Lottery results", "Weekly updates", Icons.Outlined.ConfirmationNumber
    ),
    NotificationSource(
        "category_cancellations", "Cancellations", "Weekly updates", Icons.Outlined.Cancel
    ),
    NotificationSource(
        "category_sent", "Notifications Sent", "Weekly updates", Icons.Outlined.Info
    )
)

/**
 * Displays the application's notification management screen.
 *
 * Organizes notification options by people and categories. Pressing and holding one opens
 * a menu to mute or unmute it.
 *
 * @param navController Handles screen navigation.
 * @param controllers Application controllers.
 */
@Composable
fun NotificationsScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val notifications = controllers.notifications
    val heldRow = remember { HeldRowState() }
    UnmuteWhenMutesEnd(notifications)

    HeldRowBox(heldRow) {
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
                    .verticalScroll(rememberScrollState())
            ) {
                ScreenTitle(
                    title = "Notification",
                    navController = navController,
                    showNotifications = false,
                    showBack = true
                )

                Text(
                    text = "Press and hold a person or category to mute or unmute it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                )

                NotificationSectionHeader("People")
                NotificationSources(people, notifications, heldRow)

                Spacer(modifier = Modifier.height(20.dp))

                NotificationSectionHeader("Categories")
                NotificationSources(categories, notifications, heldRow)
            }
        }
    }
}

/** Displays [sources] in a list, with a line between each of them. */
@Composable
private fun NotificationSources(
    sources: List<NotificationSource>,
    notifications: NotificationController,
    heldRow: HeldRowState
) {
    // The space on the sides leaves room for a row to grow when it is held
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        sources.forEachIndexed { index, source ->
            MutableNotificationRow(
                id = source.id,
                title = source.title,
                unmutedText = source.unmutedText,
                icon = source.icon,
                notifications = notifications,
                heldRow = heldRow
            )

            if (index < sources.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

/** Updates the screen when a temporary mute ends while it is open. */
@Composable
private fun UnmuteWhenMutesEnd(notifications: NotificationController) {
    val nextUnmuteAt = notifications.nextUnmuteAt

    LaunchedEffect(nextUnmuteAt) {
        while (nextUnmuteAt != null && notifications.nextUnmuteAt == nextUnmuteAt) {
            delay(notifications.millisUntil(nextUnmuteAt).coerceAtLeast(MIN_WAIT_MILLIS))
            notifications.removeEndedMutes()
        }
    }

    // The wait above does not count time while the device is asleep
    LifecycleResumeEffect(notifications) {
        notifications.removeEndedMutes()
        onPauseOrDispose {}
    }
}

private const val MIN_WAIT_MILLIS = 1_000L

package com.example.lambda_bbb_events.view.ui.user_screens.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

/*
 * Displays more detailed account and app settings.
 *
 * TODO: Implement the pages for Log-Out
 * TODO: Implement Notification page functionality
 * TODO: Change placeholder profile picture to user profile picture
 */
/**
 * Displays user information and app settings.
 *
 * @param navController Handles navigation.
 * @param controllers Provides the current user.
 */
@Composable
fun Settings(
    navController: NavController,
    controllers: AppControllers
) {
    val user = controllers.users.user
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogOutDialog by remember { mutableStateOf(false) }
    var accountError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val settingsGroups = listOf(
        "APP PREFERENCES" to listOf(
            Triple("Notifications", Icons.Outlined.Notifications, "notif_toggle"),
            Triple("Appearance", Icons.Outlined.Brightness6, "theme_toggle")
        ),

        "ACCOUNT SETTINGS" to listOf(
            Triple("App Policies", Icons.Outlined.ContentPaste, "app_policy"),
            Triple("Edit Profile", Icons.Outlined.Edit, "edit_profile"),
            Triple("Log Out", Icons.AutoMirrored.Outlined.ExitToApp, "log_out"),
            Triple("Delete Account", Icons.Outlined.Delete, "delete_account")
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 520.dp) // screen max 520pixel
                    .fillMaxWidth()
                    .testTag("screen_settings"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    ScreenTitle(
                        title = "Settings",
                        navController = navController,
                        showNotifications = false,
                        showBack = true
                    )
                }

                // user info: placeholder profile picture, name and role
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 16.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile picture",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = user.role.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // adds section header to each group (app preferences and account settings)
                settingsGroups.forEach { (sectionTitle, options) ->
                    item {
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                start = 16.dp,
                                top = 24.dp,
                                bottom = 10.dp
                            )
                        )
                    }

                    // per row entry, includes all necessary information and theme-ing + dividers
                    items(options) { (title, icon, route) ->
                        Column {
                            // whole row navigates to specific route per triple entry
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp)
                                    .clickable {
                                        when (route) {
                                            "delete_account" -> {
                                                showDeleteDialog = true
                                            }
                                            "log_out" -> {
                                                showLogOutDialog = true
                                            }
                                            else -> {
                                                navController.navigate(route)
                                            }
                                        }
                                    }
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // vertical box houses the page icon, name and right arrow indicator
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Alert Prompts for both log out and delete account functions
    if (showDeleteDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            // close dialog when the user taps outside the request box or presses Back.
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account?") },
            text = {
                Text(
                    if (accountError) "Please try again."
                    else "Are you sure? \nThis action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { // starts coroutine so firebase deletion runs without freezing app
                            try {
                                if (controllers.users.deleteAccount()) {
                                    showDeleteDialog = false
                                } else {
                                    accountError = true
                                }
                            } catch (_: Exception) { // a failed execution is caught by catch
                                accountError = true
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // you can't login to any account after logging out since it just navigates to signup for now
    // need to work out way to log out with device id verification not causing re-login issue.
    if (showLogOutDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            // close dialog when the user taps outside the request box or presses Back.
            onDismissRequest = { showLogOutDialog = false },
            title = { Text("Log Out?") },
            text = {
                Text("Please stay :( \nAlso note you cannot log back in so just refresh app")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogOutDialog = false
                        navController.navigate("signup") {
                            popUpTo(navController.graph.startDestinationId) { //
                                inclusive = true // removes previous screens in back stack
                            }
                            launchSingleTop = true // prevent duplicate destination entries
                        }
                    }
                ) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}

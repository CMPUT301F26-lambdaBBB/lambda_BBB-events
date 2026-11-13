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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
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

/*
 * Displays more detailed account and app settings.
 *
 * AI used to change the sizes for icons and text to make everything evenly spaced and positioned.
 * TODO: Implement the pages for the different routes
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

    val settingsGroups = listOf(
        "APP PREFERENCES" to listOf(
            Triple("Notifications", Icons.Outlined.Notifications, "notif_toggle"),
            Triple("Appearance", Icons.Outlined.Brightness6, "theme_toggle"),
            Triple("Language", Icons.Outlined.Language, "test_nav")
        ),

        "ACCOUNT SETTINGS" to listOf(
            Triple("App Policies", Icons.Outlined.ContentPaste, "test_nav"),
            Triple("Edit Profile", Icons.Outlined.Edit, "edit_profile"),
            Triple("Log Out", Icons.AutoMirrored.Outlined.ExitToApp, "test_nav"),
            Triple("Delete Account", Icons.Outlined.Delete, "test_nav")
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

                    // per row entry, includes all necessary information and theming + dividers
                    items(options) { (title, icon, route) ->
                        Column {
                            // whole row navigates to specific route per triple entry
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp)
                                    .clickable {
                                        navController.navigate(route)
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
                                    imageVector = Icons.Default.KeyboardArrowRight,
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
}

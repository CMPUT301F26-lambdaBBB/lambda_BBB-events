
package com.example.lambda_bbb_events.view.ui.user_screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.profile.ProfileRow
import com.example.lambda_bbb_events.view.ui.components.profile.ProfileSectionHeader
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle

/*
 * Displays the current user's profile information and account settings.
 *
 * TODO: Implement the Settings and Logout actions.
 * TODO: Replace the placeholder profile picture when profile image support is added.
 */

/**
 * Displays the user's profile and account options.
 *
 * Shows the user's name, email, and a placeholder profile picture.
 * Provides options for settings, editing the profile, and logging out.
 *
 * @param navController The [NavController] used to navigate between screens.
 * @param controllers The [AppControllers] instance used to retrieve user information.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val user: User = controllers.users.user

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
                .padding(horizontal = 16.dp)
                .testTag("screen_profile")
        ) {
            ScreenTitle(
                title = "Profile",
                navController = navController,
                showNotifications = false,
                showBack = false
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Placeholder for profile picture
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(165.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ProfileSectionHeader("Preferences")

            ProfileRow(
                title = "Settings",
                onClick = {
                    // TODO: Implement Settings navigation.
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            ProfileRow(
                title = "Edit Profile",
                onClick = {
                    navController.navigate("edit_profile")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Spacer(modifier = Modifier.height(25.dp))

            ProfileSectionHeader("Account")

            ProfileRow(
                title = "Logout",
                onClick = {
                    // TODO: Implement Logout.
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

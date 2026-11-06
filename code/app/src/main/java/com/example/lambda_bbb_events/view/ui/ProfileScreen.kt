package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ProfileRow
import com.example.lambda_bbb_events.view.ui.components.ProfileSectionHeader
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle


/**
 * Displays the user's profile and account options.
 *
 * Shows the user's name, email, and a placeholder profile picture.
 * Provides options for settings, editing the profile, and logging out functionality.
 *
 * @param navController The [NavController] used to navigate between screens.
 * @param controllers The [AppControllers] instance used to retrieve user information.
 */
@Composable
fun ProfileScreen(navController: NavController, controllers: AppControllers) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController
            )
        }
    ) { innerPadding ->
        val user: User = controllers.users.user

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .testTag("screen_profile")
        ) {
            ScreenTitle(
                title = "Profile",
                navController = navController,
                showNotifications = false,
                showBack = false
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(165.dp)
                )

                Text(
                    text = user.name,
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = user.email,
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 16.sp,
                    color = Color.Gray
                )

            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            ProfileSectionHeader("Preferences")
            ProfileRow(
                title = "Settings",
                onClick = {},
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
            )
            ProfileRow(
                title = "Edit Profile",
                onClick = { navController.navigate("edit_profile") },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(25.dp))

            ProfileSectionHeader("Account")
            ProfileRow(
                title = "Logout",
                onClick = {},
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}
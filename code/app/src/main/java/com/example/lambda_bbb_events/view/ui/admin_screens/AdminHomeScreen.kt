package com.example.lambda_bbb_events.view.ui.admin_screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
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
import com.example.lambda_bbb_events.model.user.UserRole
import com.example.lambda_bbb_events.view.ui.components.admin.AdminScreenTitleWithRoleSwitcher
import com.example.lambda_bbb_events.view.ui.components.admin.AdminToolTile
import com.example.lambda_bbb_events.view.ui.components.admin.NoAccessMessage
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle

/**
 * Displays the home screen for admins. Contains admin tools and recent activity sections.
 *
 * @param navController Handles screen navigation.
 * @param controllers Manages the app controllers.
 */
@Composable
fun AdminHomeScreen(
    navController: NavController,
    controllers: AppControllers,
) {
    val isAdmin = controllers.users.currentUser?.role == UserRole.ADMIN

    Scaffold(
        modifier = Modifier.testTag("admin_home_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
        // TODO: add the admin bottom navigation bar here
    ) { innerPadding ->
        if (!isAdmin) {
            NoAccessMessage(navController)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AdminScreenTitleWithRoleSwitcher(
                    navController = navController,
                    title = "Home",
                    showNotifications = true
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    item {
                        Text(
                            text = "Admin Tools",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 15.dp, bottom = 15.dp)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .padding(bottom = 25.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AdminToolTile(
                                    title = "Campsites",
                                    icon = Icons.Outlined.House,
                                    onClick = { navController.navigate("admin_campsites") },
                                    modifier = Modifier.weight(1f)
                                )
                                AdminToolTile(
                                    title = "Profiles",
                                    icon = Icons.Outlined.Person,
                                    onClick = { navController.navigate("admin_profiles") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AdminToolTile(
                                    title = "Images",
                                    icon = Icons.Outlined.Image,
                                    onClick = { navController.navigate("admin_images") },
                                    modifier = Modifier.weight(1f)
                                )
                                AdminToolTile(
                                    title = "Logs",
                                    icon = Icons.Outlined.Description,
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = "See All",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    // TODO: go to logs i guess
                                }
                            )
                        }
                    }

                    // TODO: recent logs shown

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
package com.example.lambda_bbb_events.view.ui.admin_screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.model.user.UserRole
import com.example.lambda_bbb_events.view.ui.components.admin.AdminProfileListCard
import com.example.lambda_bbb_events.view.ui.components.admin.NoAccessMessage
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.common.SearchBar

/**
 * Displays all user profiles registered in the app. Can search with search bar.
 *
 * @param navController Handles screen navigation.
 * @param controllers All the app controllers for getting user's info.
 */
@Composable
fun AdminProfilesScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val isAdmin = controllers.users.currentUser?.role == UserRole.ADMIN

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(isAdmin) {
        isLoading = true
        if (isAdmin) {
            users = controllers.users.getAllUsers()
        }
        isLoading = false
    }

    var filteredUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    if (query.isBlank()) { filteredUsers = users }
    else {
        val result = mutableListOf<User>()
        for (user in users) {
            if (user.name.contains(query, ignoreCase = true) || user.email.contains(query, ignoreCase = true)) {
                result.add(user)
            }
        }
        filteredUsers = result.sortedBy { it.name.lowercase() } // sort alphabetical
    }

    Scaffold(
        modifier = Modifier.testTag("screen_admin_profiles"),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
        // TODO: add the admin bottom navigation bar here
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScreenTitle(
                title = "Profiles",
                navController = navController,
                showBack = true
            )

            when {
                !isAdmin -> {
                    NoAccessMessage(navController)
                }

                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    SearchBar(
                        label = "Search Profiles",
                        placeholder = "Search profiles...",
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("adminProfileSearch")
                    )

                    if (filteredUsers.isEmpty()) {
                        Text(
                            text = "No profiles found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredUsers, key = { it.deviceId }) { user ->
                                AdminProfileListCard(
                                    name = user.name,
                                    email = user.email,
                                    role = user.role.name,
                                    onClick = {}, // TODO: open a profile detail screen (03.02.01)
                                    modifier = Modifier.testTag("adminProfileItem")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
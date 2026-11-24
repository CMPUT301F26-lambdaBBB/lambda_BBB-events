package com.example.lambda_bbb_events.view.ui.admin_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.user.UserRole
import com.example.lambda_bbb_events.view.ui.components.admin.NoAccessMessage
import com.example.lambda_bbb_events.view.ui.components.campsite.VerticalCampsiteCard
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.common.SearchBar
import com.example.lambda_bbb_events.view.ui.components.filter.HomeCampsiteFilter

/**
 * Displays all user campsites created in the app. Can search with search bar.
 *
 * @param navController Handles screen navigation.
 * @param controllers All the app controllers for getting user's info.
 */
@Composable
fun AdminCampsitesScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val isAdmin = controllers.users.currentUser?.role == UserRole.ADMIN

    var query by remember { mutableStateOf(controllers.campsites.searchQuery) }
    var openFilter by remember { mutableStateOf(false) }

    val results = controllers.campsites.getSearchResults()

    Scaffold(
        modifier = Modifier.testTag("screen_admin_campsites"),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
        // TODO: add the admin bottom navigation bar here
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            ScreenTitle(
                title = "Campsites",
                navController = navController,
                showBack = true
            )

            when {
                !isAdmin -> {
                    NoAccessMessage(navController)
                }

                else -> {
                    SearchBar(
                        label = "Search Campsites",
                        placeholder = "Search campsites...",
                        query = query,
                        onQueryChange = {
                            query = it
                            controllers.campsites.search(it)
                        },
                        onSearch = {
                            controllers.campsites.search(query)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("adminCampsiteSearch")
                    )

                    AssistChip(
                        onClick = {
                            openFilter = true
                        },
                        label = {
                            Text(
                                text = "Filters",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.FilterAlt,
                                contentDescription = "Filter"
                            )
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(results) { campsite ->
                            VerticalCampsiteCard(
                                campsite = campsite,
                                onClick = { navController.navigate("owner_campsite_details/${campsite.campsiteId}") },
                                modifier = Modifier.testTag("adminCampsiteItem")
                            )
                        }
                    }
                }
            }
        }
    }

    if (openFilter) {
        HomeCampsiteFilter(
            campsiteController = controllers.campsites,
            onDismiss = {
                openFilter = false
            },
            onShowResults = {
                openFilter = false
            }
        )
    }
}
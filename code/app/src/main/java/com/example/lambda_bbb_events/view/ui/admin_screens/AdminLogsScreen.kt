package com.example.lambda_bbb_events.view.ui.admin_screens.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.AdminLog
import com.example.lambda_bbb_events.model.LogType
import com.example.lambda_bbb_events.model.user.UserRole
import com.example.lambda_bbb_events.view.ui.components.admin.AdminScreenTitleWithRoleSwitcher
import com.example.lambda_bbb_events.view.ui.components.admin.NoAccessMessage
import com.example.lambda_bbb_events.view.ui.components.logs.AdminLogCard
import com.example.lambda_bbb_events.view.ui.components.logs.AdminLogDialog
import com.example.lambda_bbb_events.view.ui.components.logs.LogSearchBar
import com.example.lambda_bbb_events.view.ui.components.logs.LogTabSelector
import kotlinx.coroutines.CancellationException

/**
 * Displays notification and message logs for administrators.
 *
 * Verifies that the current user has administrative privileges
 * before displaying the logs.
 *
 * @param navController Handles screen navigation.
 * @param controllers Shared application controllers.
 * @param modifier Modifier applied to the screen.
 */
@Composable
fun AdminLogsScreen(
    navController: NavController,
    controllers: AppControllers,
    modifier: Modifier = Modifier
) {
    val isAdmin =
        controllers.users.currentUser?.role == UserRole.ADMIN

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->

        if (!isAdmin) {
            NoAccessMessage(navController)

        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                AdminScreenTitleWithRoleSwitcher(
                    navController = navController,
                    title = "Logs",
                    showNotifications = true
                )

                AdminLogsContent(
                    controllers = controllers,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Displays and manages administrative notification and message logs.
 *
 * Retrieves logs through the admin log controller and resolves
 * user and campsite identifiers into display names.
 *
 * Supports switching between log types, searching logs,
 * retrying failed requests, and reviewing individual log details.
 *
 * @param controllers Shared application controllers.
 * @param modifier Modifier applied to the content.
 */
@Composable
private fun AdminLogsContent(
    controllers: AppControllers,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable {
        mutableStateOf(LogType.NOTIFICATION)
    }

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var logs by remember {
        mutableStateOf<List<AdminLog>>(emptyList())
    }

    var selectedLogId by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var refreshTrigger by remember {
        mutableIntStateOf(0)
    }

    // Retrieve logs when the selected tab changes
    // or the administrator requests a retry.
    LaunchedEffect(selectedTab, refreshTrigger) {

        isLoading = true
        errorMessage = null
        logs = emptyList()

        try {
            logs = controllers.adminLogs.getLogs(selectedTab)

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load logs."

        } finally {
            isLoading = false
        }
    }

    // Map user identifiers to their profile names.
    val profileNames = controllers.users.userRepository.users
        .associate { user ->
            user.deviceId to user.name
        }

    // Map campsite identifiers to their campsite names.
    val campsiteNames = controllers.campsites.campsites
        .mapValues { (_, campsite) ->
            campsite.campsiteName
        }

    // Resolve identifiers without modifying the original logs.
    val resolvedLogs = remember(
        logs,
        profileNames,
        campsiteNames
    ) {
        controllers.adminLogs.resolveLogNames(
            logs = logs,
            profileNames = profileNames,
            campsiteNames = campsiteNames
        )
    }

    // Filter resolved logs without additional database requests.
    val filteredLogs = remember(
        resolvedLogs,
        searchQuery,
        selectedTab
    ) {
        controllers.adminLogs.searchLogs(
            logs = resolvedLogs.filter {
                it.type == selectedTab
            },
            query = searchQuery
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        LogTabSelector(
            selectedTab = selectedTab,
            onTabSelected = { type ->
                if (selectedTab != type) {
                    selectedTab = type
                    searchQuery = ""
                    selectedLogId = null
                    isLoading = true
                    errorMessage = null
                    logs = emptyList()
                }
            }
        )

        LogSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = {
                searchQuery = it
            }
        )

        when {

            // Display a loading indicator.
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Display an error message and retry button.
            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    TextButton(
                        onClick = {
                            refreshTrigger++
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }

            // Display an empty state.
            filteredLogs.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "No matching logs found."
                        } else {
                            when (selectedTab) {
                                LogType.NOTIFICATION ->
                                    "No notification logs available."

                                LogType.MESSAGE ->
                                    "No message logs available."
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                }
            }

            // Display the filtered logs.
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        bottom = 16.dp
                    )
                ) {
                    items(
                        items = filteredLogs,
                        key = { it.id }
                    ) { log ->

                        AdminLogCard(
                            log = log,
                            onClick = {
                                selectedLogId = log.id
                            }
                        )
                    }
                }
            }
        }
    }

    // Retrieve the selected log with its latest resolved names.
    val selectedLog = resolvedLogs.firstOrNull {
        it.id == selectedLogId
    }

    // Display the selected log's complete details.
    selectedLog?.let { log ->
        AdminLogDialog(
            log = log,
            onDismiss = {
                selectedLogId = null
            }
        )
    }
}
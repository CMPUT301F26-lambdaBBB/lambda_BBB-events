package com.example.lambda_bbb_events.view.ui.user_screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.profile.ToggleButton

/**
 * Displays the appearance settings for switching themes.
 *
 * @param navController Handles navigation.
 * @param controllers Application controllers.
 * @param darkTheme Whether dark mode is enabled.
 * @param onDarkThemeChange Updates the app theme.
 */
@Composable
fun Theme_Toggle(
    navController: NavController,
    controllers: AppControllers,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenTitle(
                title = "Appearance",
                navController = navController,
                showNotifications = false,
                showBack = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Light Mode",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))
            // use button function, default value set to light-mode on
            ToggleButton(
                isOn = !darkTheme,
                onToggle = { isLightMode ->
                    onDarkThemeChange(!isLightMode)
                }
            )
        }
    }
}

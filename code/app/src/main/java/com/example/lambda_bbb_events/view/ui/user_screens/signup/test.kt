package com.example.lambda_bbb_events.view.ui.user_screens.signup

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar

/*
 * A Test screen to put in place for work in progress navigation screens
 *
 * TODO: Delete after app is done
 */

/**
 * @param navController The [NavController] used to navigate between screens.
 * @param controllers The [AppControllers] instance used to retrieve user information.
 */

@Composable
fun Test_screen(
    navController: NavController,
    controllers: AppControllers
) {
    val previousPage = navController.previousBackStackEntry
        ?.destination
        ?.route ?: "Unknown"
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    )
    { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("screen_profile"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "This page is currently under development!. Sorry! ;-;",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
    ScreenTitle(
        title = "Came from $previousPage",
        navController = navController,
        showNotifications = false,
        showBack = true
    )
}

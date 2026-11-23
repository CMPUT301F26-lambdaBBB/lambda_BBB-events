package com.example.lambda_bbb_events.view.ui.components.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController

/*
 * Provides back navigation that cannot be triggered more than once per screen.
 */

/**
 * Goes back one screen, but only if [screenLifecycle] belongs to the screen currently shown.
 *
 * The screen currently shown is RESUMED. Once a screen starts leaving, it is no longer
 * RESUMED, so repeated taps while it animates out are ignored instead of popping the
 * screens behind it.
 *
 * @param navController The controller used for navigation.
 * @param screenLifecycle The lifecycle of the screen asking to go back.
 * @return `true` if the app went back, `false` if the request was ignored.
 */
fun goBackIfShown(navController: NavController, screenLifecycle: Lifecycle): Boolean {
    if (screenLifecycle.currentState != Lifecycle.State.RESUMED) {
        return false
    }
    return navController.popBackStack()
}

/**
 * Remembers a back action for the current screen that only works once.
 *
 * Use this for back arrows and for buttons that close a screen, so spamming them
 * cannot pop more than one screen.
 *
 * @param navController The controller used for navigation.
 * @return A callback that goes back one screen if this screen is still the one shown.
 */
@Composable
fun rememberBackAction(navController: NavController): () -> Unit {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return remember(navController, lifecycle) {
        { goBackIfShown(navController, lifecycle) }
    }
}

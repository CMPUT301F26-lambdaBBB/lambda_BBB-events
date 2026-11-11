
package com.example.lambda_bbb_events.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/*
 * Defines the navigation bar in booking screens for switching
 * between active, past, and lottery reservations.
 *
 * Uses Material 3 theme colors for selected and unselected tabs.
 */

/**
 * Displays a navigation bar for switching between booking categories.
 *
 * The bar contains three selectable tabs:
 * - Active: Displays current reservations.
 * - Past: Displays previous reservations.
 * - Lottery: Displays lottery registrations.
 *
 * @param navController Controller responsible for navigation
 * between booking screens and determining the selected tab.
 */
@Composable
fun BookingsTypeBar(navController: NavController) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    val tabs = listOf(
        "Active" to "active_bookings",
        "Past" to "past_bookings",
        "Lottery" to "lottery_bookings"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        tabs.forEach { (title, route) ->
            val isSelected = currentRoute == route

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable {
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

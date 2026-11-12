package com.example.lambda_bbb_events.view.ui.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/*
 * Defines the main bottom navigation bar throughout the app
 * for switching between Home, Explore, Bookings, Create,
 * and Profile screens.
 *
 * Uses the application's Material 3 theme.
 */

/**
 * Represents a destination in the bottom navigation bar.
 *
 * @param title Text displayed below the navigation icon.
 * @param route Route used when the item is clicked.
 * @param icon Icon displayed for the destination.
 * @param selectedRoutes Routes for which this item is selected.
 * @param testTag UI test tag for the navigation item.
 */
private data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val selectedRoutes: List<String>,
    val testTag: String
)

/**
 * Displays the main bottom navigation bar throughout the application.
 *
 * Provides navigation between five sections:
 * - Home
 * - Explore
 * - Bookings
 * - Create
 * - Profile
 *
 * The currently selected section is highlighted using
 * Material 3 theme colors.
 *
 * @param navController Controller responsible for
 * navigating between screens and tracking the current route.
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val navigationItems = listOf(
        BottomNavItem(
            title = "Home",
            route = "home",
            icon = Icons.Outlined.Home,
            selectedRoutes = listOf("home"),
            testTag = "nav_home"
        ),
        BottomNavItem(
            title = "Explore",
            route = "explore_map",
            icon = Icons.Filled.LocationOn,
            selectedRoutes = listOf(
                "explore_map",
                "explore_default"
            ),
            testTag = "nav_explore"
        ),
        BottomNavItem(
            title = "Bookings",
            route = "active_bookings",
            icon = Icons.Outlined.BookmarkBorder,
            selectedRoutes = listOf(
                "active_bookings",
                "past_bookings",
                "lottery_bookings"
            ),
            testTag = "nav_bookings"
        ),
        BottomNavItem(
            title = "Create",
            route = "my_campsites",
            icon = Icons.Outlined.AddCircleOutline,
            selectedRoutes = listOf(
                "create",
                "my_campsites"
            ),
            testTag = "nav_create"
        ),
        BottomNavItem(
            title = "Profile",
            route = "profile",
            icon = Icons.Filled.AccountCircle,
            selectedRoutes = listOf("profile"),
            testTag = "nav_profile"
        )
    )

    val navigationItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(67.dp)
                .clip(RoundedCornerShape(50.dp)),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        modifier = Modifier.testTag(item.testTag),
                        selected = currentRoute in item.selectedRoutes,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = navigationItemColors
                    )
                }
            }
        }
    }
}

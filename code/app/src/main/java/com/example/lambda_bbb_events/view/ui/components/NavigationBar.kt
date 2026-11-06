package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            containerColor = Color(0xFFF5F0FF),
            tonalElevation = 2.dp
        ) {
            NavigationBarItem(
                modifier = Modifier.testTag("nav_home"),
                selected = currentRoute == "home",
                onClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = {
                    Text("Home")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF592A96),
                    selectedTextColor = Color(0xFF592A96),
                    indicatorColor = Color(0xFFD8C4FF),
                    unselectedIconColor = Color(0xFF746B82),
                    unselectedTextColor = Color(0xFF746B82)
                )
            )

            NavigationBarItem(
                modifier = Modifier.testTag("nav_explore"),
                selected = currentRoute == "explore_map" ||
                        currentRoute == "explore_default",
                onClick = {
                    navController.navigate("explore_map") {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = "Explore"
                    )
                },
                label = {
                    Text("Explore")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF592A96),
                    selectedTextColor = Color(0xFF592A96),
                    indicatorColor = Color(0xFFD8C4FF),
                    unselectedIconColor = Color(0xFF746B82),
                    unselectedTextColor = Color(0xFF746B82)
                )
            )

            NavigationBarItem(
                modifier = Modifier.testTag("nav_bookings"),
                selected = currentRoute == "active_bookings" ||
                        currentRoute == "past_bookings" ||
                        currentRoute == "lottery_bookings",
                onClick = {
                    navController.navigate("active_bookings") {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookings"
                    )
                },
                label = {
                    Text("Bookings")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF592A96),
                    selectedTextColor = Color(0xFF592A96),
                    indicatorColor = Color(0xFFD8C4FF),
                    unselectedIconColor = Color(0xFF746B82),
                    unselectedTextColor = Color(0xFF746B82)
                )
            )

            NavigationBarItem(
                modifier = Modifier.testTag("nav_create"),
                selected = currentRoute == "create" ||
                        currentRoute == "my_campsites",
                onClick = {
                    navController.navigate("my_campsites") {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        Icons.Outlined.AddCircleOutline,
                        contentDescription = "Create"
                    )
                },
                label = {
                    Text("Create")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF592A96),
                    selectedTextColor = Color(0xFF592A96),
                    indicatorColor = Color(0xFFD8C4FF),
                    unselectedIconColor = Color(0xFF746B82),
                    unselectedTextColor = Color(0xFF746B82)
                )
            )

            NavigationBarItem(
                modifier = Modifier.testTag("nav_profile"),
                selected = currentRoute == "profile",
                onClick = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = "Profile"
                    )
                },
                label = {
                    Text("Profile")
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF592A96),
                    selectedTextColor = Color(0xFF592A96),
                    indicatorColor = Color(0xFFD8C4FF),
                    unselectedIconColor = Color(0xFF746B82),
                    unselectedTextColor = Color(0xFF746B82)
                )
            )
        }
    }
}

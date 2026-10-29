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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lambda_bbb_events.view.ui.theme.PurpleGrey80
import com.example.lambda_bbb_events.view.ui.theme.Purple80

@Composable
fun BottomNavigationBar(navController: NavController) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    Box (
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            containerColor = _root_ide_package_.com.example.lambda_bbb_events.view.ui.theme.Purple80
        ){
            // Navigation to Home
            NavigationBarItem(
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
                    indicatorColor = _root_ide_package_.com.example.lambda_bbb_events.view.ui.theme.PurpleGrey80
                )
            )

            // Navigation to Explore
            NavigationBarItem(
                selected = currentRoute == "explore",
                onClick = {
                    navController.navigate("explore") {
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
                    indicatorColor = _root_ide_package_.com.example.lambda_bbb_events.view.ui.theme.PurpleGrey80
                )
            )

            // Navigation to Bookings
            NavigationBarItem(
                selected = currentRoute == "active_bookings"
                        || currentRoute == "past_bookings"
                        || currentRoute == "lottery_bookings",
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
                    indicatorColor = _root_ide_package_.com.example.lambda_bbb_events.view.ui.theme.PurpleGrey80
                )
            )

            // Navigation to Create
            NavigationBarItem(
                selected = currentRoute == "create",
                onClick = {
                    navController.navigate("create") {
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
                    indicatorColor = _root_ide_package_.com.example.lambda_bbb_events.view.ui.theme.PurpleGrey80
                )
            )

            // Navigation to Bookings
            NavigationBarItem(
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
                    indicatorColor = _root_ide_package_.com.example.lambda_bbb_events.view.ui.theme.PurpleGrey80
                )
            )
        }
    }
}
package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
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
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ClickableMap
import com.example.lambda_bbb_events.view.ui.components.HomeCampsiteFilter
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SearchBar
import com.example.lambda_bbb_events.view.ui.components.VerticalCardSection



/**
 * Displays the campsite exploration screen with an interactive map.
 *
 * Allows users to search, filter, and browse campsites.
 * Map selections update the search location, while campsite
 * selections navigate to their details screens.
 *
 * @param navController Handles screen navigation.
 * @param controllers Manages campsite searches and filters.
 */
@Composable
fun ExploreMapScreen(navController: NavController, controllers: AppControllers) {
    var query by remember { mutableStateOf("") }
    var openFilter by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).testTag("screen_explore")
        ) {
            ScreenTitle(
                title = "Explore",
                navController = navController
            )
            SearchBar(
                label = "Search Campsites",
                placeholder = "Search campsites...",
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    controllers.campsites.search(query)
                    navController.navigate("explore_default")
                },
                modifier = Modifier.fillMaxWidth()
            )

            AssistChip(
                onClick = {
                    openFilter = true
                },
                label = {
                    Text("Filters")
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.FilterAlt,
                        contentDescription = "Filter"
                    )
                },
                shape = RoundedCornerShape(100.dp)
            )

            // Clickable Google Map: when tapped, updates the search bar query with the address
            ClickableMap(
                height = 300,
                onLocationSelected = { coordinates, address, postalCode ->
                    if (address.isNotBlank()) {
                        query = address
                    } else if (postalCode.isNotBlank()) {
                        query = postalCode
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            VerticalCardSection(
                title = "Nearby Campsites",
                campsites = controllers.campsites.campsites,
                onCampsiteClick = { navController.navigate("campsite_details/${it.campsiteId}") },
                onSeeAll = {
                    controllers.campsites.search("")
                    navController.navigate("explore_default")
                },
                modifier = Modifier.weight(1f)
            )
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
                navController.navigate("explore_default") {
                    launchSingleTop = true
                }
            }
        )
    }
}

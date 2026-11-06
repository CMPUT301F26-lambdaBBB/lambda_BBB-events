package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.HorizontalCardSection
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.FilterIconButton
import com.example.lambda_bbb_events.view.ui.components.HomeCampsiteFilter
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SearchBar

/*
 * Defines the ome screen, has campsite
 * searching, filtering, and featured listing sections.
 *
 * TODO: Display actual recently viewed and featured campsites.
 * TODO: Implement the See All actions for both sections.
 */

/**
 * Displays the application's home screen for browsing campsites.
 *
 * Allows users to search and filter campsites, with sections
 * for recently viewed and featured listings.
 *
 * @param navController Handles screen navigation.
 * @param controllers Manages campsite searches and filters.
 */
@Composable
fun HomeScreen(navController: NavController, controllers: AppControllers) {
    var query by remember { mutableStateOf("") }
    var openFilter by remember {mutableStateOf(false)}

    Scaffold(
        modifier = Modifier.testTag("screen_home"),
        bottomBar = {
            BottomNavigationBar(
                navController
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            ScreenTitle(
                title = "Home",
                navController = navController
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SearchBar(
                    label = "Search Campsites",
                    placeholder = "Search campsites...",
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = {
                        controllers.campsites.search(query)
                        navController.navigate("explore_default")
                    },
                    modifier = Modifier.fillMaxWidth(0.85f).testTag("searchInput")
                )

                FilterIconButton(
                    onClick = {
                        openFilter = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Box (modifier = Modifier.testTag("recentlyViewedSection")) {
                HorizontalCardSection(
                    "Recently Viewed",
                    onSeeAll = { /* do something */ }
                )
            }

            Box (modifier = Modifier.testTag("featuredCampsitesSection")) {
                HorizontalCardSection(
                    "Featured Campsites",
                    onSeeAll = { /* do something */ }
                )
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
                navController.navigate("explore_default") {
                    launchSingleTop = true
                }
            }
        )
    }

}

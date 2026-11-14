
package com.example.lambda_bbb_events.view.ui.user_screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.map.ClickableMap
import com.example.lambda_bbb_events.view.ui.components.filter.HomeCampsiteFilter
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.common.SearchBar
import com.example.lambda_bbb_events.view.ui.components.campsite.VerticalCardSection

/*
 * Provides a map-based campsite exploration screen with
 * searching and filtering.
 *
 * TODO: Filter nearby campsites based on the selected
 * map location instead of displaying all campsites.
 * TODO: Implement use of coordinate parameter.
 */

/**
 * Displays the campsite exploration screen with an interactive map.
 *
 * Allows users to search, filter, and browse campsites.
 * Map selections update the search location, while campsite
 * selections navigate to their details screens.
 *
 * The map and nearby campsite listings scroll vertically,
 * while the search controls and bottom navigation remain fixed.
 *
 * @param navController Handles screen navigation.
 * @param controllers Manages campsite searches and filters.
 * @param showMap Whether to display the interactive map.
 * Defaults to true.
 */
@Composable
fun ExploreMapScreen(
    navController: NavController,
    controllers: AppControllers,
    showMap: Boolean = true
) {
    var query by remember { mutableStateOf("") }
    var openFilter by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .testTag("screen_explore")
        ) {
            ScreenTitle(
                title = "Explore",
                navController = navController
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchBar(
                    label = "Search Campsites",
                    placeholder = "Search campsites...",
                    query = query,
                    onQueryChange = {
                        query = it
                    },
                    onSearch = {
                        controllers.campsites.search(query)
                        navController.navigate("explore_default")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("exploreSearchInput")
                )

                IconButton(
                    onClick = {
                        navController.navigate("qr_scanner")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Scanner",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AssistChip(
                onClick = {
                    openFilter = true
                },
                label = {
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.FilterAlt,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                shape = RoundedCornerShape(100.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            // Scrollable map and campsite listings
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /**
                 * Displays Google Maps within the application.
                 *
                 * Reference:
                 * Google Maps SDK for Android documentation.
                 * https://developers.google.com/maps/documentation/android-sdk/overview
                 */
                if (showMap) {
                    ClickableMap(
                        height = 300,
                        onLocationSelected = { _, address, postalCode ->
                            // TODO: Use coordinates to filter nearby campsites.

                            if (address.isNotBlank()) {
                                query = address
                            } else if (postalCode.isNotBlank()) {
                                query = postalCode
                            }
                        }
                    )
                }

                // Nearby Campsites
                VerticalCardSection(
                    title = "Nearby Campsites",
                    campsites = controllers.campsites.campsites.values.toList(),
                    onCampsiteClick = { campsite ->
                        navController.navigate(
                            "campsite_details/${campsite.campsiteId}"
                        )
                    },
                    onSeeAll = {
                        controllers.campsites.search("")
                        navController.navigate("explore_default")
                    },
                    modifier = Modifier.fillMaxWidth()
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

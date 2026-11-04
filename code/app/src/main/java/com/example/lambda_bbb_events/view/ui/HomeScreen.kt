package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.HorizontalCardSection
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.FilterIconButton
import com.example.lambda_bbb_events.view.ui.components.HomeCampsiteFilter
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SearchBar

@Composable
fun HomeScreen(navController: NavController, controllers: AppControllers) {
    var query by remember { mutableStateOf("") }
    var openFilter by remember {mutableStateOf(false)}

    Scaffold(
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
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                FilterIconButton(
                    onClick = {
                        openFilter = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            HorizontalCardSection(
                "Recently Viewed",
                onSeeAll = { /* do something */ })
            HorizontalCardSection(
                "Featured Campsites",
                onSeeAll = { /* do something */ })


        }
    }

    if (openFilter == true) {
        HomeCampsiteFilter(
            onDismiss = {
                openFilter = false
            }
        )
    }
}

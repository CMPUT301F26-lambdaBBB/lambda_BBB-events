package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.CampsiteController
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.FilterButton
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SearchBar
import com.example.lambda_bbb_events.view.ui.components.VerticalCardSection

@Composable
fun ExploreMapScreen(navController: NavController, campsiteController: CampsiteController) {
    var query by remember { mutableStateOf("") }

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
                title = "Explore",
                navController = navController
            )
            SearchBar(
                label = "Search Campsites",
                placeholder = "Search campsites...",
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    campsiteController.search(query)
                    navController.navigate("explore_default")
                }
            )
            FilterButton()

            // google maps placeholder
            Box(
                modifier = Modifier
                    .height(300.dp)
                    .background(
                        color = Color.Gray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("gooogel map")
            }

            Spacer(modifier = Modifier.height(20.dp))

            VerticalCardSection(
                title = "Nearby Campsites",
                campsites = campsiteController.campsites, // all for now, we dont have a way to get "nearby" yet. needs location
                onCampsiteClick = { navController.navigate("campsite_details/${it.campsiteId}") },
                onSeeAll = {
                    campsiteController.search("")
                    navController.navigate("explore_default")
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

}
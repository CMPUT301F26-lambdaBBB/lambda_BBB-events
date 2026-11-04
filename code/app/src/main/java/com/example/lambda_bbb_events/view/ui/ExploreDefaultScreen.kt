package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SearchBar
import com.example.lambda_bbb_events.view.ui.components.VerticalCampsiteCard

@Composable
fun ExploreDefaultScreen(navController: NavController, controllers: AppControllers) {
    var query by remember { mutableStateOf(controllers.campsites.searchQuery) }
    val results = remember(query) {
        controllers.campsites.search(query)
        controllers.campsites.getSearchResults()
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController
            )
        }
    ){ innerPadding ->
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
                onSearch = { controllers.campsites.search(query) },
                modifier = Modifier.fillMaxWidth()
            )
            AssistChip(
                onClick = { /* open filters */ },
                label = { Text("Filters", fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(Icons.Outlined.FilterAlt, contentDescription = "Filter", tint = Color.Black)
                },
                shape = RoundedCornerShape(100.dp)
            )

            Spacer(modifier = Modifier.height(11.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(results) { campsite ->
                    VerticalCampsiteCard(
                        campsite = campsite,
                        onClick = { navController.navigate("campsite_details/${campsite.campsiteId}") }
                    )
                }
            }
        }
    }

}
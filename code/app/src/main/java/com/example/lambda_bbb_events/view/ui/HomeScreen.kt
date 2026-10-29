package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.view.ui.components.HorizontalCardSection
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SearchBar

@Composable
fun HomeScreen(navController: NavController) {
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
            SearchBar(
                "Search Campsites",
                "Search campsites..."
            )

            Spacer(modifier = Modifier.height(25.dp))

            HorizontalCardSection(
                "Recently Viewed",
                onSeeAll = { /* do something */ })
            HorizontalCardSection(
                "Featured Campsites",
                onSeeAll = { /* do something */ })
        }
    }
}

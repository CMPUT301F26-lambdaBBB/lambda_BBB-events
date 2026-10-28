package com.example.lambda_bbb_events.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.ui.components.HorizontalCardSection
import com.example.lambda_bbb_events.ui.components.ScreenTitle
import com.example.lambda_bbb_events.ui.components.SearchBar

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScreenTitle(title = "Home", navController = navController)
        SearchBar("Search Campsites", "Search campsites...")

        Spacer(modifier = Modifier.height(25.dp))

        HorizontalCardSection("Recently Viewed", onSeeAll = { /* do something */ })
        HorizontalCardSection("Featured Campsites", onSeeAll = { /* do something */ })
    }
}

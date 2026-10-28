package com.example.lambda_bbb_events.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.ui.components.ScreenTitle
import com.example.lambda_bbb_events.ui.components.SearchBar
import com.example.lambda_bbb_events.ui.components.VerticalCampsiteCard

@Composable
fun ExploreDefaultScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            ScreenTitle(title = "Explore", navController = navController)
            SearchBar("Search Campsites", "Search campsites...")
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
                items(10) {
                    // idk if u guys want to make the search bar scroll up with the cards
                    VerticalCampsiteCard()
                }
            }
        }
    }

}
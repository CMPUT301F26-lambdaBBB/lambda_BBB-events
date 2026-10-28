package com.example.lambda_bbb_events.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.ui.components.FilterButton
import com.example.lambda_bbb_events.ui.components.ScreenTitle
import com.example.lambda_bbb_events.ui.components.SearchBar
import com.example.lambda_bbb_events.ui.components.VerticalCampsiteCard

@Composable
fun ExploreDefaultScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScreenTitle(title = "Explore", navController = navController)
        SearchBar("Search Campsites", "Search campsites...")
        FilterButton()

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
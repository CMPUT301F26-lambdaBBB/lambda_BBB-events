package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.view.ui.components.FilterButton
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SearchBar
import com.example.lambda_bbb_events.view.ui.components.VerticalCardSection

@Composable
fun ExploreMapScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        _root_ide_package_.com.example.lambda_bbb_events.view.ui.components.ScreenTitle(
            title = "Explore",
            navController = navController
        )
        _root_ide_package_.com.example.lambda_bbb_events.view.ui.components.SearchBar(
            "Search Campsites",
            "Search campsites..."
        )
        _root_ide_package_.com.example.lambda_bbb_events.view.ui.components.FilterButton()

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

        _root_ide_package_.com.example.lambda_bbb_events.view.ui.components.VerticalCardSection(
            "Nearby Campsites",
            onSeeAll = {})
    }
}
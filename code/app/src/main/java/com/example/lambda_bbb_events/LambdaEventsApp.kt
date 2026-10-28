package com.example.lambda_bbb_events

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lambda_bbb_events.ui.CampsiteDetailsScreen
import com.example.lambda_bbb_events.ui.ExploreScreen
import com.example.lambda_bbb_events.ui.HomeScreen

@Composable
fun LambdaEventsApp() {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "campsiteDetails",
            modifier = Modifier.padding(innerPadding).padding(all = 32.dp)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("explore") { ExploreScreen(navController) }
            composable("campsiteDetails") { CampsiteDetailsScreen(navController) }
        }
    }
}
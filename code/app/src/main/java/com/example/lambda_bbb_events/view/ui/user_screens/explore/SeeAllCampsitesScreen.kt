package com.example.lambda_bbb_events.view.ui.user_screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.view.ui.components.campsite.SeeAllCampsiteCard
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle

/**
 * Displays all campsites belonging to a selected section.
 *
 * @param navController The application's navigation controller.
 * @param section The selected campsite section.
 * @param campsites The campsites to display.
 */
@Composable
fun SeeAllCampsitesScreen(
    navController: NavController,
    section: CampsiteSection,
    campsites: List<Campsite>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScreenTitle(
            title = section.title,
            navController = navController,
            showBack = true
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "${campsites.size} campsites",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        if (campsites.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No campsites found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = campsites,
                    key = { it.campsiteId }
                ) { campsite ->

                    SeeAllCampsiteCard(
                        campsite = campsite,
                        onClick = {
                            navController.navigate(
                                "campsite_details/${campsite.campsiteId}"
                            )
                        }
                    )
                }
            }
        }
    }
}
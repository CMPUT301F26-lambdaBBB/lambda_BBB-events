package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.NotificationRow
import com.example.lambda_bbb_events.view.ui.components.NotificationSectionHeader
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import kotlin.repeat

@Composable
fun NotificationsScreen(navController: NavController) {
    // placeholders: replace these lists with actual data (later we'll have user class, you can take names and images there).
    // for now each inner list is [name, status, Icon/Image].
    val people = listOf(
        listOf("Midterms", "Muted until tomorrow", Icons.Default.AccountCircle),
        listOf("Admin", "Unmuted", Icons.Default.AccountCircle),
        listOf("Yuta", "Unmuted", Icons.Default.AccountCircle)
    )

    // placeholders: replace with actual data, probably will have a class for this.
    // for now each inner list is [title, status, Icon/Image].
    val categories = listOf(
        listOf("Rejection information", "High priority", Icons.Outlined.Info),
        listOf("Lottery results", "Weekly updates", Icons.Outlined.ConfirmationNumber),
        listOf("Cancellations", "Weekly updates", Icons.Outlined.Cancel),
        listOf("Notifications Sent", "Weekly updates", Icons.Outlined.Info)
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScreenTitle(
                title = "Notification",
                navController = navController,
                showNotifications = false,
                showBack = true
            )

            Text(
                text = "Manage alerts from specific people",
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp),
                fontSize = 16.sp
            )

            NotificationSectionHeader(
                "People"
            )
            repeat(people.size) { i ->
                NotificationRow(
                    people[i][0] as String,
                    people[i][1] as String,
                    onClick = {},
                    leadingIcon = { // change below to image data (later we'll make a class for it)
                        Icon(
                            people[i][2] as ImageVector,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                )
                if (i < people.lastIndex) HorizontalDivider()
            }

            Spacer(modifier = Modifier.height(20.dp))

            NotificationSectionHeader(
                "Categories"
            )
            repeat(categories.size) { i ->
                NotificationRow(
                    categories[i][0] as String,
                    categories[i][1] as String,
                    onClick = {},
                    leadingIcon = {
                        Icon(
                            categories[i][2] as ImageVector,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                )
                if (i < categories.lastIndex) HorizontalDivider()
            }
        }
    }
}
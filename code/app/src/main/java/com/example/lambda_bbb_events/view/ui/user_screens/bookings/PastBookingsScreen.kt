package com.example.lambda_bbb_events.view.ui.user_screens.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.common.SubTitle
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar

/*
 * Provides the past bookings screen with navigation
 * between reservation categories.
 *
 * TODO: Retrieve and display the user's past reservations.
 */


/**
 * Displays the user's past bookings screen.
 *
 * Provides navigation between reservation categories
 * and an empty scrollable list for past reservations.
 *
 * @param navController The [NavController] used to navigate
 * between reservation categories and application screens.
 */
@Composable
fun PastBookingsScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).testTag("past_bookings")
        ) {
            ScreenTitle(
                title = "My Reservations",
                navController = navController
            )
            BookingsTypeBar(navController)
            Spacer(modifier = Modifier.height(25.dp))
            SubTitle(title = "Past Reservations")
            Spacer(modifier = Modifier.height(25.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
//                items(10) {
//                    VerticalCampsiteCard()
//                }
            }
        }
    }
}

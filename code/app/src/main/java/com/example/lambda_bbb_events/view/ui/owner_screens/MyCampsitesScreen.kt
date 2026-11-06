package com.example.lambda_bbb_events.view.ui.owner_screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.VerticalCampsiteCard
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


/*
 * Displays campsites owned by the current user and provides
 * navigation for creating campsites and managing lotteries.
 *
 * owner registration before campsite creation might need some work
 */

/**
 * Displays the current user's owned campsites.
 *
 * Allows users to view campsite details, create new listings,
 * and manage lotteries for their first campsite.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves campsite data and manages owner permissions.
 */
@Composable
fun MyCampsitesScreen(navController: NavController, controllers: AppControllers) {
    val context = LocalContext.current
    val user: User? = controllers.users.currentUser
    val myCampsites: List<Campsite> = controllers.campsites.getCampsitesByOwner(user?.deviceId ?: "")
    val scope = rememberCoroutineScope()

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
                title = "My Campsites",
                navController = navController
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            controllers.users.becomeOwner()
                            navController.navigate("create_campsite")
                        }
                    }
                ) {
                    Text("+ Create Campsite")
                }
                Button(
                    onClick = {
                        val firstCampsiteId = myCampsites.firstOrNull()?.campsiteId ?: ""
                        if (firstCampsiteId.isNotEmpty()) {
                            navController.navigate("manage_lottery/$firstCampsiteId")
                        } else {
                            Toast.makeText(context, "No campsite is created yet", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Manage Lottery")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myCampsites) { campsite ->
                    VerticalCampsiteCard(
                        campsite = campsite,
                        onClick = { navController.navigate("owner_campsite_details/${campsite.campsiteId}") }
                    )
                }
            }
        }
    }
}
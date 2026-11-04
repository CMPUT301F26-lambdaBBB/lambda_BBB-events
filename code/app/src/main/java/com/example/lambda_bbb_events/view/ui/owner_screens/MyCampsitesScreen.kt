package com.example.lambda_bbb_events.view.ui.owner_screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.VerticalCampsiteCard

@Composable
fun MyCampsitesScreen(navController: NavController, controllers: AppControllers) {
    val user: User = controllers.users.user
    val myCampsites: List<Campsite> = controllers.campsites.getCampsitesByOwner(user.deviceId)

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
            Button(
                onClick = {
                    controllers.users.becomeOwner()
                    navController.navigate("create_campsite")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Create Campsite")
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
package com.example.lambda_bbb_events.view.ui.owner_screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers

@Composable
fun CreateLotteryScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)
}
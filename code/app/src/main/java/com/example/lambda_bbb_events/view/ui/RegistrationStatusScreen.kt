package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.RegistrationInformationBox
import com.example.lambda_bbb_events.view.ui.components.RegistrationStatusBox
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.theme.Pink40
import java.time.LocalDate

@Composable
fun RegistrationStatusScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String,
    fromDate: LocalDate,
    endDate: LocalDate,
    numPeople: Int
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Registration Status",
            navController = navController,
            showBack = true
        )

        RegistrationStatusBox(
            registrationStatus = "Lottery Required",
            message = "Join Lottery to win the Ticket to the campsite",
            boxColor = Pink40
        )

        RegistrationInformationBox(
            campsiteName = campsite?.campsiteName ?: "Unknown Campsite",
            fromDate = fromDate,
            endDate = endDate,
            numPeople = numPeople
        )
    }
}
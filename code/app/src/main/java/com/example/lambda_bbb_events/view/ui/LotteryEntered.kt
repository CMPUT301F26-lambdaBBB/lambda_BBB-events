package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.RegistrationInformationBox
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import java.time.LocalDate



/**
 * Displays a confirmation of successful lottery entry.
 *
 * Shows the campsite and reservation details, with
 * an option to return home.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves campsite information.
 * @param campsiteId The ID of the selected campsite.
 * @param fromDate The reservation start date.
 * @param endDate The reservation end date.
 * @param numPeople The number of guests.
 */
@Composable
fun LotteryEnteredScreen(
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitle(
            title = "Lottery Entered",
            navController = navController,
            showBack = true
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Green success circle
        Box(
            modifier = Modifier
                .size(180.dp)
                .border(
                    width = 6.dp,
                    color = Color(0xFFD5F9DE),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Successfully entered",
                tint = Color(0xFF00A51B),
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        RegistrationInformationBox(
            campsiteName = campsite?.campsiteName ?: "Unknown Campsite",
            fromDate = fromDate,
            endDate = endDate,
            numPeople = numPeople
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B2BD8)
            )
        ) {
            Text("Go Home")
        }
    }
}

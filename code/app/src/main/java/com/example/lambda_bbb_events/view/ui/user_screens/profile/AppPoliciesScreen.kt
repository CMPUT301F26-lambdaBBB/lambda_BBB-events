package com.example.lambda_bbb_events.view.ui.user_screens.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle

/*
 * Displays app policy and guidelines.
 */

/**
 * Displays app policies.
 *
 * @param navController Handles navigation.
 * @param controllers Shared app controllers.
 */

@Composable
fun AppPoliciesScreen(
    navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen_app_policies")
            .padding(horizontal = 16.dp)
    ) {
        ScreenTitle(
            title = "App Policy",
            navController = navController,
            showBack = true,
            showNotifications = false
        )
        // scrollable lazy column for easy reading
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 520.dp) // screen max 520pixel
                .fillMaxWidth()
                .testTag("app_policy"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ){
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                )
                {
                    Text(
                        text = "1. General Use",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Lambda Camp is designed to help users discover campsites, participate in reservation lotteries, and manage their bookings. By using this app, users agree to follow its policies and guidelines to ensure a fair and enjoyable experience for everyone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "2. User Responsibilities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Users must provide accurate personal information, including their name, email address, and phone number. Users are responsible for keeping their information updated and using the app appropriately.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "3. Lottery and Reservation Rules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Campsite reservations are allocated through a lottery system. Users may enter lotteries during the designated registration period. Winners will be notified and given an opportunity to select available reservation dates. Winning a lottery does not automatically guarantee a reservation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "4. Cancellations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Users may cancel their reservations through the app. Once a reservation is cancelled, the campsite may become available to other eligible users. Users should review their reservation details carefully before confirming a cancellation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "5. Privacy and Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "CampEasy collects personal information necessary to manage profiles, lottery entries, reservations, and notifications. Personal information should only be used for authorized app-related purposes. Users may update or request deletion of their profiles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "6. Community Guidelines",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Users must treat campsite owners and other participants respectfully. Harassment, fraudulent activity, and misuse of the reservation system are prohibited. Users must follow the specific rules established by campsite owners during their stay.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "7. Campsite Owner Responsibilities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Campsite owners are responsible for providing accurate descriptions, locations, occupancy limits, and availability information. Owners must maintain their listings and clearly communicate campsite rules to users before reservations are confirmed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "8. Notifications and Communication",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "CampEasy may send notifications about lottery results, reservation confirmations, cancellations, and important updates. Users can manage their notification preferences through their profile settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "9. Policy Updates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "These policies may be updated as the application develops. Users are encouraged to review the latest guidelines regularly to stay informed about their responsibilities and any changes to the reservation process.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Button(
                        modifier = Modifier
                        .fillMaxWidth()
                        .testTag("button_accept_policy"),
                        colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        onClick = {
                            navController.navigate("settings")
                        }
                    ) {
                        Text("Accept")
                    }

                }
            }
        }
    }
}
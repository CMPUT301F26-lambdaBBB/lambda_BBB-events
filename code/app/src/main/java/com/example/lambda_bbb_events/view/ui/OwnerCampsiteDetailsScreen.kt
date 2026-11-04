package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.RatingSummary
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle

@Composable
fun OwnerCampsiteDetailsScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)

    if (campsite == null) {
        Text("Campsite not found")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ScreenTitle(
                    campsite.campsiteName,
                    navController,
                    showBack = true
                )

                // Image carousel placeholder
                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .background(
                            color = Color.Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("some images here")
                }

                RatingSummary(
                    campsite.rating,
                    campsite.ratingCount
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = campsite.address,
                        fontSize = 15.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Max People",
                        modifier = Modifier.size(28.dp)
                    )
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy((-8).dp)
                    ) {
                        Text(
                            text = "Max Occupancy",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${campsite.maxOccupancy} people",
                            fontSize = 15.sp
                        )
                    }

                }

                // action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OwnerActionButton(
                        label = "Edit",
                        icon = Icons.Outlined.Edit,
                        backgroundColor = Color(0xFFEDE7FF),
                        contentColor = Color.Black,
                        onClick = { /* open edit screen */ },
                        modifier = Modifier.weight(1f)
                    )
                    OwnerActionButton(
                        label = "View",
                        icon = Icons.Outlined.Visibility,
                        backgroundColor = Color(0xFFEDE7FF),
                        contentColor = Color.Black,
                        onClick = { navController.navigate("campsite_details/${campsite.campsiteId}") },
                        modifier = Modifier.weight(1f)
                    )
                    OwnerActionButton(
                        label = "Manage Lottery",
                        icon = Icons.Outlined.ManageAccounts,
                        backgroundColor = Color(0xFFEDE7FF),
                        contentColor = Color.Black,
                        onClick = { /* open lottery dashboard screen */ },
                        modifier = Modifier.weight(1f)
                    )
                    OwnerActionButton(
                        label = "Delete",
                        icon = Icons.Outlined.Delete,
                        backgroundColor = Color(0xFFFADADD),
                        contentColor = Color(0xFFB3261E),
                        onClick = { /* ask if want to confirm delete */ },
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider()

                SectionTitle("Upcoming Reservations")
                // reservations placeholder
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .background(
                            color = Color.Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("upcoming")
                }
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .background(
                            color = Color.Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("lalalalala")
                }

                HorizontalDivider()

                SectionTitle("Past Reservations")
                // reservations placeholder
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .background(
                            color = Color.Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("pssst")
                }

                HorizontalDivider()

                SectionTitle("Cancelled Reservations")
                // reservations placeholder
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .background(
                            color = Color.Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("im a can")
                }
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .background(
                            color = Color.Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("canada")
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun OwnerActionButton(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            maxLines = 2
        )
    }
}
package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.RatingSummary
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle


/**
 * Displays detailed information about a selected campsite.
 *
 * Shows campsite tags, description, and placeholders for images,
 * QR code, and map. Allows users to check campsite availability.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves campsite information.
 * @param campsiteId The ID of the selected campsite.
 */
@Composable
fun CampsiteDetailsScreen(
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

                HorizontalDivider()

                SectionTitle("Tags")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (tag in campsite.tags) {
                        DisplayTag(tag, Color(0xfff5f5f5), Color(0xffe5e5e5))
                    }
                }

                HorizontalDivider()

                SectionTitle("Campsite QR Code")
                // QR code placeholder
                Box(
                    modifier = Modifier
                        .height(160.dp)
                        .background(
                            color = Color.Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("qr code")
                }

                HorizontalDivider()

                SectionTitle("Description")
                Text(campsite.description)

                HorizontalDivider()

                // google maps placeholder
                SectionTitle("Map Location")
                Box(
                    modifier = Modifier
                        .height(320.dp)
                        .background(
                            color = Color.Gray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("gooogel map")
                }

                HorizontalDivider()

                Button(
                    onClick = { navController.navigate("campsite_check_availability/${campsite.campsiteId}") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check Availability")
                }

                // TODO: review section
            }
        }
    }
}

/**
 * Displays a heading for a section of the application.
 *
 * @param text The text displayed as the section heading.
 */
@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold)
}

/**
 * Displays a campsite tag with customizable colors.
 *
 * @param text The text displayed inside the tag.
 * @param bgColor The background color of the tag.
 * @param borderColor The color of the tag's border.
 */
@Composable
fun DisplayTag(
    text: String,
    bgColor: Color,
    borderColor: Color
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = borderColor,
            shape = RoundedCornerShape(16.dp)
        )
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
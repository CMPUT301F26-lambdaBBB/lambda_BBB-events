package com.example.lambda_bbb_events.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
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
import com.example.lambda_bbb_events.ui.components.RatingStars
import com.example.lambda_bbb_events.ui.components.RatingSummary
import com.example.lambda_bbb_events.ui.components.ScreenTitle

@Composable
fun CampsiteDetailsScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ScreenTitle("Yuta Land", navController, showBack = true)

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

                RatingSummary(4.0f, 15)

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
                        text = "some address here lallalalalal street oh my goodness ABC DEF",
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
                            text = "25 people",
                            fontSize = 15.sp
                        )
                    }

                }

                HorizontalDivider()

                SectionTitle("Tags")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // replace the hardcoded colors
                    DisplayTag("has internet", Color(0xfff5f5f5), Color(0xffe5e5e5))
                    DisplayTag("campfire pit", Color(0xfff5f5f5), Color(0xffe5e5e5))
                    DisplayTag("lake access", Color(0xfff5f5f5), Color(0xffe5e5e5))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DisplayTag("stargazing", Color(0xfff5f5f5), Color(0xffe5e5e5))
                    DisplayTag("dog friendly", Color(0xfff5f5f5), Color(0xffe5e5e5))
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
                Text("The Yuta Man Site stands as the ultimate, undisputed sanctuary for Yuta...")

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

                Button(onClick = { /* check availability */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Check Availability")
                }

                // TODO: review section
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold)
}

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
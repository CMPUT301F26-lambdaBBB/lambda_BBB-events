package com.example.lambda_bbb_events.view.ui.components.campsite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.view.ui.components.rating.RatingSummary


/*
 * Defines a clickable campsite card
 *
 * TODO: Replace the image placeholder with an actual campsite image.
 */

/**
 * Displays a campsite card with its key details.
 *
 * Shows the campsite name, location, rating, reviews,
 * and maximum occupancy.
 *
 * @param campsite The campsite information to display.
 * @param onClick Callback triggered when the card is selected.
 * @param modifier Customizes the card's layout and appearance.
 */
@Composable
fun VerticalCampsiteCard(
    campsite: Campsite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(cardShape)
            .border(1.dp, Color(0xFFE5E5E5), cardShape)
            .background(Color.White)
            .clickable { onClick() }
    ) {
        // image placeholder, replace with the real campsite image later
        Box(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight()
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("image", color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(-2.dp, Alignment.CenterVertically)
        ) {
            // name
            Text(
                text = campsite.campsiteName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = campsite.address,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // rating
            RatingSummary(campsite.rating, campsite.ratingCount)

            Spacer(modifier = Modifier.height(8.dp))

            // max occupancy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Max occupancy",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${campsite.maxOccupancy}",
                    fontSize = 13.sp
                )
            }
        }
    }
}
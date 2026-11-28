package com.example.lambda_bbb_events.view.ui.components.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.model.formatting.parseDateOrNull
import com.example.lambda_bbb_events.model.reservation.Reservation
import com.example.lambda_bbb_events.view.ui.components.common.ProfilePicture
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults


/**
 * A card to display to the owner that has information of an entrant who booked a campsite.
 * It shows the entrant's profile picture, name, the reservation status, the reserved dates,
 * and the number of guests.
 *
 * @param name The name of the entrant.
 * @param profilePictureImageId The ID of the entrant's profile picture. Blank shows a default icon.
 * @param reservation The reservation of the entrant.
 * @param modifier Modifier for layout.
 */
@Composable
fun BookedEntrantCard(
    name: String,
    profilePictureImageId: String,
    reservation: Reservation,
    isRejected: Boolean,
    onReject:() -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    val startDate = parseDateOrNull(reservation.startDate)
    val endDate = parseDateOrNull(reservation.endDate)
    val dates = if (startDate != null && endDate != null) {
        formatDateRangeFromLocalDate(startDate, endDate)
    } else {
        "Dates not set"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
            .testTag("bookedEntrantCard"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfilePicture(
            imageId = profilePictureImageId,
            size = 56.dp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = "Reservation dates",
                    modifier = Modifier.size(16.dp)
                )
                Text(text = dates, fontSize = 13.sp)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "People",
                    modifier = Modifier.size(16.dp)
                )
                Text(text = "${reservation.guests} people", fontSize = 13.sp)
            }
        }



        Button(
            onClick =  onReject,
            enabled = !isRejected,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                disabledContentColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.testTag("cancelReservationButton")
        ) {
            Text(if (isRejected) "Cancelled" else "Cancel")
        }
    }

}
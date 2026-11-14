package com.example.lambda_bbb_events.view.ui.components.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.model.Reservation
import com.example.lambda_bbb_events.model.ReservationStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A card for info on reservation of an entrant on the owner's campsite. It shows the name
 * of the entrant, the status of the reservation, the reservation dates, the number of guests,
 * whether the guest accepted the campsite rules, and a short booking reference.
 *
 * @param guestName The name of the entrant who made the reservation.
 * @param reservation The reservation to show.
 */
@Composable
fun OwnerReservationCard(
    guestName: String,
    reservation: Reservation,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    val guestWord = if (reservation.guests == 1) "guest" else "guests"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // guest name and reservation status
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = guestName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            ReservationStatusPill(reservation.status)
        }

        InfoRow(
            icon = Icons.Outlined.CalendarMonth,
            label = "Reservation Dates",
            value = formatDateRangeFromLocalDate(
                LocalDate.parse(reservation.startDate),
                LocalDate.parse(reservation.endDate)
            )
        )

        InfoRow(
            icon = Icons.Outlined.Person,
            label = "Guests",
            value = "${reservation.guests} $guestWord"
        )

        // rules and policies confirmation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (reservation.ruleConfirmed) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = "Rules accepted",
                    tint = Color(0xFF3B9A35),
                    modifier = Modifier.size(20.dp)
                )
                Text(text = "Campsite rules accepted", fontSize = 14.sp)
            } else {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "Rules not accepted",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Campsite rules not accepted yet",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * An icon with a small label and a value next to it.
 *
 * @param icon The icon to display.
 * @param label The small text above the value.
 * @param value The content text to show.
 */
@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = value, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

/**
 * A label showing the status of a reservation. Confirmed is green, rejected is
 * red, pending is purple, and completed and cancelled are gray.
 *
 * @param status The status of the reservation.
 */
@Composable
private fun ReservationStatusPill(status: ReservationStatus, modifier: Modifier = Modifier) {
    val label: String
    val backgroundColor: Color
    val textColor: Color

    when (status) {
        ReservationStatus.CONFIRMED -> {
            label = "Confirmed"
            backgroundColor = Color(0xFFC8F7C5)
            textColor = Color(0xFF3B9A35)
        }
        ReservationStatus.PENDING -> {
            label = "Pending"
            backgroundColor = Color(0xFFE8E4FF)
            textColor = Color(0xFF4B3FD1)
        }
        ReservationStatus.REJECTED -> {
            label = "Rejected"
            backgroundColor = Color(0xFFFADADD)
            textColor = Color(0xFFB3261E)
        }
        ReservationStatus.CANCELLED -> {
            label = "Cancelled"
            backgroundColor = Color(0xFFEEEEEE)
            textColor = Color(0xFF555555)
        }
        ReservationStatus.COMPLETED -> {
            label = "Completed"
            backgroundColor = Color(0xFFEEEEEE)
            textColor = Color(0xFF555555)
        }
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1
        )
    }
}

/**
 * Formats two dates as a range to be displayed (e.g. "Sep 10, 2026 - Sep 15, 2026").
 *
 * @param start The start date.
 * @param end The end date.
 * @return The range as a string.
 */
fun formatDateRangeFromLocalDate(start: LocalDate, end: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    return "${start.format(formatter)} - ${end.format(formatter)}"
}
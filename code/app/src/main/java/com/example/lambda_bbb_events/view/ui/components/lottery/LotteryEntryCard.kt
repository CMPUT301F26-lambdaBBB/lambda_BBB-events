package com.example.lambda_bbb_events.view.ui.components.lottery

import android.text.format.DateUtils.formatDateRange
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.WaitlistEntry
import com.example.lambda_bbb_events.model.WaitlistStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A card for the lottery tab in the lottery bookings screen. It shows the campsite, the
 * state of the entry (e.g. in waitlist, won, lost, etc.), when the registration ends,
 * the booking period, and the number of guests.
 *
 * @param campsiteName The name of the campsite the lottery is for.
 * @param address The address of the campsite. Not shown if no address.
 * @param entry The entrant's waitlist entry.
 * @param lottery The lottery the entry belongs to.
 * @param onClick Called when the card is clicked.
 */
@Composable
fun LotteryEntryCard(
    campsiteName: String,
    address: String,
    entry: WaitlistEntry,
    lottery: Lottery,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = campsiteName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline()
            )

            EntryStatusPill(entry.status)

            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "View campsite",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (address.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = address,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        PeriodRow(
            label = "Registration ends",
            dates = formatSingleDate(lottery.registrationEnd)
        )

        PeriodRow(
            label = "Booking",
            dates = formatDateRange(lottery.bookingStart, lottery.bookingEnd)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = "Guests",
                modifier = Modifier.size(20.dp)
            )

            val guestWord = if (entry.numberOfGuests == 1) "guest" else "guests"

            Text(
                text = "${entry.numberOfGuests} $guestWord",
                fontSize = 14.sp
            )
        }
    }
}

/**
 * A label showing the status of a waitlist entry. In the waitlist is
 * purple, won and accepted are green, declined is red, and lost is gray.
 *
 * @param status The status of the entry.
 */
@Composable
fun EntryStatusPill(status: WaitlistStatus, modifier: Modifier = Modifier) {
    val label: String
    val backgroundColor: Color
    val textColor: Color

    when (status) {
        WaitlistStatus.IN_WAITLIST -> {
            label = "In waitlist"
            backgroundColor = Color(0xFFE8E4FF)
            textColor = Color(0xFF4B3FD1)
        }
        WaitlistStatus.WON -> {
            label = "Won"
            backgroundColor = Color(0xFFC8F7C5)
            textColor = Color(0xFF3B9A35)
        }
        WaitlistStatus.ACCEPTED -> {
            label = "Accepted"
            backgroundColor = Color(0xFFC8F7C5)
            textColor = Color(0xFF3B9A35)
        }
        WaitlistStatus.DECLINED -> {
            label = "Declined"
            backgroundColor = Color(0xFFFADADD)
            textColor = Color(0xFFB3261E)
        }
        WaitlistStatus.LOST -> {
            label = "Lost"
            backgroundColor = Color(0xFFEEEEEE)
            textColor = Color(0xFF555555)
        }
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 2.dp),
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
 * Formats one date to be displayed (e.g. "Sep 10, 2026").
 *
 * @param date The date to format.
 * @return The date as a string.
 */
fun formatSingleDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    return formatter.format(date)
}
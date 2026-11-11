package com.example.lambda_bbb_events.view.ui.components.lottery

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
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.controller.LotteryPhase
import com.example.lambda_bbb_events.model.Lottery
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A card that shows the details of a lottery including the registration and booking
 * periods, the number of winners, how full the waitlist is, and which phase
 * the lottery is in.
 *
 * @param lotteryNumber The number shown in the title, like 1 for "Lottery 1".
 * @param lottery The lottery to show.
 * @param phase Where the lottery is in its timeline.
 * @param onClick Called when the card is clicked.
 */
@Composable
fun OwnerLotteryCard(
    lotteryNumber: Int,
    lottery: Lottery,
    phase: LotteryPhase,
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
                text = "Lottery $lotteryNumber",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            PhasePill(phase)

            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "View lottery",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        PeriodRow(
            label = "Registration",
            dates = formatDateRange(lottery.registrationStart, lottery.registrationEnd)
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
                contentDescription = "Winners and waitlist",
                modifier = Modifier.size(20.dp)
            )

            val winnerWord = if (lottery.numberOfWinners == 1) "winner" else "winners"

            Text(
                text = "${lottery.numberOfWinners} $winnerWord  |  " +
                        "${lottery.entryIds.size} / ${lottery.waitlistCapacity} waitlisted",
                fontSize = 14.sp
            )
        }
    }
}

/**
 * A calendar icon next to a label and a date range.
 *
 * @param label The text above the dates, like "Registration:".
 * @param dates The date range text.
 */
@Composable
fun PeriodRow(label: String, dates: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Outlined.CalendarMonth,
            contentDescription = label,
            modifier = Modifier.size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy((2).dp)) {
            Text(text = label, fontSize = 12.sp, lineHeight = 14.sp, color = Color.Gray)
            Text(text = dates, fontSize = 14.sp, lineHeight = 18.sp)
        }
    }
}

/**
 * A label showing the phase of a lottery. Open phases are green,
 * cancelled is red, ended is gray, and the waiting phases are pink.
 *
 * @param phase The phase to show.
 */
@Composable
fun PhasePill(phase: LotteryPhase) {
    val backgroundColor: Color
    val textColor: Color

    when (phase) {
        LotteryPhase.REGISTRATION_OPEN, LotteryPhase.BOOKING_OPEN -> {
            backgroundColor = Color(0xFFC8F7C5)
            textColor = Color(0xFF3B9A35)
        }
        LotteryPhase.CANCELLED -> {
            backgroundColor = Color(0xFFFADADD)
            textColor = Color(0xFFB3261E)
        }
        LotteryPhase.ENDED -> {
            backgroundColor = Color(0xFFEEEEEE)
            textColor = Color(0xFF555555)
        }
        else -> {
            backgroundColor = Color(0xFFE8E4FF)
            textColor = Color(0xFF4B3FD1)
        }
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = phase.label,
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
private fun formatDateRange(start: Date, end: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    return "${formatter.format(start)} - ${formatter.format(end)}"
}
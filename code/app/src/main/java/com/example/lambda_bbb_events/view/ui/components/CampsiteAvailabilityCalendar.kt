
package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val availableColor = Color.White
private val bookedColor = Color(0xFFF9D3DF)
private val selectedColor = Color(0xFF89F5A3)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CampsiteAvailabilityCalendar(
    unavailableDates: Set<LocalDate>,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onDateRangeChange: (LocalDate?, LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    initialMonth: YearMonth = YearMonth.now()
) {
    var displayedMonth by remember { mutableStateOf(initialMonth) }
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val firstDay = displayedMonth.atDay(1)
    val firstDayOffset = firstDay.dayOfWeek.value % 7
    val daysInMonth = displayedMonth.lengthOfMonth()
    val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7

    val calendarDays = (0 until totalCells).map { index ->
        firstDay.plusDays((index - firstDayOffset).toLong())
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            displayedMonth = displayedMonth.minusMonths(1)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("‹", fontSize = 30.sp)
                    }

                    Box {
                        TextButton(
                            onClick = { monthExpanded = true }
                        ) {
                            Text(
                                displayedMonth.month.getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.ENGLISH
                                ),
                                color = Color.Black
                            )
                            Text("⌄", color = Color.Black)
                        }

                        DropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = {
                                monthExpanded = false
                            }
                        ) {
                            Month.values().forEach { month ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            month.getDisplayName(
                                                TextStyle.SHORT,
                                                Locale.ENGLISH
                                            )
                                        )
                                    },
                                    onClick = {
                                        displayedMonth = YearMonth.of(
                                            displayedMonth.year,
                                            month
                                        )
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        TextButton(
                            onClick = { yearExpanded = true }
                        ) {
                            Text(
                                displayedMonth.year.toString(),
                                color = Color.Black
                            )
                            Text("⌄", color = Color.Black)
                        }

                        DropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = {
                                yearExpanded = false
                            }
                        ) {
                            val currentYear = displayedMonth.year

                            (currentYear - 2..currentYear + 4).forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year.toString()) },
                                    onClick = {
                                        displayedMonth = YearMonth.of(
                                            year,
                                            displayedMonth.month
                                        )
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            displayedMonth = displayedMonth.plusMonths(1)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("›", fontSize = 30.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        "Su", "Mo", "Tu", "We",
                        "Th", "Fr", "Sa"
                    ).forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                calendarDays.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            val isCurrentMonth =
                                YearMonth.from(date) == displayedMonth

                            val isBooked = date in unavailableDates

                            val isSelected =
                                startDate != null &&
                                        !date.isBefore(startDate) &&
                                        !date.isAfter(endDate ?: startDate)

                            val canSelect =
                                isCurrentMonth &&
                                        !isBooked &&
                                        !date.isBefore(LocalDate.now())

                            val backgroundColor = when {
                                !isCurrentMonth -> Color.Transparent
                                isBooked -> bookedColor
                                isSelected -> selectedColor
                                canSelect -> availableColor
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(backgroundColor)
                                    .clickable(enabled = canSelect) {
                                        if (
                                            startDate == null ||
                                            endDate != null ||
                                            !date.isAfter(startDate)
                                        ) {
                                            onDateRangeChange(date, null)
                                        } else {
                                            val hasConflict =
                                                generateSequence(startDate) {
                                                    it.plusDays(1)
                                                }
                                                    .takeWhile {
                                                        it.isBefore(date)
                                                    }
                                                    .any {
                                                        it in unavailableDates
                                                    }

                                            if (hasConflict) {
                                                onDateRangeChange(date, null)
                                            } else {
                                                onDateRangeChange(
                                                    startDate,
                                                    date
                                                )
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    color = when {
                                        !isCurrentMonth -> Color.LightGray
                                        else -> Color.Black
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalendarLegend("Available", availableColor)
            CalendarLegend("Booked / unavailable", bookedColor)
            CalendarLegend("Selected", selectedColor)
        }
    }
}

@Composable
private fun CalendarLegend(
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )

        Text(
            text = label,
            fontSize = 11.sp
        )
    }
}

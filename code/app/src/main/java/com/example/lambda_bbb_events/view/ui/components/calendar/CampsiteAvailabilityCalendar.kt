
package com.example.lambda_bbb_events.view.ui.components.calendar

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
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.view.ui.theme.bookingStatusColors
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/*
 * Defines an interactive calendar for selecting campsite booking dates.
 *
 * Uses Material 3 theme colors to display available,
 * unavailable, and selected dates in light and dark modes.
 */

/**
 * Displays an interactive calendar for selecting reservation dates.
 *
 * Allows users to select a date range while excluding unavailable dates.
 *
 * @param unavailableDates Dates that cannot be booked.
 * @param startDate The selected start date, or null.
 * @param endDate The selected end date, or null.
 * @param onDateRangeChange Callback for updating the date range.
 * @param modifier Customizes the calendar layout.
 * @param initialMonth The initially displayed month.
 */
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
    var displayedMonth by remember(initialMonth) {
        mutableStateOf(initialMonth)
    }
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val statusColors = MaterialTheme.bookingStatusColors

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
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Month and year navigation
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
                        Text(
                            text = "‹",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Month dropdown
                    Box {
                        TextButton(
                            onClick = { monthExpanded = true }
                        ) {
                            Text(
                                text = displayedMonth.month.getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.ENGLISH
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "⌄",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
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
                                            text = month.getDisplayName(
                                                TextStyle.SHORT,
                                                Locale.ENGLISH
                                            ),
                                            style = MaterialTheme.typography.bodyMedium
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

                    // Year dropdown
                    Box {
                        TextButton(
                            onClick = { yearExpanded = true }
                        ) {
                            Text(
                                text = displayedMonth.year.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "⌄",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
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
                                    text = {
                                        Text(
                                            text = year.toString(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
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
                        Text(
                            text = "›",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Weekday labels
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Calendar date grid
                calendarDays.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
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

                            // Background for each date
                            val backgroundColor = when {
                                !isCurrentMonth -> Color.Transparent
                                isBooked -> statusColors.bookedBackground
                                isSelected -> statusColors.selectedBackground
                                canSelect -> MaterialTheme.colorScheme.surface
                                else -> Color.Transparent
                            }

                            // Text color for each date
                            val textColor = when {
                                !isCurrentMonth ->
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

                                isBooked -> statusColors.booked

                                isSelected -> statusColors.selected

                                !canSelect ->
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                                else -> MaterialTheme.colorScheme.onSurface
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
                                    style = if (isSelected) {
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    } else {
                                        MaterialTheme.typography.bodyMedium
                                    },
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Calendar legend
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalendarLegend(
                label = "Booked / unavailable",
                color = statusColors.bookedBackground
            )

            CalendarLegend(
                label = "Selected",
                color = statusColors.selectedBackground
            )
        }
    }
}

/**
 * Displays a colored indicator with a descriptive label
 * for the calendar legend.
 *
 * @param label Text describing the meaning of the color.
 * @param color Color used for the legend indicator.
 */
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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

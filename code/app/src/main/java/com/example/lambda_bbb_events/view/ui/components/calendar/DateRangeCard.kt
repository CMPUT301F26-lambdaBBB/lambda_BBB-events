package com.example.lambda_bbb_events.view.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/**
 * A card asking users to select a date range.
 *
 * The card doesn't store date data but only changes them in [onStartDateChange]
 * and [onEndDateChange].
 *
 * @param title The title shown at the top of the card.
 * @param startDate The first date as text in yyyy-MM-dd format. Empty if not picked.
 * @param endDate The second date as text in yyyy-MM-dd format. Empty if not picked.
 * @param onStartDateChange Called when the first date changes. Reset calls it with "".
 * @param onEndDateChange Called when the second date changes. Reset calls it with "".
 * @param startLabel The label above the first field. Defaults to "From".
 * @param endLabel The label above the second field. Defaults to "To".
 */
@Composable
fun DateRangeCard(
    title: String,
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    startLabel: String = "From",
    endLabel: String = "To"
) {
    val cardShape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, Color(0xFFE5E5E5), cardShape)
            .background(Color.White)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Reset",
                fontSize = 12.sp,
                color = Color(0xFF5a4ef5),
                modifier = Modifier.clickable {
                    onStartDateChange("")
                    onEndDateChange("")
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = startLabel, fontSize = 13.sp)

                DatePickerField(
                    value = startDate,
                    onValueChange = onStartDateChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = endLabel, fontSize = 13.sp)

                DatePickerField(
                    value = endDate,
                    onValueChange = onEndDateChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Parses date string into a [LocalDate].
 *
 * @param text The date as a string in yyyy-MM-dd format.
 * @return The [LocalDate], or `null` if the text is empty or not a full valid date.
 */
fun parseDateOrNull(text: String): LocalDate? {
    return try {
        LocalDate.parse(text)
    } catch (e: Exception) {
        null
    }
}
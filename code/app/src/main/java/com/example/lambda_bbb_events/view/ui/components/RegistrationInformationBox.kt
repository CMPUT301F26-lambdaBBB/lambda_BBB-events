
package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Displays a campsite registration summary.
 */

/**
 * Displays a summary of the selected campsite registration details.
 *
 * The information box shows the campsite name, reservation
 * start date, end date, and number of people included
 * in the registration.
 *
 * @param campsiteName The name of the selected campsite.
 * @param fromDate The starting date of the reservation.
 * @param endDate The ending date of the reservation.
 * @param numPeople The number of people included
 * in the campsite registration.
 */
@Composable
fun RegistrationInformationBox(
    campsiteName: String,
    fromDate: LocalDate,
    endDate: LocalDate,
    numPeople: Int
) {
    val dateFormatter = DateTimeFormatter.ofPattern(
        "MMMM d, yyyy",
        Locale.ENGLISH
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Text(
            text = "You Selected: $campsiteName",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 8.dp
            )
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = Color.LightGray
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = "From Date",
                    modifier = Modifier.size(28.dp)
                )

                Row {
                    Text(
                        text = "From:",
                        fontSize = 16.sp,
                        modifier = Modifier.width(50.dp)
                    )

                    Text(
                        text = fromDate.format(dateFormatter),
                        fontSize = 14.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = "To Date",
                    modifier = Modifier.size(28.dp)
                )

                Row {
                    Text(
                        text = "To:",
                        fontSize = 16.sp,
                        modifier = Modifier.width(50.dp)
                    )

                    Text(
                        text = endDate.format(dateFormatter),
                        fontSize = 14.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "People",
                    modifier = Modifier.size(28.dp)
                )

                Text(
                    text = "$numPeople people",
                    fontSize = 17.sp
                )
            }
        }
    }
}

package com.example.lambda_bbb_events.view.ui.owner_screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.view.ui.components.calendar.DatePickerField
import com.example.lambda_bbb_events.view.ui.components.calendar.DateRangeCard
import com.example.lambda_bbb_events.view.ui.components.calendar.parseDateOrNull
import com.example.lambda_bbb_events.view.ui.components.campsite.VerticalCampsiteCard
import com.example.lambda_bbb_events.view.ui.components.common.CampsiteThumbnail
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.common.THUMBNAIL_HEIGHT
import com.example.lambda_bbb_events.view.ui.components.common.THUMBNAIL_WIDTH
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.lambda_bbb_events.view.ui.components.navigation.rememberBackAction
import com.example.lambda_bbb_events.view.ui.components.rating.RatingSummary

/**
 * Screen where a campsite owner can create a lottery for their campsite.
 *
 * @param navController The navigation controller.
 * @param controllers The app's controllers.
 */
@Composable
fun CreateMultipleCampsiteLotteryScreen(
    navController: NavController,
    controllers: AppControllers,
) {
    val goBack = rememberBackAction(navController)

    val currentDeviceId = controllers.users.user.deviceId

    val allActiveCampsites = controllers.campsites.campsites.values
        .filter { it.active }
        .sortedBy { it.campsiteName.lowercase() }

    val userCampsites = allActiveCampsites.filter { it.ownerId == currentDeviceId } // campsites where current user made
    val nonUserCampsites = allActiveCampsites.filter { it.ownerId != currentDeviceId } // campsites where current user did not make
    val selectedIds = remember { mutableStateListOf<String>() }

    var showOtherCampsites by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var registrationStart by remember { mutableStateOf("") }
    var registrationEnd by remember { mutableStateOf("") }
    var bookingStart by remember { mutableStateOf("") }
    var bookingEnd by remember { mutableStateOf("") }
    var numberOfWinners by remember { mutableStateOf("1") }
    var waitlistCapacity by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle("Create lottery (multiple)", navController, showBack = true)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Your Campsites",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            for (campsite in userCampsites) {
                val campsiteId = campsite.campsiteId
                val isSelected = campsiteId in selectedIds
                VerticalCampsiteCardWithCheckbox(
                    campsite = campsite,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) selectedIds.remove(campsiteId)
                        else selectedIds.add(campsiteId)
                    }
                )
            }

            if (nonUserCampsites.isNotEmpty()) {
                // make visibility toggleable to show/hide list
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOtherCampsites = !showOtherCampsites },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Other Peoples Campsites (optional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (showOtherCampsites) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (showOtherCampsites) "Hide" else "Show"
                    )
                }

                if (showOtherCampsites) {
                    for (campsite in nonUserCampsites) {
                        val campsiteId = campsite.campsiteId
                        val isSelected = campsiteId in selectedIds
                        VerticalCampsiteCardWithCheckbox(
                            campsite = campsite,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) selectedIds.remove(campsiteId)
                                else selectedIds.add(campsiteId)
                            }
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 10.dp))

            Text(
                text = "Enter Lottery Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            DateRangeCard(
                title = "Registration Period",
                startDate = registrationStart,
                endDate = registrationEnd,
                onStartDateChange = { registrationStart = it },
                onEndDateChange = { registrationEnd = it }
            )

            DateRangeCard(
                title = "Booking Period",
                startDate = bookingStart,
                endDate = bookingEnd,
                onStartDateChange = { bookingStart = it },
                onEndDateChange = { bookingEnd = it }
            )

            FormTextFieldWithTitle(
                title = "Number of winners",
                value = numberOfWinners,
                onValueChange = { numberOfWinners = it },
                placeholder = "How many entrants are drawn"
            )

            FormTextFieldWithTitle(
                title = "Waitlist capacity (optional)",
                value = waitlistCapacity,
                onValueChange = { waitlistCapacity = it },
                placeholder = "Max entrants allowed to join. Leave blank for no limit."
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = goBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        scope.launch {
                            val selectedCampsites = (userCampsites + nonUserCampsites)
                                .filter { it.campsiteId in selectedIds }

                            // selected campsites must include at least one of your own campsites
                            if (selectedCampsites.isNotEmpty() &&
                                selectedCampsites.none { it.ownerId == currentDeviceId }
                            ) {
                                errorMessage = "Include at least one of your own campsites."
                                return@launch
                            }

                            // get the owners for selected campsites
                            val lotteryOwnerIds = selectedCampsites
                                .map { it.ownerId }
                                .distinct()

                            val error = controllers.lotteries.createLottery(
                                ownerIds = lotteryOwnerIds,
                                campsiteIds = selectedIds.toList(),
                                registrationStart = parseDateOrNull(registrationStart),
                                registrationEnd = parseDateOrNull(registrationEnd),
                                bookingStart = parseDateOrNull(bookingStart),
                                bookingEnd = parseDateOrNull(bookingEnd),
                                numberOfWinnersString = numberOfWinners,
                                waitlistCapacityString = waitlistCapacity,
                                pendingOwnerIds = lotteryOwnerIds.filter { it != currentDeviceId }
                            )

                            if (error == null) {
                                goBack()
                            } else {
                                errorMessage = error
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Create lottery")
                }
            }
        }
    }
}

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
fun VerticalCampsiteCardWithCheckbox(
    campsite: Campsite,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(THUMBNAIL_HEIGHT)
            .clip(cardShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = cardShape
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Campsite image
        CampsiteThumbnail(
            campsite = campsite,
            modifier = Modifier
                .width(THUMBNAIL_WIDTH)
                .fillMaxHeight()
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(
                (-2).dp,
                Alignment.CenterVertically
            )
        ) {
            // Campsite name
            Text(
                text = campsite.campsiteName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Campsite location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = campsite.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campsite rating
            RatingSummary(
                campsite.rating,
                campsite.ratingCount
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Maximum occupancy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Max occupancy",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${campsite.maxOccupancy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = null,
            modifier = Modifier.padding(end = 15.dp)
        )
    }
}
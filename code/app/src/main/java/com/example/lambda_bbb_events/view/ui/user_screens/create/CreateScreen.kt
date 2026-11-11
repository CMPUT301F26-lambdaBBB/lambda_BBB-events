package com.example.lambda_bbb_events.view.ui.user_screens.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.map.ClickableMap
import com.example.lambda_bbb_events.view.ui.components.calendar.PopUpShowDatePicker
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import kotlinx.coroutines.launch
import java.time.LocalDate

/*
 * Provides form for creating campsites and lotteries,
 *
 * TODO: Save and validate the selected lottery dates and settings.
 * TODO: selected photos to stay instead of only storing in local
 */

/**
 * Create Screen for creating a new campsite and associated lottery.
 *
 * Requires three sets of information to create:
 * - **Basic Info**: Enter the campsite name, description,
 *   maximum occupancy, tags, and photos.
 * - **Location**: Select a location on an interactive map
 *   and enter the campsite address and postal code.
 * - **Lottery**: Enter lottery dates, registration dates,
 *   number of spots, eligibility, and additional rules.
 *
 * Selecting "Done" creates a campsite and an associated
 * lottery using [AppControllers], then navigates to the
 * home screen.
 *
 * @param navController The navigation controller for navigating between screens.
 * @param controllers AppControllers holding campsites, lotteries, and user data.
 * @param showMap Whether to display the interactive location map.
 * Defaults to true.

 */
@Composable
fun CreateScreen(
    navController: NavController,
    controllers: AppControllers,
    showMap: Boolean = true
) {
    val context = LocalContext.current

    // status variables, 1-> basic info, 2-> location, 3-> lottery
    var currentStep by remember { mutableStateOf(0) }

    // basic info the campsite
    var campsiteName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxOccupancy by remember { mutableStateOf("1") }
    var isOccupancyExpanded by remember { mutableStateOf(false) }
    val tags = remember { mutableStateListOf("Wifi", "Lake View", "Pet Friendly") }
    var newTagText by remember { mutableStateOf("") }
    var isAddingTag by remember { mutableStateOf(false) }
    val selectedImageUris = remember { mutableStateListOf<Uri>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.addAll(uris)
        }
    }
    // variables for the location
    var searchLocation by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(51.1869) }
    var longitude by remember { mutableStateOf(-115.5323) }

    // variables for the lottery and registration
    var lotteryDate by remember { mutableStateOf("") }
    var startRegistration by remember { mutableStateOf("") }
    var endRegistration by remember { mutableStateOf("") }
    var startBooking by remember { mutableStateOf("") }
    var endBooking by remember { mutableStateOf("") }
    var numberOfSpots by remember { mutableStateOf("10") }
    var isSpotsExpanded by remember { mutableStateOf(false) }
    var eligibility by remember { mutableStateOf("All Entrants") }
    var isEligibilityExpanded by remember { mutableStateOf(false) }
    var additionalRules by remember { mutableStateOf("") }

    // dropdown options
    val occupancyOptions = listOf("1", "2", "3", "4", "5", "6", "7", "10", "15", "20", "25", "50+") // may change to be more specific !!!
    val spotsOptions = listOf("5", "10", "15", "20", "25", "50", "100")
    val eligibilityOptions = listOf("All Entrants", "Verified Only", "First-time Entrants")



    /**
     * Creates a campsite and an associated lottery using generated IDs.
     *
     * Assigns the current user as the owner and sets the lottery's
     * waitlist capacity to twice its number of winners.
     */
    fun saveCampsiteAndLottery() {
        val generatedCampsiteId = UUID.randomUUID().toString()
        val ownerDeviceId = controllers.users.user.deviceId

        val newCampsite = Campsite(
            campsiteId = generatedCampsiteId,
            ownerId = ownerDeviceId,
            campsiteName = campsiteName.trim(),
            description = description.trim(),
            address = if (postalCode.isNotBlank()) "${address.trim()}, ${postalCode.trim()}" else address.trim(),
            latitude = latitude,
            longitude = longitude,
            maxOccupancy = maxOccupancy.filter { it.isDigit() }.toIntOrNull() ?: 1,
            rulesPolicies = additionalRules.trim(),
            tags = tags.toList(),
            imageIds = selectedImageUris.map { it.toString() }
        )
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            controllers.campsites.addCampsite(newCampsite)
        }

        val newLottery = Lottery(
            lotteryId = UUID.randomUUID().toString(),
            campsiteIds = listOf(generatedCampsiteId),
            ownerId = ownerDeviceId,
            numberOfWinners = numberOfSpots.filter { it.isDigit() }.toIntOrNull() ?: 10,
            waitlistCapacity = (numberOfSpots.filter { it.isDigit() }.toIntOrNull() ?: 10) * 2
        )

        scope.launch {
            controllers.lotteries.createLottery(
                ownerId = ownerDeviceId,
                campsiteId = generatedCampsiteId,
                registrationStart = LocalDate.parse(startRegistration),
                registrationEnd = LocalDate.parse(endRegistration),
                bookingStart = LocalDate.parse(startBooking),
                bookingEnd = LocalDate.parse(endRegistration),
                numberOfWinnersString = numberOfSpots,
                waitlistCapacityString = numberOfSpots,
            )
        }
        navController.navigate("home")
    }

    Scaffold(
        modifier = Modifier.testTag("screen_create"),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(
                navController
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitle(
                title = "Create Campsite",
                navController = navController
            )

            StepStageIndicator(currentStep = currentStep, onStepClick = { currentStep = it })

            // Keep the form inside the available space so buttons can scroll
            // above the bottom navigation bar on smaller devices.
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = currentStep,
                label = "create_screen_animation",
                transitionSpec = {
                    if (targetState > initialState) {

                        slideInHorizontally(
                            initialOffsetX = { it }
                        ) + fadeIn() togetherWith

                                slideOutHorizontally(
                                    targetOffsetX = { -it }
                                ) + fadeOut()

                    } else {

                        slideInHorizontally(
                            initialOffsetX = { -it }
                        ) + fadeIn() togetherWith

                                slideOutHorizontally(
                                    targetOffsetX = { it }
                                ) + fadeOut()
                    }
                }
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when (step) {

                        // Basic Info Screen
                        0 -> {
                            FormLabel("Campsite Name *")
                            FormInputField(
                                value = campsiteName,
                                onValueChange = { campsiteName = it },
                                placeholder = "Enter campsite name",
                                modifier = Modifier.testTag("create_name")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FormLabel("Description *")
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = {
                                    Text(
                                        "Enter campsite description",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min=120.dp)
                                    .animateContentSize()
                                    .testTag("create_description"),
                                minLines = 5,
                                maxLines = 10,
                                shape = RoundedCornerShape(16.dp),
                                singleLine = false,
                                colors = createTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FormLabel("Max Occupancy *")
                            Box {
                                OutlinedTextField(
                                    value = maxOccupancy,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select Occupancy",
                                            modifier = Modifier.clickable { isOccupancyExpanded = true }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .clickable { isOccupancyExpanded = true },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = createTextFieldColors()
                                )
                                DropdownMenu(
                                    expanded = isOccupancyExpanded,
                                    onDismissRequest = { isOccupancyExpanded = false }
                                ) {
                                    occupancyOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                maxOccupancy = option
                                                isOccupancyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            FormLabel("Tags")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                tags.forEach { tag ->
                                    TagChip(tagText = tag, onDelete = { tags.remove(tag) })
                                }
                                Surface(
                                    onClick = { isAddingTag = !isAddingTag },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Add Tags",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }

                            if (isAddingTag) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FormInputField(
                                        value = newTagText,
                                        onValueChange = { newTagText = it },
                                        placeholder = "New tag name",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            if (newTagText.isNotBlank()) {
                                                tags.add(newTagText.trim())
                                                newTagText = ""
                                                isAddingTag = false
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Add")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            FormLabel("Add Photo")
                            if (selectedImageUris.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(selectedImageUris) { uri ->
                                        Box {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.size(80.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        "Photo",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Photo",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .align(Alignment.TopEnd)
                                                    .clickable { selectedImageUris.remove(uri) }
                                            )
                                        }
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (selectedImageUris.isEmpty()) "+ Add Photo" else "+ Add More Photos (${selectedImageUris.size} selected)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = "Add Photo",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { currentStep = 1 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Add Locations →", style = MaterialTheme.typography.labelLarge)
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                        }

                        // Location Screen
                        1 -> {
                            FormInputField(
                                value = searchLocation,
                                onValueChange = { searchLocation = it },
                                placeholder = "Search location...",
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
                                }
                            )

                            if (showMap) {
                                Spacer(modifier = Modifier.height(4.dp))
                                ClickableMap(
                                    height = 220,
                                    onLocationSelected = { coordinates, fetchedAddress, fetchedPostalCode ->
                                        latitude = coordinates.latitude
                                        longitude = coordinates.longitude
                                        if (fetchedAddress.isNotBlank()) {
                                            address = fetchedAddress
                                            searchLocation = fetchedAddress
                                        }
                                        if (fetchedPostalCode.isNotBlank()) {
                                            postalCode = fetchedPostalCode
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            FormLabel("Address")
                            FormInputField(
                                value = address,
                                onValueChange = { address = it },
                                placeholder = "Enter street address",
                                modifier = Modifier.testTag("create_address")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FormLabel("Postal Code")
                            FormInputField(
                                value = postalCode,
                                onValueChange = { postalCode = it },
                                placeholder = "Enter postal code",
                                modifier = Modifier.testTag("create_postal_code")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { currentStep = 0 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text("← Back", style = MaterialTheme.typography.labelLarge)
                                }
                                Button(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(
                                        "Set Lottery →",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        2 -> {
                            FormLabel("Start Registration *")
                            DateField(
                                value = startRegistration,
                                onClick = { PopUpShowDatePicker(context) { startRegistration = it } }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FormLabel("End Registration *")
                            DateField(
                                value = endRegistration,
                                onClick = { PopUpShowDatePicker(context) { endRegistration = it } }
                            )

                            FormLabel("Start Booking *")
                            DateField(
                                value = startRegistration,
                                onClick = { PopUpShowDatePicker(context) { startBooking = it } }
                            )

                            FormLabel("End Booking *")
                            DateField(
                                value = endRegistration,
                                onClick = { PopUpShowDatePicker(context) { endBooking = it } }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel("Number of Spots *")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box {
                                        OutlinedTextField(
                                            value = numberOfSpots,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Select Spots",
                                                    modifier = Modifier.clickable {
                                                        isSpotsExpanded = true
                                                    }
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isSpotsExpanded = true },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = createTextFieldColors()
                                        )
                                        DropdownMenu(
                                            expanded = isSpotsExpanded,
                                            onDismissRequest = { isSpotsExpanded = false }
                                        ) {
                                            spotsOptions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option) },
                                                    onClick = {
                                                        numberOfSpots = option
                                                        isSpotsExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel("Eligibility *")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box {
                                        OutlinedTextField(
                                            value = eligibility,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Select Eligibility",
                                                    modifier = Modifier.clickable {
                                                        isEligibilityExpanded = true
                                                    }
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isEligibilityExpanded = true },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = createTextFieldColors()
                                        )
                                        DropdownMenu(
                                            expanded = isEligibilityExpanded,
                                            onDismissRequest = { isEligibilityExpanded = false }
                                        ) {
                                            eligibilityOptions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option) },
                                                    onClick = {
                                                        eligibility = option
                                                        isEligibilityExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            FormLabel("Additional Rules")
                            OutlinedTextField(
                                value = additionalRules,
                                onValueChange = { additionalRules = it },
                                placeholder = { Text("Enter additional rules", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min=120.dp)
                                    .animateContentSize(),
                                minLines = 2,
                                maxLines = 5,
                                shape = RoundedCornerShape(16.dp),
                                singleLine = false,
                                colors = createTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text("← Back", style = MaterialTheme.typography.labelLarge)
                                }
                                Button(
                                    onClick = {
                                        saveCampsiteAndLottery()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("Done", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays a navigation indicator for the campsite
 * creation process.
 *
 * Users can select a stage to navigate directly to it.
 *
 * @param currentStep Index of the currently active stage.
 * @param onStepClick Callback invoked with the selected
 * stage index when a stage is clicked.
 */
@Composable
private fun StepStageIndicator(currentStep: Int, onStepClick: (Int) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepPill("Basic Info", isActive = currentStep == 0, onClick = { onStepClick(0) })
            Text("--->", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            StepPill("Location", isActive = currentStep == 1, onClick = { onStepClick(1) })
            Text("--->", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            StepPill("Lottery", isActive = currentStep == 2, onClick = { onStepClick(2) })
        }
    }
}


/**
 * Displays a selectable stage indicator.
 *
 * @param text Label displayed inside the stage indicator.
 * @param isActive Whether the stage is currently selected.
 * @param onClick Callback invoked when the stage is clicked.
 */
@Composable
private fun StepPill(text: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            style = if (isActive) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
            color = if (isActive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Displays a label for an input field.
 *
 * @param text The label displayed above the input field.
 */
@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}


/**
 * Displays a customizable single-line form input field.
 *
 * @param value The current text entered in the field.
 * @param onValueChange Callback invoked when the text changes.
 * @param placeholder Text displayed when the field is empty.
 * @param leadingIcon Optional composable displayed at the
 * beginning of the input field. Defaults to null.
 * @param modifier Modifier used to customize the input
 * field's layout and appearance.
 */
@Composable
private fun FormInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = leadingIcon,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = createTextFieldColors()
    )
}


/**
 * Displays a read-only date selection field.
 *
 * Clicking the field or calendar icon invokes the
 * provided callback to open a date picker.
 *
 * @param value The currently selected date as a string.
 * @param onClick Callback invoked when the date field
 * or calendar icon is clicked.
 */
@Composable
private fun DateField(value: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        placeholder = { Text("Select Date", color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Select Date",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClick() },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = createTextFieldColors()
    )
}


/**
 * Displays a removable campsite tag.
 *
 * Clicking the close icon invokes the deletion callback.
 *
 * @param tagText The text displayed inside the tag.
 * @param onDelete Callback invoked when the close icon
 * is clicked.
 */

@Composable
private fun TagChip(tagText: String, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = tagText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Tag",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Applies the app's Material 3 light/dark colors to form inputs.
 */
@Composable
private fun createTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary
    )
}

package com.example.lambda_bbb_events.view.ui

import android.app.DatePickerDialog
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.Lottery
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.model.repository.LotteryRepository
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import java.util.Calendar
import java.util.UUID

/**
 * Create Screen for creating a new campsite and associated lottery.
 *
 * @param navController The navigation controller for navigating between screens.
 * @param campsiteRepository The repository for saving campsites.
 * @param lotteryRepository The repository for saving lotteries.
 * @param userController Optional user controller to obtain campsite owner details.
 */
@Composable
fun CreateScreen(
    navController: NavController,
    campsiteRepository: CampsiteRepository = remember { CampsiteRepository() },
    lotteryRepository: LotteryRepository = remember { LotteryRepository() },
    userController: UserController? = null
) {
    var currentStep by remember { mutableStateOf(0) }

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

    var searchLocation by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

    var lotteryDate by remember { mutableStateOf("") }
    var startRegistration by remember { mutableStateOf("") }
    var endRegistration by remember { mutableStateOf("") }
    var numberOfSpots by remember { mutableStateOf("10") }
    var isSpotsExpanded by remember { mutableStateOf(false) }
    var eligibility by remember { mutableStateOf("All Entrants") }
    var isEligibilityExpanded by remember { mutableStateOf(false) }
    var additionalRules by remember { mutableStateOf("") }

    val occupancyOptions = listOf("1", "2", "5", "10", "15", "20", "25", "50+")
    val spotsOptions = listOf("5", "10", "15", "20", "25", "50", "100")
    val eligibilityOptions = listOf("All Entrants", "Verified Only", "First-time Entrants")

    val context = LocalContext.current

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected("$year-${month + 1}-$dayOfMonth")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun saveCampsiteAndLottery() {
        val generatedCampsiteId = UUID.randomUUID().toString()
        val ownerDeviceId = userController?.user?.deviceId ?: ""

        val newCampsite = Campsite(
            campsiteId = generatedCampsiteId,
            ownerId = ownerDeviceId,
            campsiteName = campsiteName.trim(),
            description = description.trim(),
            address = if (postalCode.isNotBlank()) "${address.trim()}, ${postalCode.trim()}" else address.trim(),
            maxOccupancy = maxOccupancy.filter { it.isDigit() }.toIntOrNull() ?: 1,
            rulesPolicies = additionalRules.trim(),
            tags = tags.toList(),
            imageIds = selectedImageUris.map { it.toString() }
        )

        campsiteRepository.addCampsite(newCampsite)

        val newLottery = Lottery(
            lotteryId = UUID.randomUUID().toString(),
            campsiteIds = listOf(generatedCampsiteId),
            ownerId = ownerDeviceId,
            numberOfWinners = numberOfSpots.filter { it.isDigit() }.toIntOrNull() ?: 10,
            waitlistCapacity = (numberOfSpots.filter { it.isDigit() }.toIntOrNull() ?: 10) * 2
        )

        lotteryRepository.addLottery(newLottery)
        navController.navigate("home")
    }

    Scaffold(
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

            Spacer(modifier = Modifier.height(4.dp))

            AnimatedContent(
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
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                ) {
                    when (step) {
                        0 -> {
                            FormLabel("Campsite Name *")
                            FormInputField(
                                value = campsiteName,
                                onValueChange = { campsiteName = it },
                                placeholder = "Enter campsite name"
                            )

                            FormLabel("Description *")
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = {
                                    Text(
                                        "Enter campsite description",
                                        color = Color.Gray
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min=120.dp)
                                    .animateContentSize(),
                                minLines = 5,
                                maxLines = 10,
                                shape = RoundedCornerShape(16.dp),
                                singleLine = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Gray
                                )
                            )

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
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFFAFAFA),
                                        unfocusedContainerColor = Color(0xFFFAFAFA),
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.Gray
                                    )
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
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Add Tags",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
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
                                                color = Color.LightGray,
                                                modifier = Modifier.size(80.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        "Photo",
                                                        fontSize = 12.sp,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Photo",
                                                tint = Color.Red,
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
                                color = Color(0xFFFBF4FF),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(0xFFE1BEE7)
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
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = "Add Photo",
                                        modifier = Modifier.size(32.dp),
                                        tint = Color.DarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { currentStep = 1 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF3E5F5),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Add Locations →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                        }

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

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(Color(0xFFC8E6C9), shape = RoundedCornerShape(16.dp))
                                    .border(1.dp, Color.LightGray, shape = RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Map Location Preview",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            FormLabel("Address")
                            FormInputField(
                                value = address,
                                onValueChange = { address = it },
                                placeholder = "Enter street address"
                            )

                            FormLabel("Postal Code")
                            FormInputField(
                                value = postalCode,
                                onValueChange = { postalCode = it },
                                placeholder = "Enter postal code"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

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
                                        containerColor = Color(0xFFEEEEEE),
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("← Back", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF3E5F5),
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        "Set Lottery →",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        2 -> {
                            FormLabel("Lottery Date *")
                            DateField(
                                value = lotteryDate,
                                onClick = { showDatePicker { lotteryDate = it } }
                            )

                            FormLabel("Start Registration *")
                            DateField(
                                value = startRegistration,
                                onClick = { showDatePicker { startRegistration = it } }
                            )

                            FormLabel("End Registration *")
                            DateField(
                                value = endRegistration,
                                onClick = { showDatePicker { endRegistration = it } }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel("Number of Spots *")
                                    Spacer(modifier = Modifier.height(8.dp))
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
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFFFAFAFA),
                                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color.Gray
                                            )
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
                                    Spacer(modifier = Modifier.height(8.dp))
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
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFFFAFAFA),
                                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color.Gray
                                            )
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

                            FormLabel("Additional Rules")
                            OutlinedTextField(
                                value = additionalRules,
                                onValueChange = { additionalRules = it },
                                placeholder = { Text("Enter additional rules", color = Color.Gray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min=120.dp)
                                    .animateContentSize(),
                                minLines = 2,
                                maxLines = 5,
                                shape = RoundedCornerShape(16.dp),
                                singleLine = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.Gray
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

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
                                        containerColor = Color(0xFFEEEEEE),
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("← Back", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                                        containerColor = Color(0xFFF3E5F5),
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Done", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepStageIndicator(currentStep: Int, onStepClick: (Int) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFAFAFA),
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
            Text("--->", color = Color.Gray, fontSize = 12.sp)
            StepPill("Location", isActive = currentStep == 1, onClick = { onStepClick(1) })
            Text("--->", color = Color.Gray, fontSize = 12.sp)
            StepPill("Lottery", isActive = currentStep == 2, onClick = { onStepClick(2) })
        }
    }
}

@Composable
private fun StepPill(text: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) Color(0xFFE8DEF8) else Color.Transparent,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.Black else Color.Gray,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

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
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = leadingIcon,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFAFAFA),
            unfocusedContainerColor = Color(0xFFFAFAFA),
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = Color.Gray
        )
    )
}

@Composable
private fun DateField(value: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        placeholder = { Text("Select Date", color = Color.Gray) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Select Date",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClick() },
                tint = Color.DarkGray
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFAFAFA),
            unfocusedContainerColor = Color(0xFFFAFAFA),
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = Color.Gray
        )
    )
}

@Composable
private fun TagChip(tagText: String, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFAFAFA),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = tagText, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Tag",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() },
                tint = Color.Gray
            )
        }
    }
}

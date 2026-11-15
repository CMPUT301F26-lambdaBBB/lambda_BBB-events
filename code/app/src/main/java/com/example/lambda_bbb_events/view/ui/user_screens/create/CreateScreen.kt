package com.example.lambda_bbb_events.view.ui.user_screens.create

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.Campsite
import com.example.lambda_bbb_events.model.ImageMetaData
import com.example.lambda_bbb_events.model.ImageProcessor
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.launch
import com.example.lambda_bbb_events.view.ui.components.navigation.StepStageIndicator
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Coordinates the three-step campsite creation form and saves the campsite
 * and associated lottery when the owner presses Done.
 *
 * @param navController The controller for app navigation.
 * @param controllers The campsite, lottery, and user controllers.
 * @param showMap Whether the location step shows a clickable map.
 */
@Composable
fun CreateScreen(
    navController: NavController,
    controllers: AppControllers,
    showMap: Boolean = true
) {
    val form = remember { CreateCampsiteFormState() }
    var currentStep by remember { mutableStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val imageProcessor = ImageProcessor()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.testTag("screen_create"),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitle(title = "Create Campsite", navController = navController)

            StepStageIndicator(
                currentStep = currentStep,
                onStepClick = { currentStep = it }
            )

            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = currentStep,
                label = "create_screen_animation",
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                    } else {
                        slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                                slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    }
                }
            ) { step ->
                when (step) {
                    0 -> CreateBasicInfoStep(
                        form = form,
                        onNext = { currentStep = 1 }
                    )

                    1 -> CreateLocationStep(
                        form = form,
                        showMap = showMap,
                        onBack = { currentStep = 0 },
                        onNext = { currentStep = 2 }
                    )

                    2 -> CreateLotteryStep(
                        form = form,
                        onBack = { currentStep = 1 },
                        isSaving = isSaving,
                        errorMessage = saveError,
                        onDone = {
                            val registrationStart = runCatching {
                                LocalDate.parse(form.startRegistration)
                            }.getOrNull()
                            val registrationEnd = runCatching {
                                LocalDate.parse(form.endRegistration)
                            }.getOrNull()
                            val bookingStart = runCatching {
                                LocalDate.parse(form.startBooking)
                            }.getOrNull()
                            val bookingEnd = runCatching {
                                LocalDate.parse(form.endBooking)
                            }.getOrNull()

                            when {
                                isSaving -> Unit
                                registrationStart == null || registrationEnd == null ||
                                        bookingStart == null || bookingEnd == null -> {
                                    saveError = "Select valid registration and booking dates."
                                }

                                registrationEnd.isBefore(registrationStart) ||
                                        bookingEnd.isBefore(bookingStart) -> {
                                    saveError = "End dates cannot be before start dates."
                                }

                                else -> {
                                    isSaving = true
                                    saveError = null
                                    try {
                                        scope.launch {
                                            val campsiteId = UUID.randomUUID().toString()
                                            val ownerId = controllers.users.user.deviceId
                                            val winnerCount = form.numberOfSpots.toIntOrNull() ?: 10
                                            val imageMetaData = mutableListOf<ImageMetaData>()

                                            for (uri in form.selectedImageUris) {
                                                val bitmap =
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                        ImageDecoder.decodeBitmap(
                                                            ImageDecoder.createSource(
                                                                context.contentResolver,
                                                                uri
                                                            )
                                                        )
                                                    } else {
                                                        MediaStore.Images.Media.getBitmap(
                                                            context.contentResolver,
                                                            uri
                                                        )
                                                    }

                                                imageMetaData += imageProcessor.uploadImage(
                                                    imageId = UUID.randomUUID().toString(),
                                                    uploaderUserId = ownerId,
                                                    bitmap = bitmap
                                                )
                                            }

                                            // Save campsite first so the lottery can reference it.
                                            controllers.campsites.addCampsite(
                                                campsiteId = campsiteId,
                                                ownerId = ownerId,
                                                campsiteName = form.campsiteName.trim(),
                                                description = form.description.trim(),
                                                address = if (form.postalCode.isNotBlank()) {
                                                    "${form.address.trim()}, ${form.postalCode.trim()}"
                                                } else {
                                                    form.address.trim()
                                                },
                                                latitude = form.latitude,
                                                longitude = form.longitude,
                                                maxOccupancy = form.maxOccupancy.filter { it.isDigit() }
                                                    .toIntOrNull() ?: 1,
                                                rulesPolicies = form.additionalRules.trim(),
                                                tags = form.tags.toList(),
                                                imageIds = imageMetaData
                                            )
                                            controllers.lotteries.createLottery(
                                                ownerId = ownerId,
                                                campsiteId = campsiteId,
                                                registrationStart = registrationStart,
                                                registrationEnd = registrationEnd,
                                                bookingStart = bookingStart,
                                                bookingEnd = bookingEnd,
                                                numberOfWinnersString = winnerCount.toString(),
                                                waitlistCapacityString = winnerCount.toString()
                                            )
                                            withContext(Dispatchers.Main) {
                                                navController.navigate("home") {
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        saveError = e.message ?: "Failed to create campsite."
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

package com.example.lambda_bbb_events.view.ui.user_screens.create

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
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.model.image.ImageMetaData
import com.example.lambda_bbb_events.view.ui.components.navigation.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.launch
import com.example.lambda_bbb_events.view.ui.components.navigation.StepStageIndicator
import androidx.compose.ui.platform.LocalContext
import com.example.lambda_bbb_events.model.formatting.parseDateOrNull
import com.example.lambda_bbb_events.model.services.decodePhoto
import androidx.compose.ui.geometry.Offset


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
    val imageProcessor = controllers.images
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.testTag("screen_create"),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                        placeSearch = controllers.places,
                        onBack = { currentStep = 0 },
                        onNext = { currentStep = 2 }
                    )

                    2 -> CreateLotteryStep(
                        form = form,
                        onBack = { currentStep = 1 },
                        isSaving = isSaving,
                        errorMessage = saveError,
                        onDone = {
                            val registrationStart = parseDateOrNull(form.startRegistration)
                            val registrationEnd = parseDateOrNull(form.endRegistration)
                            val bookingStart = parseDateOrNull(form.startBooking)
                            val bookingEnd = parseDateOrNull(form.endBooking)
                            val spots = form.numberOfSpots.toIntOrNull()

                            when {
                                isSaving -> Unit
                                spots == null || spots < 1 -> {
                                    saveError = "Enter the number of spots (at least 1)."
                                }

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
                                    scope.launch {
                                        try {
                                            val campsiteId = UUID.randomUUID().toString()
                                            val ownerId = controllers.users.user.deviceId
                                            val winnerCount = spots
                                            val imageMetaData = mutableListOf<ImageMetaData>()

                                            for (uri in form.selectedImageUris) {
                                                val bitmap = decodePhoto(context, uri)
                                                val focus = form.photoFocus[uri] ?: Offset(0.5f, 0.5f)

                                                imageMetaData += imageProcessor.uploadImage(
                                                    imageId = UUID.randomUUID().toString(),
                                                    uploaderUserId = ownerId,
                                                    bitmap = bitmap,
                                                    focusX = focus.x,
                                                    focusY = focus.y
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
                                                maxOccupancy = form.maxOccupancy.toIntOrNull() ?: 1,
                                                rulesPolicies = form.additionalRules.trim(),
                                                tags = form.tags.toList(),
                                                imageIds = imageMetaData
                                            )
                                            controllers.lotteries.createLottery(
                                                ownerIds = listOf(ownerId),
                                                campsiteIds = listOf(campsiteId),
                                                registrationStart = registrationStart,
                                                registrationEnd = registrationEnd,
                                                bookingStart = bookingStart,
                                                bookingEnd = bookingEnd,
                                                numberOfWinnersString = winnerCount.toString(),
                                                waitlistCapacityString = winnerCount.toString()
                                            )
                                            navController.navigate("home") {
                                                launchSingleTop = true
                                            }
                                        } catch (e: Exception) {
                                            saveError = e.message ?: "Failed to create campsite."
                                        } finally {
                                            isSaving = false
                                        }
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

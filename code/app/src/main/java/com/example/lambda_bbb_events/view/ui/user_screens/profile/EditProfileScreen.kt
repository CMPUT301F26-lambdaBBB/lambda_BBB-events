
package com.example.lambda_bbb_events.view.ui.user_screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.formatting.formatPhoneNumber
import com.example.lambda_bbb_events.model.formatting.parsePhoneNumber
import com.example.lambda_bbb_events.model.services.decodePhoto
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.PhoneNumberFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.ProfilePicture
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import kotlinx.coroutines.launch
import java.util.UUID

/*
 * Provides a screen for editing the current user's profile.
 */

/**
 * Allows users to edit their profile information.
 *
 * Displays editable name, email, and phone fields.
 * Saves changes and displays an error if the update fails.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves and updates user information.
 */
@Composable
fun EditProfileScreen(
    navController: NavController,
    controllers: AppControllers
) {
    val user: User = controllers.users.user

    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    val savedPhone = remember(user.phone) { parsePhoneNumber(user.phone) }
    var phoneCountry by remember { mutableStateOf(savedPhone.country) }
    var phone by remember { mutableStateOf(savedPhone.localNumber) }
    var showCreationError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // The profile picture is saved as soon as it changes, separately from the other details
    var pictureId by remember { mutableStateOf(user.profilePictureImageId) }
    var isSavingPicture by remember { mutableStateOf(false) }
    var pictureError by remember { mutableStateOf<String?>(null) }

    /** Saves [newPictureId] as the profile picture, then deletes the picture it replaced. */
    suspend fun savePicture(newPictureId: String) {
        val oldPictureId = controllers.users.updateProfilePicture(newPictureId)
        pictureId = newPictureId
        if (!oldPictureId.isNullOrBlank()) {
            controllers.images.deleteImage(oldPictureId)
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isSavingPicture = true
            pictureError = null
            scope.launch {
                try {
                    val photo = decodePhoto(context, uri, maxSize = 1080)
                    val newPictureId = UUID.randomUUID().toString()
                    controllers.images.uploadImage(
                        imageId = newPictureId,
                        uploaderUserId = user.deviceId,
                        bitmap = photo
                    )
                    savePicture(newPictureId)
                } catch (e: Exception) {
                    pictureError = "Could not upload the photo. Please try again."
                } finally {
                    isSavingPicture = false
                }
            }
        }
    }

    fun pickPhoto() {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle(
            title = "Edit Profile",
            navController = navController,
            showNotifications = false
        )

        Spacer(Modifier.height(16.dp))

        // Profile picture, with a camera badge showing it can be changed
        Box {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isSavingPicture) { pickPhoto() }
                    .testTag("edit_profile_picture")
            ) {
                ProfilePicture(
                    imageId = pictureId,
                    size = 165.dp
                )
                if (isSavingPicture) {
                    CircularProgressIndicator(modifier = Modifier.testTag("profile_picture_saving"))
                }
            }
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(10.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = { pickPhoto() },
                enabled = !isSavingPicture,
                modifier = Modifier.testTag("change_profile_picture")
            ) {
                Text(if (pictureId.isBlank()) "Add photo" else "Change photo")
            }
            if (pictureId.isNotBlank()) {
                TextButton(
                    onClick = {
                        isSavingPicture = true
                        pictureError = null
                        scope.launch {
                            try {
                                savePicture("")
                            } catch (e: Exception) {
                                pictureError = "Could not remove the photo. Please try again."
                            } finally {
                                isSavingPicture = false
                            }
                        }
                    },
                    enabled = !isSavingPicture,
                    modifier = Modifier.testTag("remove_profile_picture")
                ) {
                    Text("Remove photo")
                }
            }
        }

        val error = pictureError
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))

        FormTextFieldWithTitle(
            title = "Name",
            value = name,
            onValueChange = { name = it },
            placeholder = "Yuta",
            modifier = Modifier.testTag("edit_profile_name")
        )

        FormTextFieldWithTitle(
            title = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "yuta@hotmail.com",
            modifier = Modifier.testTag("edit_profile_email")
        )

        PhoneNumberFieldWithTitle(
            title = "Phone Number",
            country = phoneCountry,
            onCountryChange = { phoneCountry = it },
            number = phone,
            onNumberChange = { phone = it },
            modifier = Modifier.testTag("edit_profile_phone")
        )

        Spacer(Modifier.height(64.dp))

        Button(
            onClick = {
                scope.launch {
                    val successful = controllers.users.updateEntrant(
                        name.trim(),
                        email.trim(),
                        formatPhoneNumber(phoneCountry, phone)
                    )

                    if (successful) {
                        navController.navigate("profile")
                    } else {
                        showCreationError = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Save Changes",
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (showCreationError) {
            Text(
                text = "Unable to update profile. Please review your information.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

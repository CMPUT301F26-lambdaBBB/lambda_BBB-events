package com.example.lambda_bbb_events.view.ui.admin_screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.user.User
import com.example.lambda_bbb_events.model.user.UserRole
import com.example.lambda_bbb_events.model.formatting.formatLocalNumber
import com.example.lambda_bbb_events.model.formatting.parsePhoneNumber
import com.example.lambda_bbb_events.view.ui.components.admin.NoAccessMessage
import com.example.lambda_bbb_events.view.ui.components.common.ProfilePicture
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.navigation.rememberBackAction
import com.example.lambda_bbb_events.view.ui.components.profile.ShowAlertDialog
import kotlinx.coroutines.launch

/**
 * Displays a user's profile to an admin, who can promote the user to admin or remove them.
 * Both actions ask for confirmation first, and show an error if they fail.
 *
 * Promoting is only offered for users who are not admins yet. Afterwards the screen stays
 * open and shows the new role.
 *
 * Removing also deletes the user's active lotteries and archives their campsites, then goes
 * back to the previous screen.
 *
 * Users who are not admins see a message that they do not have access instead.
 *
 * @param navController Handles screen navigation.
 * @param controllers All the app controllers, for reading and changing the user.
 * @param userId The device ID of the user to show.
 */
@Composable
fun AdminProfileDetailsScreen(
    navController: NavController,
    controllers: AppControllers,
    userId: String
) {
    val isAdmin = controllers.users.currentUser?.role == UserRole.ADMIN

    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var removeError by remember { mutableStateOf(false) }
    var showMakeAdminDialog by remember { mutableStateOf(false) }
    var makeAdminError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val goBack = rememberBackAction(navController)

    LaunchedEffect(userId, isAdmin) {
        isLoading = true
        if (isAdmin) {
            user = try {
                controllers.users.getUser(userId)
            } catch (_: Exception) { // a failed read is shown as the profile not being found
                null
            }
        }
        isLoading = false
    }

    Scaffold(
        modifier = Modifier.testTag("screen_admin_profile_details"),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
        // TODO: add the admin bottom navigation bar here
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ScreenTitle(
                title = "Profile",
                navController = navController,
                showNotifications = false,
                showBack = true
            )

            val shownUser = user
            when {
                !isAdmin -> {
                    NoAccessMessage(navController)
                }

                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                shownUser == null -> {
                    Text(
                        text = "Profile not found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfilePicture(
                            imageId = shownUser.profilePictureImageId,
                            size = 165.dp
                        )

                        Spacer(Modifier.height(15.dp))

                        Text(
                            text = shownUser.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Text(
                            text = shownUser.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        RoleChip(role = shownUser.role)
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    ProfileDetailRow(
                        icon = Icons.Outlined.Person,
                        label = "Name",
                        value = shownUser.name
                    )
                    ProfileDetailRow(
                        icon = Icons.Outlined.Email,
                        label = "Email",
                        value = shownUser.email
                    )
                    ProfileDetailRow(
                        icon = Icons.Outlined.Phone,
                        label = "Phone",
                        value = formatPhone(shownUser.phone)
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    if (shownUser.role != UserRole.ADMIN) {
                        Button(
                            onClick = {
                                makeAdminError = false
                                showMakeAdminDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("adminMakeAdminButton"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Make Admin",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (makeAdminError) {
                            Text(
                                text = "Could not make this user an admin. Please try again.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = {
                            removeError = false
                            showDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("adminRemoveProfileButton"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "Remove Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (removeError) {
                        Text(
                            text = "Could not remove this profile. Please try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // prompts alert dialog when make admin is clicked
                    if (showMakeAdminDialog) {
                        ShowAlertDialog(
                            dialogLabel = "Make Admin?",
                            dialogCaption = "${shownUser.name} will be able to manage all " +
                                    "campsites and profiles, including removing them.",
                            confirmButton = {
                                showMakeAdminDialog = false
                                scope.launch {
                                    try {
                                        val promoted = controllers.users.makeAdmin(userId)
                                        if (promoted != null) {
                                            user = promoted
                                        } else {
                                            makeAdminError = true
                                        }
                                    } catch (_: Exception) { // a failed write keeps the old role
                                        makeAdminError = true
                                    }
                                }
                            },
                            confirmText = "Confirm",
                            dismissState = { showMakeAdminDialog = false },
                            cancelText = "Cancel",
                            modifier = Modifier
                        )
                    }

                    // prompts alert dialog when remove is clicked
                    if (showDialog) {
                        ShowAlertDialog(
                            dialogLabel = "Remove Profile?",
                            dialogCaption = "${shownUser.name}'s profile will be removed.\nThis action cannot be undone.",
                            confirmButton = {
                                showDialog = false
                                scope.launch { // starts coroutine so firebase deletion runs without freezing app
                                    try {
                                        // Delete the user's lotteries and archive their campsites first, so a
                                        // failure leaves the profile in place to retry instead of leaving them
                                        // orphaned. Campsites are kept, inactive, for past bookings and records.
                                        controllers.lotteries.cancelLotteriesOfDeletedOwner(userId) { campsiteId ->
                                            controllers.campsites.getCampsite(campsiteId)?.campsiteName
                                        }
                                        controllers.campsites.archiveCampsitesByOwner(userId)
                                        if (controllers.users.removeUser(userId)) {
                                            goBack()
                                        } else {
                                            removeError = true
                                        }
                                    } catch (_: Exception) { // a failed execution is caught by catch
                                        removeError = true
                                    }
                                }
                            },
                            confirmText = "Remove",
                            dismissState = { showDialog = false },
                            cancelText = "Cancel",
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleChip(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val roleName = when (role) {
        UserRole.ADMIN -> "Admin"
        UserRole.OWNER -> "Owner"
        UserRole.ENTRANT -> "Entrant"
    }

    Surface(
        modifier = modifier
            .testTag("adminProfileRole"),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = roleName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatPhone(phone: String): String {
    if (phone.isBlank()) {
        return "Not provided"
    }
    val parsed = parsePhoneNumber(phone)
    return "${parsed.country.dialCode} ${formatLocalNumber(parsed.country, parsed.localNumber)}"
}
package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.model.User
import com.example.lambda_bbb_events.view.ui.components.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle

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
    var phone by remember { mutableStateOf(user.phone) }
    var showCreationError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Edit Profile", navController, showNotifications = false)

        Spacer(Modifier.height(16.dp))

        // placeholder: profile picture goes here
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(165.dp)
        )

        Spacer(Modifier.height(16.dp))

        FormTextFieldWithTitle(
            title = "Name",
            value = name,
            onValueChange = { name = it },
            placeholder = "Yuta",
        )

        FormTextFieldWithTitle(
            title = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "yuta@hotmail.com",
        )

        FormTextFieldWithTitle(
            title = "Phone Number",
            value = phone,
            onValueChange = { phone = it },
            placeholder = "+1",
        )

        Spacer(Modifier.height(64.dp))

        Button(
            onClick = {
                val successful = controllers.users.updateEntrant(name.trim(), email.trim(), phone.trim())
                if (successful) {
                    navController.navigate("profile")
                } else {
                    showCreationError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Save Changes", fontWeight = FontWeight.Bold)
        }

        if (showCreationError) {
            Text("Unable to create account. Please review your information.")
        }
    }
}
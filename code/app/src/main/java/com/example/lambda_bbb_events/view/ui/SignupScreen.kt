package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.view.ui.components.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(
    navController: NavController,
    controllers: AppControllers? = null,
    onCreateEntrant: suspend (String, String, String) -> Boolean =
        { name, email, phone ->
            requireNotNull(controllers)
                .users
                .createEntrant(name, email, phone)
        }
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showCreationError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().testTag("signupScreen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Sign Up", navController, showNotifications = false)

        Spacer(Modifier.height(16.dp))

        // placeholder for logo, change with real logo later if were even adding one
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 120.dp)
                .background(Color(0xFF708096), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("LOGO", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Lambda Camping",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        FormTextFieldWithTitle(
            title = "Name",
            value = name,
            onValueChange = { name = it },
            placeholder = "Yuta",
            modifier = Modifier.testTag("nameInput"),
            titleTag = "nameLabel"
        )

        FormTextFieldWithTitle(
            title = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "yuta@hotmail.com",
            modifier = Modifier.testTag("emailInput"),
            titleTag = "emailLabel"
        )

        FormTextFieldWithTitle(
            title = "Phone Number",
            value = phone,
            onValueChange = { phone = it },
            placeholder = "+1",
            modifier = Modifier.testTag("phonenumberInput"),
            titleTag = "phonenumberLabel"
        )

        Spacer(Modifier.height(64.dp))

        Button(
            onClick = {
                scope.launch {
                    val successful = onCreateEntrant(
                        name.trim(),
                        email.trim(),
                        phone.trim()
                    )

                    if (!successful) {
                        showCreationError = true
                    }
                }
            }
        )
        {
            Text("Continue", fontWeight = FontWeight.Bold)
        }

        if (showCreationError) {
            Text("Unable to create account. Please review your information.")
        }
    }
}
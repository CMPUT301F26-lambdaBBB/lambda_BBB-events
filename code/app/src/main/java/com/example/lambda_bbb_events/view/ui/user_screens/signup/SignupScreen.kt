package com.example.lambda_bbb_events.view.ui.user_screens.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.formatting.DEFAULT_COUNTRY_CODE
import com.example.lambda_bbb_events.model.formatting.formatPhoneNumber
import com.example.lambda_bbb_events.view.ui.components.common.AppLogo
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.PhoneNumberFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import kotlinx.coroutines.launch

/*
 * Provides the signup screen for creating a new entrant account.
 *
 * TODO: Replace the logo placeholder when a final logo is added.
 * TODO: Add loading and error handling during account creation.
 */

/**
 * Displays the account registration screen.
 *
 * Requires the user's name, email, and phone number
 * and attempts to create an entrant account.
 * Displays an error message if registration fails.
 *
 * @param navController The [NavController] used for navigation.
 * @param controllers Optional [AppControllers] used for account
 * creation when no custom callback is provided.
 * @param onCreateEntrant Callback that submits user information
 * and returns whether account creation succeeded.
 * Defaults to using the user controller.
 * @param showBack Whether to show a back button, such as when opened from the welcome screen.
 */
@Composable
fun SignupScreen(
    navController: NavController,
    controllers: AppControllers? = null,
    showBack: Boolean = false,
    onCreateEntrant: suspend (String, String, String) -> Boolean =
        { name, email, phone ->
            requireNotNull(controllers)
                .users
                .createEntrant(name, email, phone)
        }
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneCountry by remember { mutableStateOf(DEFAULT_COUNTRY_CODE) }
    var phone by remember { mutableStateOf("") }
    var showCreationError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(vertical = 12.dp)
            .testTag("signupScreen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Sign Up", navController, showBack = showBack, showNotifications = false)

        Spacer(Modifier.height(16.dp))

        AppLogo(size = 120.dp)
        Text(
            text = "Lambda Camping",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
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

        PhoneNumberFieldWithTitle(
            title = "Phone Number",
            country = phoneCountry,
            onCountryChange = { phoneCountry = it },
            number = phone,
            onNumberChange = { phone = it },
            modifier = Modifier.testTag("phonenumberInput"),
            titleTag = "phonenumberLabel"
        )

        Spacer(Modifier.height(64.dp))

        Button(
            modifier = Modifier.testTag("continueButton"),
            onClick = {
                scope.launch {
                    val successful = onCreateEntrant(
                        name.trim(),
                        email.trim(),
                        formatPhoneNumber(phoneCountry, phone)
                    )

                    if (!successful) {
                        showCreationError = true
                    }
                }
            }
        )
        {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (showCreationError) {
            Text(
                // A device can only have one account, which is kept after logging out
                text = if (controllers?.users?.hasAccountOnDevice == true) {
                    "This device already has an account. Go back and log in instead."
                } else {
                    "Unable to create account. Please review your information."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
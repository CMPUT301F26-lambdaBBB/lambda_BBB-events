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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.formatting.DEFAULT_COUNTRY_CODE
import com.example.lambda_bbb_events.model.formatting.formatPhoneNumber
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.PhoneNumberFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import kotlinx.coroutines.launch

/*
 * Lets a user log back in to the account on this device after logging out.
 */

/**
 * Displays the login screen. Accounts belong to the device they were made on, so the
 * email and phone number entered are checked against this device's account, and if they
 * match the main app opens.
 *
 * @param navController The [NavController] used to go back to the welcome screen.
 * @param controllers Optional [AppControllers] used to log in when no custom
 * callback is provided.
 * @param onLogin Callback that submits the email and phone number and returns whether
 * logging in succeeded. Defaults to using the user controller.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    controllers: AppControllers? = null,
    onLogin: suspend (String, String) -> Boolean = { email, phone ->
        requireNotNull(controllers).users.login(email, phone)
    }
) {
    var email by remember { mutableStateOf("") }
    var phoneCountry by remember { mutableStateOf(DEFAULT_COUNTRY_CODE) }
    var phone by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var loggingIn by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
            .testTag("loginScreen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Log In", navController, showBack = true, showNotifications = false)

        Text(
            text = "Enter the email and phone number of the account on this device.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        FormTextFieldWithTitle(
            title = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "yuta@hotmail.com",
            modifier = Modifier.testTag("loginEmailInput"),
            titleTag = "loginEmailLabel"
        )

        PhoneNumberFieldWithTitle(
            title = "Phone Number",
            country = phoneCountry,
            onCountryChange = { phoneCountry = it },
            number = phone,
            onNumberChange = { phone = it },
            modifier = Modifier.testTag("loginPhoneInput"),
            titleTag = "loginPhoneLabel"
        )

        Spacer(Modifier.height(48.dp))

        Button(
            modifier = Modifier.testTag("loginSubmitButton"),
            enabled = !loggingIn,
            onClick = {
                loggingIn = true
                loginError = ""
                scope.launch {
                    val successful = try {
                        onLogin(email.trim(), formatPhoneNumber(phoneCountry, phone))
                    } catch (_: Exception) {
                        false
                    }
                    // On success the app switches to the main screens by itself
                    loggingIn = false
                    if (!successful) {
                        loginError = if (controllers?.users?.hasAccountOnDevice == false) {
                            "This device has no account. Go back and sign up instead."
                        } else {
                            "The email or phone number doesn't match this device's account."
                        }
                    }
                }
            }
        ) {
            Text("Log In", style = MaterialTheme.typography.labelLarge)
        }

        if (loginError.isNotEmpty()) {
            Text(
                text = loginError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("loginError")
            )
        }
    }
}

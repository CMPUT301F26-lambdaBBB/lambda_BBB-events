package com.example.lambda_bbb_events.view.ui.user_screens.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.view.ui.components.common.AppLogo

/*
 * The first screen shown to a device without an account, where the user picks
 * between logging in and signing up.
 */

/**
 * Lets the user choose between logging in to an existing account and signing up.
 *
 * @param onLogin Called when the Log In button is pressed.
 * @param onSignUp Called when the Sign Up button is pressed.
 */
@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onSignUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 24.dp)
            .testTag("welcomeScreen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppLogo(size = 160.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Lambda Camping",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("loginButton")
        ) {
            Text("Log In", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSignUp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signupButton")
        ) {
            Text("Sign Up", style = MaterialTheme.typography.labelLarge)
        }
    }
}

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.lambda_bbb_events.controller.UserController
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle

@Composable
fun SignupScreen(
    navController: NavController,
    userController: UserController
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showCreationError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
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

        Text(
            text = "Name",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        FormTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Yuta",
        )

        Text(
            text = "Email",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        FormTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "yuta@hotmail.com",
        )

        Text(
            text = "Phone Number",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        FormTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = "+1",
        )

        Spacer(Modifier.height(64.dp))

        Button(
            onClick = {
                val successful = userController.createEntrant(name.trim(), email.trim(), phone.trim())
                if (successful) {
                    navController.navigate("home")
                } else {
                    showCreationError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Continue", fontWeight = FontWeight.Bold)
        }

        if (showCreationError) {
            Text("Unable to create account. Please review your information.")
        }
    }
}

@Composable
fun FormTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            // to yuta: change these so colors are not hard coded; add colors in ui.theme.Color

            // this is background color of search bar
            focusedContainerColor = Color(0xFFF2F2F2),
            unfocusedContainerColor = Color(0xFFF2F2F2),

            // this is border color of search bar
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = Color.Gray,

            // this is your cursor color, the thing that's blinking when u type
            cursorColor = Color.Gray,

            // this is color of the label at the top of the search bar when typing, u can remove if no likey
            focusedLabelColor = Color.Gray,
            unfocusedLabelColor = Color.Gray,
        )
    )
}
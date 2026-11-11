package com.example.lambda_bbb_events.view.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/*
 * Defines reusable single-line and multi-line text fields
 * for forms, including fields with titles.
 *
 */


/**
 * Displays a multi-line text field for longer form responses.
 *
 * @param value The current text displayed in the field.
 * @param onValueChange Callback invoked when the user edits the text.
 * @param placeholder Text displayed when the field is empty.
 * @param modifier Modifier used to customize the text field layout.
 */
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFFFFFF),
            unfocusedContainerColor = Color(0xFFFFFFFF),
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = Color.Gray,
            cursorColor = Color.Gray,
            focusedLabelColor = Color.Gray,
            unfocusedLabelColor = Color.Gray,
        )
    )
}

/**
 * Displays a single-line form text field with a title above it.
 *
 * Uses [FormTextField] to display the editable input field.
 *
 * @param title Text displayed above the input field.
 * @param value The current text displayed in the field.
 * @param onValueChange Callback invoked when the user edits the text.
 * @param placeholder Text displayed when the field is empty.
 * @param modifier Modifier passed to the underlying text field.
 * @param titleTag Test tag used to identify the title in UI tests.
 */

@Composable
fun FormTextFieldWithTitle(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    titleTag: String = ""
) {
    Column(
        modifier = Modifier
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).testTag(titleTag)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FormTextField(
            value,
            onValueChange,
            placeholder,
            modifier = modifier
        )
    }
}

/**
 * Displays a multi-line text field for entering longer form responses.
 *
 * @param value The current text displayed in the field.
 * @param onValueChange Callback invoked when the user edits the text.
 * @param placeholder Text displayed when the field is empty.
 * @param modifier Modifier intended to customize the text field layout.
 * Currently unused by the implementation.
 */

@Composable
fun FormTextFieldMultiLine(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = false,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFFFFFF),
            unfocusedContainerColor = Color(0xFFFFFFFF),
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = Color.Gray,
            cursorColor = Color.Gray,
            focusedLabelColor = Color.Gray,
            unfocusedLabelColor = Color.Gray,
        )
    )
}

/**
 * Displays a customizable title bar for application screens.
 *
 * The back button returns to the previous
 * screen and the notification button navigates to the notifications screen.
 *
 * @param title Text displayed as the screen heading.
 * @param navController The [androidx.navigation.NavController] used to navigate
 * between screens and return to the previous screen.
 * @param modifier Modifier used to customize the title bar's
 * layout and appearance.
 * @param showBack Whether to display the back button.
 * Defaults to false.
 * @param showNotifications Whether to display the
 * notifications button. Defaults to true.
 */
@Composable
fun ScreenTitle(
    title: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    showNotifications: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        if (showBack) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        Text(
            title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center).testTag("screenTitle")
        )
        if (showNotifications) {
            IconButton(
                onClick = { navController.navigate("notifications") }, // change this to notification screen once implemented
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }
        }
    }
}

/**
 * Displays a subtitle for a section of the application.
 *
 * @param title The text displayed as the subtitle.
 */
@Composable
fun SubTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
}
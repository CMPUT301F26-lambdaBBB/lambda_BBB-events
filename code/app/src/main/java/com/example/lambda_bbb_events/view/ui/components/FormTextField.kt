package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            // to yuta: change these so colors are not hard coded; add colors in ui.theme.Color

            // this is background color of search bar
            focusedContainerColor = Color(0xFFFFFFFF),
            unfocusedContainerColor = Color(0xFFFFFFFF),

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

@Composable
fun FormTextFieldWithTitle(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
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
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            // to yuta: change these so colors are not hard coded; add colors in ui.theme.Color

            // this is background color of search bar
            focusedContainerColor = Color(0xFFFFFFFF),
            unfocusedContainerColor = Color(0xFFFFFFFF),

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
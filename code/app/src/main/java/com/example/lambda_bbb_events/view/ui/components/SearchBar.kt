package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/*
 * Defines a reusable search bar
 */

/**
 * Displays a customizable search bar for entering search queries.
 *
 * @param label Label displayed for the search field.
 * @param placeholder Text displayed when the search field is empty.
 * @param query The current search query entered by the user.
 * @param onQueryChange Callback invoked when the search query changes.
 * @param onSearch Callback invoked when the user presses
 * the search action on the keyboard.
 * @param modifier Modifier used to customize the search bar's
 * layout and appearance.
 */
@Composable
fun SearchBar(
    label: String,
    placeholder: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = { query ->
            onQueryChange(query)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier,
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search Icon")
        },
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
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}
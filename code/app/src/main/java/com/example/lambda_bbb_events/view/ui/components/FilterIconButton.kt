package com.example.lambda_bbb_events.view.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
/**
 * Displays an icon button for opening filtering options.
 *
 * @param onClick Callback invoked when the user clicks
 * the filter button.
 */
@Composable
fun FilterIconButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .background(
                color = Color(0xFFF2F2F2),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("filterButton"),
        shape = RoundedCornerShape(16.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color(0xFFF2F2F2)
        )
    ){
        Icon(
            imageVector = Icons.Outlined.FilterAlt,
            contentDescription = "Open Filters"
        )
    }

}
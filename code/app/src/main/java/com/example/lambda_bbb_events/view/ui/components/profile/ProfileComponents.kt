
package com.example.lambda_bbb_events.view.ui.components.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
 * Defines reusable profile navigation rows and section headers
 * for organizing user profile options.
 */

/**
 * Displays a clickable row for navigating between profile options.
 *
 * @param title The text displayed in the profile row.
 * @param onClick Callback invoked when the user clicks the row.
 * @param modifier Modifier used to customize the row's layout and appearance.
 * @param leadingIcon Composable function that displays the icon
 * at the beginning of the row.
 */
@Composable
fun ProfileRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        leadingIcon()

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "right_arrow",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Displays a section heading for grouping profile options.
 *
 * @param title Text displayed as the section heading.
 * @param modifier Modifier used to customize the heading's
 * layout and appearance.
 */
@Composable
fun ProfileSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
}

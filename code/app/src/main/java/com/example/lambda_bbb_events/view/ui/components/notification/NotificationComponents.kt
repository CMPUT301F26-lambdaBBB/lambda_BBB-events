
package com.example.lambda_bbb_events.view.ui.components.notification

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
 * Defines reusable notification settings rows
 * for displaying notification options.
 */

/**
 * Displays a clickable row representing a notification setting or option.
 *
 * @param title The main text displayed in the notification row.
 * @param subtitle Additional descriptive text displayed below the title.
 * @param onClick Callback invoked when the user clicks the row.
 * @param modifier Modifier used to customize the row's layout and appearance.
 * @param leadingIcon Composable function that displays the icon
 * at the beginning of the row.
 * @param isMuted Whether to display an icon showing that the row is muted.
 * @param onLongClickLabel Describes what pressing and holding does, for screen readers.
 * @param onLongClick Callback invoked when the user presses and holds the row.
 */
@Composable
fun NotificationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    leadingIcon: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        leadingIcon()

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isMuted) {
            // The subtitle already says it is muted
            Icon(
                imageVector = Icons.Outlined.NotificationsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "right_arrow",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Displays a section heading for grouping notification options.
 *
 * @param title Text displayed as the section heading.
 * @param modifier Modifier used to customize the heading's
 * layout and appearance.
 */
@Composable
fun NotificationSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

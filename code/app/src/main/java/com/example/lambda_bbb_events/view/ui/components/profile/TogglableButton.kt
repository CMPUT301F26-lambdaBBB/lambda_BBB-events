
package com.example.lambda_bbb_events.view.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/*
Provides togglable button to be re-used throughout the app to switch between two options
 */

/**
 * Displays an On/Off toggle.
 *
 * @param isOn Whether the toggle is enabled.
 * @param onToggle Called when an option is selected.
 * @param modifier Modifier applied to the toggle.
 * @param onText Displayed Text on "True" button.
 * @param offText Displayed Text on "False" button.
 */
@Composable
fun ToggleButton(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onText: String,
    offText: String,
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // create two toggle options and checks which one is selected.
        listOf(true to onText, false to offText).forEach { (value, title) ->
            val selected = isOn == value
            // checks if current state of button is equal to truth value of button

            // button row where icon and text are housed
            Row(
                modifier = Modifier
                    .clip(shape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent // highlight  the button if selected
                    )
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onToggle(value) }
                    )
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // add check-mark icon if selected
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

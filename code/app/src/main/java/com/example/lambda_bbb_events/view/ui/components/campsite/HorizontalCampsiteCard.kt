
package com.example.lambda_bbb_events.view.ui.components.campsite

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
 * Defines a horizontal card for displaying campsite information.
 *
 * TODO: Replace the placeholder with actual campsite details.
 */

/**
 * Displays a placeholder card for campsite information.
 */
@Composable
fun HorizontalCampsiteCard() {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(220.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SOMETHING stuff here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Allows users to add and remove tags.
 *
 * Displays existing tags as removable chips.
 *
 * @param tags The current list of tags.
 * @param onAddTag Callback for adding a tag.
 * @param onRemoveTag Callback for removing a tag.
 * @param modifier Customizes the editor's layout.
 */
@Composable
fun TagEditor(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddingTag by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        for (tag in tags) {
            TagChip(
                tagText = tag,
                onRemove = {
                    onRemoveTag(tag)
                }
            )
        }

        // Chip that opens the new tag input
        Surface(
            onClick = {
                isAddingTag = !isAddingTag
            },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Add Tags",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    if (isAddingTag) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newTagText,
                onValueChange = {
                    newTagText = it
                },
                placeholder = {
                    Text(
                        text = "New tag name",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Button(
                onClick = {
                    val newTag = newTagText.trim()

                    // Skip empty or duplicate tags
                    if (newTag.isNotEmpty() && !tags.contains(newTag)) {
                        onAddTag(newTag)
                        newTagText = ""
                        isAddingTag = false
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Displays an individual tag as a removable chip.
 *
 * The chip contains the tag name and a close icon
 * that allows the user to remove the tag.
 *
 * @param tagText The text displayed inside the tag chip.
 * @param onRemove Callback invoked when the close icon is clicked.
 */
@Composable
private fun TagChip(
    tagText: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            )
        ) {
            Text(
                text = tagText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Tag",
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        onRemove()
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

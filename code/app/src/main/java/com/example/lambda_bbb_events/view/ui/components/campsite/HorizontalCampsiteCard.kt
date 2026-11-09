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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
    // display the campsite info here someone thx xoxo
    Box(
        Modifier
            .width(220.dp)
            .height(220.dp)
            .background(color = Color.Gray, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("SOMETHING stuff here")
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
    modifier: Modifier = Modifier,
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
                onRemove = { onRemoveTag(tag) }
            )
        }

        // chip that opens the new tag box
        Surface(
            onClick = { isAddingTag = !isAddingTag },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE8F5E9)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Add Tags",
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (isAddingTag) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newTagText,
                onValueChange = { newTagText = it },
                placeholder = { Text("New tag name", color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    val newTag = newTagText.trim()

                    // skip empty tags and tags that are already there
                    if (newTag.isNotEmpty() && !tags.contains(newTag)) {
                        onAddTag(newTag)
                        newTagText = ""
                        isAddingTag = false
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add")
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
 * @param onRemove Callback invoked when the close icon
 * is clicked.
 */
@Composable
private fun TagChip(tagText: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFAFAFA),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = tagText, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Tag",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() },
                tint = Color.Gray
            )
        }
    }
}
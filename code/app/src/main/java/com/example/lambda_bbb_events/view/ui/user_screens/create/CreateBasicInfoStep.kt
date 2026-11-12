package com.example.lambda_bbb_events.view.ui.user_screens.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.campsite.TagEditor

@Composable
internal fun CreateBasicInfoStep(
    form: CreateCampsiteFormState,
    onNext: () -> Unit
) {
    var isOccupancyExpanded by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }
    var isAddingTag by remember { mutableStateOf(false) }
    val occupancyOptions = listOf("1", "2", "3", "4", "5", "6", "7", "10", "15", "20", "25", "50+")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        val remaining = (5 - form.selectedImageUris.size).coerceAtLeast(0)
        form.selectedImageUris.addAll(uris.take(remaining))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FormTextFieldWithTitle(
            title = "Campsite Name *",
            value = form.campsiteName,
            onValueChange = { form.campsiteName = it },
            placeholder = "Enter campsite name",
            modifier = Modifier.testTag("create_name")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Description *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = form.description,
            onValueChange = { form.description = it },
            placeholder = {
                Text(
                    "Enter campsite description",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min=120.dp)
                .animateContentSize()
                .testTag("create_description"),
            minLines = 5,
            maxLines = 10,
            shape = RoundedCornerShape(16.dp),
            singleLine = false
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Max Occupancy *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box {
            OutlinedTextField(
                value = form.maxOccupancy,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select Occupancy",
                        modifier = Modifier.clickable { isOccupancyExpanded = true }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .clickable { isOccupancyExpanded = true },
                shape = RoundedCornerShape(16.dp)
            )
            DropdownMenu(
                expanded = isOccupancyExpanded,
                onDismissRequest = { isOccupancyExpanded = false }
            ) {
                occupancyOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            form.maxOccupancy = option
                            isOccupancyExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tags",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        TagEditor(
            tags = form.tags,
            onAddTag = { tag ->
                form.tags.add(tag)
            },
            onRemoveTag = { tag ->
                form.tags.remove(tag)
            }
        )

        if (isAddingTag) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormTextFieldWithTitle(
                    title = "New Tag",
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    placeholder = "New tag name",
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newTagText.isNotBlank()) {
                            form.tags.add(newTagText.trim())
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Add Photo",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (form.selectedImageUris.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(form.selectedImageUris) { uri ->
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "Photo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Photo",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.TopEnd)
                                .clickable { form.selectedImageUris.remove(uri) }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (form.selectedImageUris.isEmpty()) "+ Add Photo" else "+ Add More Photos (${form.selectedImageUris.size} selected)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Add Photo",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onNext() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Add Locations →", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
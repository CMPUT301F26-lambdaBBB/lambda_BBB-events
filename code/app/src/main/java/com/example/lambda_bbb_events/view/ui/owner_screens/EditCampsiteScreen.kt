
package com.example.lambda_bbb_events.view.ui.owner_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldMultiLine
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.campsite.TagEditor
import kotlinx.coroutines.launch

/*
 * Provides a screen for editing campsite details.
 * Allows owners to save changes or cancel editing.
 *
 * TODO: Implement campsite photo viewing and management.
 */

/**
 * Allows users to edit campsite details and tags.
 *
 * Saves or discards changes and displays errors when needed.
 *
 * @param navController Handles screen navigation.
 * @param controllers Retrieves and updates campsite data.
 * @param campsiteId The ID of the campsite being edited.
 */
@Composable
fun EditCampsiteScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)
    val scope = rememberCoroutineScope()

    if (campsite == null) {
        Text(
            text = "Campsite not found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    var name by remember(campsiteId) {
        mutableStateOf(campsite.campsiteName)
    }

    var address by remember(campsiteId) {
        mutableStateOf(campsite.address)
    }

    var maxOccupancy by remember(campsiteId) {
        mutableStateOf(campsite.maxOccupancy.toString())
    }

    var description by remember(campsiteId) {
        mutableStateOf(campsite.description)
    }

    var rulesPolicies by remember(campsiteId) {
        mutableStateOf(campsite.rulesPolicies)
    }

    val tags = remember(campsiteId) {
        campsite.tags.toMutableStateList()
    }

    var showError by remember(campsiteId) {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTitle(
            title = "Edit campsite",
            navController = navController,
            showBack = true
        )

        // Scrollable campsite editing form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormTextFieldWithTitle(
                title = "Campsite name",
                value = name,
                onValueChange = {
                    name = it
                },
                placeholder = "Campsite name"
            )

            // Photos placeholder
            Text(
                text = "Photos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Some images here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = {
                    // TODO: Open the photo manager.
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Manage photos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Address
            Text(
                text = "Address",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            FormTextFieldMultiLine(
                value = address,
                onValueChange = {
                    address = it
                },
                placeholder = "Address"
            )

            // Max occupancy
            FormTextFieldWithTitle(
                title = "Max Occupancy",
                value = maxOccupancy,
                onValueChange = {
                    maxOccupancy = it
                },
                placeholder = "Number of people"
            )

            // Description
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            FormTextFieldMultiLine(
                value = description,
                onValueChange = {
                    description = it
                },
                placeholder = "Describe the campsite"
            )

            // Rules and policies
            FormTextFieldWithTitle(
                title = "Rules and policies",
                value = rulesPolicies,
                onValueChange = {
                    rulesPolicies = it
                },
                placeholder = "Rules entrants must follow"
            )

            // Tags
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            TagEditor(
                tags = tags,
                onAddTag = {
                    tags.add(it)
                },
                onRemoveTag = {
                    tags.remove(it)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Validation error
        if (showError) {
            Text(
                text = "Please enter a name, an address, and a max occupancy of at least 1.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Fixed bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        val saved = controllers.campsites.updateCampsite(
                            campsiteId,
                            name,
                            address,
                            maxOccupancy,
                            description,
                            rulesPolicies,
                            tags
                        )

                        if (saved) {
                            navController.popBackStack()
                        } else {
                            showError = true
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save changes",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

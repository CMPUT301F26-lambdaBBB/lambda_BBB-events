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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.view.ui.components.FormTextFieldMultiLine
import com.example.lambda_bbb_events.view.ui.components.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.TagEditor
import kotlinx.coroutines.launch


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
        Text("Campsite not found")
        return
    }

    var name by remember { mutableStateOf(campsite.campsiteName) }
    var address by remember { mutableStateOf(campsite.address) }
    var maxOccupancy by remember { mutableStateOf(campsite.maxOccupancy.toString()) }
    var description by remember { mutableStateOf(campsite.description) }
    var rulesPolicies by remember { mutableStateOf(campsite.rulesPolicies) }
    val tags = remember { campsite.tags.toMutableStateList() }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScreenTitle("Edit campsite", navController, showBack = true)

        // the form scrolls, the Cancel and Save buttons stay at the bottom
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormTextFieldWithTitle(
                title = "Campsite name",
                value = name,
                onValueChange = { name = it },
                placeholder = "Campsite name"
            )

            // photos placeholder
            Text("Photos", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Gray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("some images here", color = Color.White)
            }
            OutlinedButton(
                onClick = { /* open the photo manager later */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage photos", color = Color.Black)
            }

            Text(
                text = "Address",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            FormTextFieldMultiLine(
                value = address,
                onValueChange = { address = it },
                placeholder = "Address"
            )

            FormTextFieldWithTitle(
                title = "Max Occupancy",
                value = maxOccupancy,
                onValueChange = { maxOccupancy = it },
                placeholder = "Number of people"
            )

            Text(
                text = "Description",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            FormTextFieldMultiLine(
                value = description,
                onValueChange = { description = it },
                placeholder = "Describe the campsite"
            )

            FormTextFieldWithTitle(
                title = "Rules and policies",
                value = rulesPolicies,
                onValueChange = { rulesPolicies = it },
                placeholder = "Rules entrants must follow"
            )

            Text(
                text = "Tags",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            TagEditor(
                tags = tags,
                onAddTag = { tags.add(it) },
                onRemoveTag = { tags.remove(it) }
            )


            if (showError) {
                Text(
                    text = "Please enter a name, an address, and a max occupancy of at least 1."
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
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
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Save changes")
                }
            }
        }
    }
}
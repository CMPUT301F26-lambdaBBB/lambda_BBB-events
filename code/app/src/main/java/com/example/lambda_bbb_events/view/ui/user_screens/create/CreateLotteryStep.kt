package com.example.lambda_bbb_events.view.ui.user_screens.create

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.view.ui.components.calendar.DatePickerField

@Composable
internal fun CreateLotteryStep(
    form: CreateCampsiteFormState,
    onBack: () -> Unit,
    isSaving: Boolean,
    errorMessage: String?,
    onDone: () -> Unit
) {
    var isSpotsExpanded by remember { mutableStateOf(false) }
    var isEligibilityExpanded by remember { mutableStateOf(false) }
    val spotsOptions = listOf("5", "10", "15", "20", "25", "50", "100")
    val eligibilityOptions = listOf("All Entrants", "Verified Only", "First-time Entrants")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Start Registration *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        DatePickerField(
            value = form.startRegistration,
            onValueChange = { form.startRegistration = it },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "End Registration *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        DatePickerField(
            value = form.endRegistration,
            onValueChange = { form.endRegistration = it },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Start Booking *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        DatePickerField(
            value = form.startBooking,
            onValueChange = { form.startBooking = it },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "End Booking *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        DatePickerField(
            value = form.endBooking,
            onValueChange = { form.endBooking = it },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Number of Spots *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = form.numberOfSpots,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Spots",
                                modifier = Modifier.clickable {
                                    isSpotsExpanded = true
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSpotsExpanded = true },
                        shape = RoundedCornerShape(16.dp)
                    )
                    DropdownMenu(
                        expanded = isSpotsExpanded,
                        onDismissRequest = { isSpotsExpanded = false }
                    ) {
                        spotsOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    form.numberOfSpots = option
                                    isSpotsExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Eligibility *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = form.eligibility,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Eligibility",
                                modifier = Modifier.clickable {
                                    isEligibilityExpanded = true
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEligibilityExpanded = true },
                        shape = RoundedCornerShape(16.dp)
                    )
                    DropdownMenu(
                        expanded = isEligibilityExpanded,
                        onDismissRequest = { isEligibilityExpanded = false }
                    ) {
                        eligibilityOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    form.eligibility = option
                                    isEligibilityExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Additional Rules",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = form.additionalRules,
            onValueChange = { form.additionalRules = it },
            placeholder = { Text("Enter additional rules", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min=120.dp)
                .animateContentSize(),
            minLines = 2,
            maxLines = 5,
            shape = RoundedCornerShape(16.dp),
            singleLine = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("← Back", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = {
                    onDone()
                },
                enabled = !isSaving,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (isSaving) {
            Text("Saving campsite...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

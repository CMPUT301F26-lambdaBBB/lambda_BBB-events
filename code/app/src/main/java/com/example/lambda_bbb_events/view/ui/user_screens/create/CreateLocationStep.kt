package com.example.lambda_bbb_events.view.ui.user_screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.map.ClickableMap

@Composable
internal fun CreateLocationStep(
    form: CreateCampsiteFormState,
    showMap: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = form.searchLocation,
            onValueChange = { form.searchLocation = it },
            placeholder = { Text("Search location...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        if (showMap) {
            Spacer(modifier = Modifier.height(4.dp))
            ClickableMap(
                height = 220,
                onLocationSelected = { coordinates, fetchedAddress, fetchedPostalCode ->
                    form.latitude = coordinates.latitude
                    form.longitude = coordinates.longitude
                    if (fetchedAddress.isNotBlank()) {
                        form.address = fetchedAddress
                        form.searchLocation = fetchedAddress
                    }
                    if (fetchedPostalCode.isNotBlank()) {
                        form.postalCode = fetchedPostalCode
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        FormTextFieldWithTitle(
            title = "Address",
            value = form.address,
            onValueChange = { form.address = it },
            placeholder = "Enter street address",
            modifier = Modifier.testTag("create_address")
        )

        Spacer(modifier = Modifier.height(12.dp))

        FormTextFieldWithTitle(
            title = "Postal Code",
            value = form.postalCode,
            onValueChange = { form.postalCode = it },
            placeholder = "Enter postal code",
            modifier = Modifier.testTag("create_postal_code")
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
                onClick = { onNext() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    "Set Lottery →",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

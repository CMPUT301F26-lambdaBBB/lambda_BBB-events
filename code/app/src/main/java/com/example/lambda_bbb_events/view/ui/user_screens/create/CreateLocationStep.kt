package com.example.lambda_bbb_events.view.ui.user_screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.services.PlaceSearch
import com.example.lambda_bbb_events.view.ui.components.common.FormTextFieldWithTitle
import com.example.lambda_bbb_events.view.ui.components.map.ClickableMap
import com.example.lambda_bbb_events.view.ui.components.map.LocationSearchBar
import com.example.lambda_bbb_events.view.ui.components.map.MapMove
import com.google.android.gms.maps.model.LatLng

/**
 * The second step of creating a campsite, for choosing where it is. The location can be
 * searched for by name or tapped on the map, and its address can then be edited.
 *
 * @param form The details entered so far.
 * @param showMap Whether to show the map.
 * @param placeSearch Finds places by name for the search bar.
 * @param onBack Called to go back to the previous step.
 * @param onNext Called to go on to the next step.
 */
@Composable
internal fun CreateLocationStep(
    form: CreateCampsiteFormState,
    showMap: Boolean,
    placeSearch: PlaceSearch,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    // Only where the map starts, so it does not reload each time a location is chosen
    val initialLocation = remember { LatLng(form.latitude, form.longitude) }
    var move by remember { mutableStateOf<MapMove?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LocationSearchBar(
            query = form.searchLocation,
            onQueryChange = { form.searchLocation = it },
            placeSearch = placeSearch,
            near = LatLng(form.latitude, form.longitude),
            onPlaceSelected = { place ->
                val location = LatLng(place.latitude, place.longitude)
                form.latitude = location.latitude
                form.longitude = location.longitude
                form.address = place.address
                form.postalCode = place.postalCode // the old one was for another place
                move = MapMove(location)
            }
        )

        if (showMap) {
            Spacer(modifier = Modifier.height(4.dp))
            ClickableMap(
                height = 220,
                initialLocation = initialLocation,
                move = move,
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

package com.example.lambda_bbb_events.view.ui.user_screens.create

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Shared state for the three steps of the campsite creation form.
 *
 * Stores user inputs for basic information, location, and
 * lottery configuration. The state is shared across all
 * creation steps to preserve values during navigation.
 */
internal class CreateCampsiteFormState {

    // Basic Info
    var campsiteName by mutableStateOf("")
    var description by mutableStateOf("")
    var maxOccupancy by mutableStateOf("1")
    val tags = mutableStateListOf("Wifi", "Lake View", "Pet Friendly")
    val selectedImageUris = mutableStateListOf<Uri>()

    // Location
    var searchLocation by mutableStateOf("")
    var address by mutableStateOf("")
    var postalCode by mutableStateOf("")
    var latitude by mutableStateOf(51.1869)
    var longitude by mutableStateOf(-115.5323)

    // Lottery
    var startRegistration by mutableStateOf("")
    var endRegistration by mutableStateOf("")
    var startBooking by mutableStateOf("")
    var endBooking by mutableStateOf("")
    var numberOfSpots by mutableStateOf("10")
    var eligibility by mutableStateOf("All Entrants")
    var additionalRules by mutableStateOf("")
}

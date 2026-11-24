package com.example.lambda_bbb_events.view.ui.components.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.services.PlaceDetails
import com.example.lambda_bbb_events.model.services.PlaceSearch
import com.example.lambda_bbb_events.model.services.PlaceSearchLimitException
import com.example.lambda_bbb_events.model.services.PlaceSearchSwitchedException
import com.example.lambda_bbb_events.model.services.PlaceSuggestion
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

/*
 * Provides a search bar that recommends places while the user types.
 */

/** How long typing must pause before searching, so that not every letter is a request. */
private const val TYPING_PAUSE_MS = 400L

/** The fewest characters to search for. */
private const val MIN_QUERY_LENGTH = 3

/**
 * Displays a search bar for finding a place by name, like the one in Google Maps.
 *
 * Recommended places are listed below the bar while the user types. Choosing one, or pressing
 * search on the keyboard to choose the first, passes its location to [onPlaceSelected].
 *
 * Only text typed here is searched for. Text set by the screen, such as the address of a
 * location tapped on the map, is shown without searching.
 *
 * @param query The text in the search bar.
 * @param onQueryChange Called when the text changes, including to the name of a chosen place.
 * @param placeSearch Finds the places.
 * @param near Where to prefer places near.
 * @param onPlaceSelected Called with the location and address of the chosen place.
 * @param modifier Modifier for styling and layout.
 */
@Composable
fun LocationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeSearch: PlaceSearch,
    near: LatLng,
    onPlaceSelected: (PlaceDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    // What the user typed and is still choosing a place for, or empty when not searching
    var typedQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<PlaceSuggestion>()) }
    // The text the suggestions are for, which is behind what was typed until the search finishes
    var suggestedFor by remember { mutableStateOf("") }
    var isSuggesting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var sessionToken by remember { mutableStateOf(UUID.randomUUID().toString()) }
    // The text being searched for again, because its place could not be looked up
    var searchedAgain by remember { mutableStateOf<String?>(null) }
    var choosing by remember { mutableStateOf<Job?>(null) }
    val isLoading = isSuggesting || choosing?.isActive == true
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // False once the text is replaced by something else, such as a tapped location's address
    val isSearching = typedQuery.isNotEmpty() && typedQuery == query

    // Restarts on every change, which cancels the search for the earlier text
    LaunchedEffect(typedQuery) {
        val text = typedQuery.trim()
        if (text.length < MIN_QUERY_LENGTH) {
            suggestions = emptyList()
            isSuggesting = false
            return@LaunchedEffect
        }

        delay(TYPING_PAUSE_MS)
        isSuggesting = true
        try {
            suggestions = placeSearch.suggest(text, near.latitude, near.longitude, sessionToken)
            suggestedFor = text
            message = when {
                suggestions.isEmpty() -> "No places found."
                text == searchedAgain -> "Choose the place again."
                else -> null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            suggestions = emptyList()
            message = errorMessage(e)
        }
        isSuggesting = false
    }

    fun choose(suggestion: PlaceSuggestion) {
        focusManager.clearFocus()
        typedQuery = ""
        message = null
        onQueryChange(suggestion.name)

        choosing?.cancel() // a slow earlier choice must not replace this one
        choosing = scope.launch {
            try {
                onPlaceSelected(placeSearch.getPlace(suggestion, sessionToken))
            } catch (e: CancellationException) {
                throw e
            } catch (_: PlaceSearchSwitchedException) {
                searchedAgain = suggestion.name.trim()
                typedQuery = suggestion.name
            } catch (e: Exception) {
                message = errorMessage(e)
            }
            sessionToken = UUID.randomUUID().toString() // the next search is a new session
            choosing = null
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                message = null
                searchedAgain = null
                typedQuery = it
                onQueryChange(it)
            },
            placeholder = { Text("Search location...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            message = null
                            typedQuery = ""
                            onQueryChange("")
                        },
                        modifier = Modifier.testTag("locationSearchClear")
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    val first = suggestions.firstOrNull()
                    if (isSearching && first != null && suggestedFor == typedQuery.trim()) {
                        choose(first)
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("locationSearchInput"),
            shape = RoundedCornerShape(16.dp)
        )

        if (isSearching && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("locationSuggestions"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                for (suggestion in suggestions) {
                    SuggestionRow(
                        suggestion = suggestion,
                        onClick = { choose(suggestion) }
                    )
                }
            }
        }

        message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/** Displays one recommended place. */
@Composable
private fun SuggestionRow(
    suggestion: PlaceSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("locationSuggestion_${suggestion.placeId}")
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (suggestion.area.isNotBlank()) {
                Text(
                    text = suggestion.area,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun errorMessage(error: Exception): String =
    if (error is PlaceSearchLimitException) {
        "Search limit reached. Tap the map to choose a location."
    } else {
        "Search is unavailable. Tap the map to choose a location."
    }

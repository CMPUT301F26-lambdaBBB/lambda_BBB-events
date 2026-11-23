package com.example.lambda_bbb_events.view.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.model.COUNTRY_CODES
import com.example.lambda_bbb_events.model.CountryCode

/*
 * Defines a phone number input with a country calling code picker.
 */

/**
 * Displays a phone number input with a title, where the country code is picked from a
 * dropdown inside the field instead of typed.
 *
 * @param title Text displayed above the field.
 * @param country The selected country.
 * @param onCountryChange Called when a different country is picked.
 * @param number The local number, without the country code.
 * @param onNumberChange Called with the digits entered for the local number.
 * @param modifier Modifier applied to the number input.
 * @param titleTag Test tag for the field title.
 */
@Composable
fun PhoneNumberFieldWithTitle(
    title: String,
    country: CountryCode,
    onCountryChange: (CountryCode) -> Unit,
    number: String,
    onNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    titleTag: String = ""
) {
    var pickingCountry by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = if (titleTag.isNotEmpty()) Modifier.testTag(titleTag) else Modifier
        )

        Spacer(modifier = Modifier.height(12.dp))

        FormTextField(
            value = number,
            onValueChange = { input -> onNumberChange(input.filter { it.isDigit() }) },
            placeholder = "Phone number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = modifier,
            // The country code picker sits inside the field, before the number, and its
            // dropdown opens directly below it
            leadingIcon = {
                Box(modifier = Modifier.fillMaxHeight()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { pickingCountry = true }
                            .padding(start = 16.dp)
                            .testTag("country_code_selector")
                    ) {
                        Text(
                            text = "${country.flag} ${country.dialCode}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Choose country code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier
                                .height(28.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    CountryCodeDropdown(
                        expanded = pickingCountry,
                        onDismiss = { pickingCountry = false },
                        onPick = {
                            onCountryChange(it)
                            pickingCountry = false
                        }
                    )
                }
            }
        )
    }
}

/**
 * Displays a searchable dropdown of countries to pick a calling code from.
 *
 * Countries can be found by name, ISO code, or calling code. The search is cleared each
 * time the dropdown closes.
 *
 * @param expanded Whether the dropdown is open.
 * @param onDismiss Called when the dropdown is closed without picking.
 * @param onPick Called with the picked country.
 */
@Composable
private fun CountryCodeDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onPick: (CountryCode) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val trimmed = query.trim()
    val matches = COUNTRY_CODES.filter {
        trimmed.isEmpty() ||
                it.name.contains(trimmed, ignoreCase = true) ||
                it.isoCode.equals(trimmed, ignoreCase = true) ||
                it.dialCode.removePrefix("+").startsWith(trimmed.removePrefix("+"))
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            query = ""
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surface,
        // Limited height so the list scrolls inside the dropdown on small screens
        modifier = Modifier
            .width(280.dp)
            .heightIn(max = 360.dp)
            .testTag("country_code_picker")
    ) {
        FormTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search countries",
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .testTag("country_search")
        )

        matches.forEach { option ->
            DropdownMenuItem(
                leadingIcon = {
                    Text(
                        text = option.flag,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = option.dialCode,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = {
                    query = ""
                    onPick(option)
                },
                modifier = Modifier.testTag("country_option_${option.isoCode}")
            )
        }

        if (matches.isEmpty()) {
            Text(
                text = "No countries found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

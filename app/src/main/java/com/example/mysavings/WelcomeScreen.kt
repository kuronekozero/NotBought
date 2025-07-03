@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.mysavings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher



@Composable
fun WelcomeScreen(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 1. Create a scroll state to remember the scroll position
        val scrollState = rememberScrollState()

        val languageCode by viewModel.languageCode.collectAsState()
        val currencyCode by viewModel.currencyCode.collectAsState()
        var showLanguageMenu by remember { mutableStateOf(false) }
        var showCurrencyMenu by remember { mutableStateOf(false) }

        val languages = mapOf("ru" to "Русский", "en" to "English")
        val currencies = mapOf(
            "RUB" to "₽ Ruble",
            "USD" to "$ Dollar",
            "EUR" to "€ Euro",
            "GBP" to "£ Pound"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                // 2. Add the verticalScroll modifier to make the column scrollable
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            // 3. Remove verticalArrangement, as a scrollable column starts from the top
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.welcome_para1),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.welcome_para2),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.welcome_para3),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Language selection
            Text(stringResource(R.string.welcome_language_select_label), style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = showLanguageMenu,
                onExpandedChange = { showLanguageMenu = !showLanguageMenu }
            ) {
                OutlinedTextField(
                    value = languages[languageCode] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.settings_language_select)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLanguageMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false }
                ) {
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                if (code != languageCode) {
                                    viewModel.onLanguageSelected(code)
                                }
                                showLanguageMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Currency selection
            Text(stringResource(R.string.welcome_currency_select_label), style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = showCurrencyMenu,
                onExpandedChange = { showCurrencyMenu = !showCurrencyMenu }
            ) {
                OutlinedTextField(
                    value = currencies[currencyCode] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.settings_currency_select)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCurrencyMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCurrencyMenu,
                    onDismissRequest = { showCurrencyMenu = false }
                ) {
                    currencies.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                if (code != currencyCode) {
                                    viewModel.onCurrencySelected(code)
                                }
                                showCurrencyMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.welcome_change_later_note),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Start button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.welcome_button_start))
            }
        }
    }
}

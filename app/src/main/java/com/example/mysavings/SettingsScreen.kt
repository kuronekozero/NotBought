@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mysavings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
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
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let { viewModel.exportDataToUri(it) }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.importDataFromUri(it) }
        }
    )

    LaunchedEffect(key1 = Unit) {
        viewModel.uiAction.collectLatest { action ->
            when (action) {
                is SettingsViewModel.UiAction.LaunchExport -> {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    exportLauncher.launch("mysavings_export_$timeStamp.csv")
                }
                is SettingsViewModel.UiAction.LaunchImport -> {
                    // Some file providers label CSVs as "text/comma-separated-values", "application/octet-stream", etc.
                    // Use a broad filter so user can pick any file, we'll validate the extension ourselves.
                    importLauncher.launch(arrayOf("*/*"))
                }
            }
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.snackbarMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.settings_data_management), style = MaterialTheme.typography.titleLarge)
            
            Button(
                onClick = { viewModel.onExportClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_export_button))
            }
            
            OutlinedButton(
                onClick = { viewModel.onImportClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_import_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.settings_language_label), style = MaterialTheme.typography.titleLarge)

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
                                    (context as? Activity)?.recreate()
                                }
                                showLanguageMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Currency selection
            Text(stringResource(R.string.settings_currency_label), style = MaterialTheme.typography.titleLarge)

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
        }
    }
}
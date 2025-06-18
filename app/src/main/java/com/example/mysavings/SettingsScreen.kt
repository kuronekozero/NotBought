package com.example.mysavings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val currentTheme by viewModel.themeOption.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // --- НАЧАЛО НОВОГО КОДА для импорта/экспорта ---
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
                    importLauncher.launch(arrayOf("text/csv"))
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
    // --- КОНЕЦ НОВОГО КОДА ---

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Тема оформления", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            ThemeOptionRow(
                text = "Светлая",
                selected = currentTheme == ThemeOption.LIGHT,
                onClick = { viewModel.setThemeOption(ThemeOption.LIGHT) }
            )
            ThemeOptionRow(
                text = "Темная",
                selected = currentTheme == ThemeOption.DARK,
                onClick = { viewModel.setThemeOption(ThemeOption.DARK) }
            )
            ThemeOptionRow(
                text = "Как в системе",
                selected = currentTheme == ThemeOption.SYSTEM,
                onClick = { viewModel.setThemeOption(ThemeOption.SYSTEM) }
            )

            // --- НАЧАЛО НОВОГО КОДА (кнопки) ---
            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Управление данными", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onExportClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Экспортировать статистику")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.onImportClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Импортировать статистику")
            }
            // --- КОНЕЦ НОВОГО КОДА (кнопки) ---
        }
    }
}

@Composable
private fun ThemeOptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
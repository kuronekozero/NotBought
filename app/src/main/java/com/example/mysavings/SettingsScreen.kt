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

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
        }
    }
}
package com.example.mysavings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val savingEntryDao: SavingEntryDao,
    private val context: Context,
) : ViewModel() {

    fun onWelcomeDismissed() {
        viewModelScope.launch {
            settingsRepository.setWelcomeScreenSeen()
        }
    }

    sealed class UiAction {
        object LaunchExport : UiAction()
        object LaunchImport : UiAction()
    }

    data class UiState(
        val snackbarMessage: String? = null
    )

    private val _uiAction = Channel<UiAction>()
    val uiAction = _uiAction.receiveAsFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val languageCode: StateFlow<String> = settingsRepository.languageCodeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ru")

    val currencyCode: StateFlow<String> = settingsRepository.currencyCodeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "RUB")

    fun onLanguageSelected(code: String) {
        viewModelScope.launch {
            settingsRepository.setLanguageCode(code)
            _uiState.update { it.copy(snackbarMessage = context.getString(R.string.settings_language_changed_message)) }
        }
    }

    fun onCurrencySelected(code: String) {
        viewModelScope.launch {
            settingsRepository.setCurrencyCode(code)
            _uiState.update { it.copy(snackbarMessage = context.getString(R.string.settings_currency_changed_message)) }
        }
    }

    fun onExportClicked() {
        viewModelScope.launch {
            _uiAction.send(UiAction.LaunchExport)
        }
    }

    fun onImportClicked() {
        viewModelScope.launch {
            _uiAction.send(UiAction.LaunchImport)
        }
    }

    fun exportDataToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val entries = savingEntryDao.getAllEntriesList()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    CsvHandler.writeCsv(outputStream, entries)
                }
                _uiState.update { it.copy(snackbarMessage = context.getString(R.string.settings_export_success)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = context.getString(R.string.settings_export_error)) }
            }
        }
    }

    fun importDataFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val entries = CsvHandler.readCsv(inputStream)
                    savingEntryDao.insertAll(entries)
                }
                _uiState.update { it.copy(snackbarMessage = context.getString(R.string.settings_import_success)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = context.getString(R.string.settings_import_error)) }
            }
        }
    }

    fun snackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
    private val savingEntryDao: SavingEntryDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, savingEntryDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
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

    val themeOption: StateFlow<ThemeOption> = settingsRepository.themeOptionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeOption.DARK // Or your default
        )

    fun setThemeOption(option: ThemeOption) {
        viewModelScope.launch {
            settingsRepository.saveThemeOption(option)
        }
    }

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
                _uiState.update { it.copy(snackbarMessage = "Экспорт завершен успешно") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Ошибка при экспорте") }
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
                _uiState.update { it.copy(snackbarMessage = "Импорт завершен успешно") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Ошибка при импорте") }
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
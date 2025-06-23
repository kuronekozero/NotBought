package com.example.mysavings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

class HistoryViewModel(
    private val savingEntryDao: SavingEntryDao,
    private val userCategoryDao: UserCategoryDao
) : ViewModel() {

    val allEntries: StateFlow<List<SavingEntry>> = savingEntryDao.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<UserCategory>> = userCategoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var showDeleteConfirmation by mutableStateOf(false)
    private var entryToDelete: SavingEntry? = null

    var entryName by mutableStateOf("")
    var entryCost by mutableStateOf("")
    var entryCategory by mutableStateOf("")
    var isSavingEntry by mutableStateOf(true)
    private var entryToEdit: SavingEntry? = null

    private val _navigateBack = Channel<Boolean>()
    val navigateBack = _navigateBack.receiveAsFlow()


    fun onDeleteRequest(entry: SavingEntry) {
        entryToDelete = entry
        showDeleteConfirmation = true
    }

    fun confirmDeletion() {
        entryToDelete?.let {
            viewModelScope.launch {
                savingEntryDao.delete(it)
            }
        }
        cancelDeletion()
    }

    fun cancelDeletion() {
        showDeleteConfirmation = false
        entryToDelete = null
    }

    fun loadEntryForEdit(entryId: Int) {
        viewModelScope.launch {
            entryToEdit = savingEntryDao.getEntryById(entryId)
            entryToEdit?.let {
                entryName = it.itemName
                entryCost = abs(it.cost).toString()
                entryCategory = it.category
                isSavingEntry = it.cost >= 0
            }
        }
    }

    fun onEditorNameChange(name: String) { entryName = name }
    fun onEditorCostChange(cost: String) { entryCost = cost }
    fun onEditorCategoryChange(category: String) { entryCategory = category }
    fun onEditorTypeChange(isSaving: Boolean) { isSavingEntry = isSaving }

    fun updateEntry() {
        val costValue = entryCost.toDoubleOrNull()
        if (entryName.isBlank() || costValue == null || costValue <= 0 || entryCategory.isBlank()) {
            return
        }

        entryToEdit?.let {
            val finalCost = if (isSavingEntry) costValue else -costValue
            val updatedEntry = it.copy(
                itemName = entryName,
                cost = finalCost,
                category = entryCategory
            )
            viewModelScope.launch {
                savingEntryDao.update(updatedEntry)
                _navigateBack.send(true)
            }
        }
    }
}

class HistoryViewModelFactory(
    private val savingEntryDao: SavingEntryDao,
    private val userCategoryDao: UserCategoryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(savingEntryDao, userCategoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
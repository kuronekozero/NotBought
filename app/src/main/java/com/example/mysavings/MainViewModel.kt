package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class MainViewModel(
    private val savingEntryDao: SavingEntryDao,
    private val userCategoryDao: UserCategoryDao
) : ViewModel() {
    var itemName by mutableStateOf("")
    var itemCost by mutableStateOf("")
    var selectedCategoryName by mutableStateOf("")
    private var selectedCategoryId: Int? = null

    var newCategoryName by mutableStateOf("")
    var showCategoryDialog by mutableStateOf(false)

    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    var showDatePickerDialog by mutableStateOf(false)
        private set

    val categories: StateFlow<List<UserCategory>> = userCategoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            categories.collect { categoryList ->
                if (selectedCategoryName.isBlank() && categoryList.isNotEmpty()) {
                    onCategoryChange(categoryList.first())
                }
            }
        }
    }

    fun onItemNameChange(name: String) { itemName = name }
    fun onItemCostChange(cost: String) { itemCost = cost }
    fun onCategoryChange(category: UserCategory) {
        selectedCategoryName = category.name
        selectedCategoryId = category.id
    }

    fun saveSavingEntry(entryType: EntryType) {
        val costFromInput = itemCost.toDoubleOrNull()
        if (itemName.isNotBlank() && costFromInput != null && costFromInput > 0 && selectedCategoryName.isNotBlank()) {

            val finalCost = if (entryType == EntryType.WASTE) {
                -costFromInput
            } else {
                costFromInput
            }

            val entryDateTime = LocalDateTime.of(selectedDate, LocalTime.now())

            val newEntry = SavingEntry(
                itemName = itemName,
                cost = finalCost,
                category = selectedCategoryName,
                date = entryDateTime
            )
            viewModelScope.launch {
                savingEntryDao.insert(newEntry)
                itemName = ""
                itemCost = ""
                selectedDate = LocalDate.now()
            }
        } else {
            // Handle error case if needed
        }
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate = date
        closeDatePickerDialog()
    }

    fun openDatePickerDialog() {
        showDatePickerDialog = true
    }

    fun closeDatePickerDialog() {
        showDatePickerDialog = false
    }

    fun openCategoryDialog() { showCategoryDialog = true }
    fun closeCategoryDialog() { showCategoryDialog = false }
    fun onNewCategoryNameChange(name: String) { newCategoryName = name }
    fun addNewCategory() {
        if (newCategoryName.isNotBlank()) {
            viewModelScope.launch {
                userCategoryDao.insert(UserCategory(name = newCategoryName))
                newCategoryName = ""
            }
        }
    }
    fun deleteCategory(category: UserCategory) {
        viewModelScope.launch {
            userCategoryDao.delete(category)
        }
    }
}

class MainViewModelFactory(
    private val savingEntryDao: SavingEntryDao,
    private val userCategoryDao: UserCategoryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(savingEntryDao, userCategoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
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

class MainViewModel(
    private val savingEntryDao: SavingEntryDao,
    private val userCategoryDao: UserCategoryDao // <<<--- Добавляем DAO категорий
) : ViewModel() {

    var itemName by mutableStateOf("")
    var itemCost by mutableStateOf("")

    // Список категорий из базы данных
    val categories: StateFlow<List<UserCategory>> = userCategoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Выбранная категория (храним объект UserCategory или ее имя)
    // Для простоты будем хранить имя, т.к. оно уникально
    var selectedCategoryName by mutableStateOf("")
        private set // Чтобы изменять только через onCategoryChange

    // Состояние для диалога управления категориями
    var showCategoryDialog by mutableStateOf(false)
    var newCategoryNameInput by mutableStateOf("")


    init {
        // При инициализации ViewModel, если список категорий не пуст,
        // устанавливаем selectedCategoryName в первую из списка.
        // Это также поможет, если пользователь удалит текущую выбранную категорию.
        viewModelScope.launch {
            categories.collectLatest { categoryList ->
                if (categoryList.isNotEmpty()) {
                    // Если текущая selectedCategoryName невалидна или пуста, или не содержится в новом списке
                    if (selectedCategoryName.isBlank() || categoryList.none { it.name == selectedCategoryName }) {
                        selectedCategoryName = categoryList.first().name
                    }
                } else {
                    selectedCategoryName = "" // Если категорий нет, сбрасываем
                }
            }
        }
    }


    fun onItemNameChange(newName: String) { itemName = newName }
    fun onItemCostChange(newCost: String) { itemCost = newCost }

    fun onCategoryChange(newCategory: UserCategory) { // Теперь принимаем UserCategory
        selectedCategoryName = newCategory.name
    }
    // Если нужно менять по имени (например, из текстового поля, но у нас dropdown)
    // fun onCategoryNameChange(newCategoryName: String) { selectedCategoryName = newCategoryName }


    fun saveSavingEntry() {
        val cost = itemCost.toDoubleOrNull()
        if (itemName.isNotBlank() && cost != null && cost > 0 && selectedCategoryName.isNotBlank()) {
            val newEntry = SavingEntry(
                itemName = itemName,
                cost = cost,
                category = selectedCategoryName, // Сохраняем имя категории
                date = LocalDateTime.now()
            )
            viewModelScope.launch {
                savingEntryDao.insert(newEntry)
                itemName = ""
                itemCost = ""
                // selectedCategoryName остается (для удобства)
            }
        } else {
            println("Ошибка: Имя, стоимость и категория должны быть заполнены корректно.")
        }
    }

    // --- Функции для управления категориями ---
    fun openCategoryDialog() {
        newCategoryNameInput = "" // Сбрасываем поле ввода при открытии
        showCategoryDialog = true
    }

    fun closeCategoryDialog() {
        showCategoryDialog = false
    }

    fun onNewCategoryNameInputChange(name: String) {
        newCategoryNameInput = name
    }

    fun addUserCategory() {
        val name = newCategoryNameInput.trim()
        if (name.isNotBlank()) {
            viewModelScope.launch {
                userCategoryDao.insert(UserCategory(name = name))
                newCategoryNameInput = "" // Очищаем поле после добавления
                // Диалог можно не закрывать, чтобы пользователь мог добавить еще
            }
        }
    }

    fun deleteUserCategory(category: UserCategory) {
        viewModelScope.launch {
            userCategoryDao.delete(category)
            // Если удаленная категория была выбрана, нужно сбросить selectedCategoryName
            // Логика в init { categories.collectLatest ... } должна это обработать
        }
    }
}

// Обновленная Фабрика для MainViewModel
class MainViewModelFactory(
    private val savingEntryDao: SavingEntryDao,
    private val userCategoryDao: UserCategoryDao // <<<--- Добавляем DAO категорий
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(savingEntryDao, userCategoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
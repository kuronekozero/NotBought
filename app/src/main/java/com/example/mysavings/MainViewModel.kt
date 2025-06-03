package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(private val dao: SavingEntryDao) : ViewModel() {

    // Состояния для полей ввода
    var itemName by mutableStateOf("")
    var itemCost by mutableStateOf("")
    var selectedCategoryName by mutableStateOf(Category.getDisplayNames().first()) // По умолчанию первая категория

    // Список названий категорий для выбора
    val categories = Category.getDisplayNames()

    fun onItemNameChange(newName: String) {
        itemName = newName
    }

    fun onItemCostChange(newCost: String) {
        // Можно добавить валидацию на ввод только цифр
        itemCost = newCost
    }

    fun onCategoryChange(newCategoryName: String) {
        selectedCategoryName = newCategoryName
    }

    fun saveSavingEntry() {
        val cost = itemCost.toDoubleOrNull()
        if (itemName.isNotBlank() && cost != null && cost > 0) {
            val newEntry = SavingEntry(
                itemName = itemName,
                cost = cost,
                category = selectedCategoryName,
                date = LocalDate.now() // Дата устанавливается при создании записи
            )
            viewModelScope.launch { // Запускаем корутину для операции с БД
                dao.insert(newEntry)
                // Очищаем поля после сохранения
                itemName = ""
                itemCost = ""
                // Можно сбросить категорию или оставить текущую для удобства
                // selectedCategoryName = categories.first()
            }
        } else {
            // Здесь можно обработать ошибку ввода (например, показать Snackbar)
            println("Ошибка: Имя и стоимость должны быть заполнены корректно.")
        }
    }
}

// Фабрика для создания MainViewModel с передачей DAO
class MainViewModelFactory(private val dao: SavingEntryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
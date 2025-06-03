package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview // Для предпросмотра
import com.example.mysavings.ui.theme.MySavingsTheme // Твоя тема оформления
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var categoryExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Добавить экономию",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = viewModel.itemName,
            onValueChange = { viewModel.onItemNameChange(it) },
            label = { Text("Название товара/услуги") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.itemCost,
            onValueChange = { viewModel.onItemCostChange(it) },
            label = { Text("Стоимость") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Выпадающий список для категорий (ИСПРАВЛЕННЫЙ БЛОК)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = viewModel.selectedCategoryName,
                onValueChange = { /* Не даем изменять напрямую, только через выбор */ },
                label = { Text("Категория") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { categoryExpanded = true }, // <<<--- ИЗМЕНЕНИЕ ЗДЕСЬ: onTap заменен на clickable
                readOnly = true, // Делаем поле только для чтения
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                }
            )
            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false },
                modifier = Modifier.fillMaxWidth() // Можно указать .exposedDropdownSize(true) для соответствия ширине поля
            ) {
                viewModel.categories.forEach { categoryName ->
                    DropdownMenuItem(
                        text = { Text(categoryName) },
                        onClick = {
                            viewModel.onCategoryChange(categoryName)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }


        Button(
            onClick = { viewModel.saveSavingEntry() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}

// Preview для удобства разработки в Android Studio
@Preview(showBackground = true)
@Composable
fun DefaultPreviewOfMainScreen() {
    // Для превью нам нужен "фейковый" ViewModel или null, если DI не настроен для превью.
    // Поскольку MainViewModel требует DAO, для простого превью можно создать его без DAO,
    // но тогда методы, использующие DAO, вызовут ошибку.
    // Самый простой вариант - не передавать ViewModel или использовать фейковый DAO.
    // Для данного примера, предположим, что MySavingsTheme существует
    MySavingsTheme {
        // MainScreen(viewModel = MainViewModel(FakeSavingEntryDao())) // Пример с фейковым DAO
        // Для простоты, если Fake DAO нет, просто закомментируйте или создайте простой вариант
        Text("Preview requires a ViewModel instance or a simplified Composable for preview.")
    }
}

// Если нужен Fake DAO для превью (необязательно, но полезно):
// class FakeSavingEntryDao : SavingEntryDao {
// override suspend fun insert(entry: SavingEntry) {}
// override fun getAllEntries(): Flow<List<SavingEntry>> = flowOf(emptyList())
// override fun getTotalSaved(): Flow<Double?> = flowOf(0.0)
// override fun getEntriesForDate(date: LocalDate): Flow<List<SavingEntry>> = flowOf(emptyList())
// }
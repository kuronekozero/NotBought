package com.example.mysavings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // Иконка для кнопки управления категориями
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog // Для диалогового окна

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var categoryExpanded by remember { mutableStateOf(false) }
    // Собираем список категорий из ViewModel
    val categoriesList by viewModel.categories.collectAsState()
    // Состояние для диалога (управляется из ViewModel)
    val showDialog = viewModel.showCategoryDialog


    if (showDialog) {
        CategoryManagementDialog(
            viewModel = viewModel,
            categories = categoriesList,
            onDismiss = { viewModel.closeCategoryDialog() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Добавить экономию", style = MaterialTheme.typography.headlineSmall)

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
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )

        // Блок выбора категории и кнопка управления
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) { // Занимает все доступное место, кроме кнопки
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedCategoryName, // Используем имя выбранной категории
                        onValueChange = {},
                        label = { Text("Категория") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        if (categoriesList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Нет категорий. Добавьте категорию.") },
                                onClick = {
                                    categoryExpanded = false
                                    viewModel.openCategoryDialog() // Открыть диалог, если категорий нет
                                }
                            )
                        } else {
                            categoriesList.forEach { category -> // Теперь итерируемся по List<UserCategory>
                                DropdownMenuItem(
                                    text = { Text(category.name) }, // Отображаем имя категории
                                    onClick = {
                                        viewModel.onCategoryChange(category) // Передаем объект UserCategory
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { viewModel.openCategoryDialog() }) {
                Icon(Icons.Filled.Edit, contentDescription = "Управление категориями")
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementDialog(
    viewModel: MainViewModel,
    categories: List<UserCategory>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Управление категориями", style = MaterialTheme.typography.titleLarge)

                // Поле для добавления новой категории
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.newCategoryNameInput,
                        onValueChange = { viewModel.onNewCategoryNameInputChange(it) },
                        label = { Text("Новая категория") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.addUserCategory() }) {
                        Icon(Icons.Filled.Add, contentDescription = "Добавить категорию")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Существующие категории:", style = MaterialTheme.typography.titleMedium)

                // Список существующих категорий для удаления
                if (categories.isEmpty()) {
                    Text("Пользовательских категорий нет.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) { // Ограничиваем высоту списка
                        items(categories) { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category.name)
                                IconButton(onClick = { viewModel.deleteUserCategory(category) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Удалить категорию")
                                }
                            }
                            Divider()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onDismiss) {
                    Text("Закрыть")
                }
            }
        }
    }
}
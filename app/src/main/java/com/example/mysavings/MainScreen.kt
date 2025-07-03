// --- ЗАМЕНИ ВСЕ СОДЕРЖИМОЕ ФАЙЛА MainScreen.kt НА ЭТОТ КОД ---
package com.example.mysavings

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    entryType: EntryType,
    navController: NavController
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    val categoriesList by viewModel.categories.collectAsState()
    val showDialog = viewModel.showCategoryDialog
    val context = LocalContext.current

    if (viewModel.showDatePickerDialog) {
        val calendar = Calendar.getInstance()
        val initialDate = viewModel.selectedDate
        calendar.set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)

        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                viewModel.onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            setOnDismissListener { viewModel.closeDatePickerDialog() }
            show()
        }
    }

    if (showDialog) {
        CategoryManagementDialog(
            viewModel = viewModel,
            categories = categoriesList,
            onDismiss = { viewModel.closeCategoryDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (entryType == EntryType.SAVING) stringResource(R.string.main_add_saving_title) else stringResource(R.string.main_add_waste_title))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.edit_entry_back_button_desc))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = viewModel.itemName,
                onValueChange = { viewModel.onItemNameChange(it) },
                label = { Text(stringResource(R.string.main_item_name_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.itemCost,
                onValueChange = { viewModel.onItemCostChange(it) },
                label = { Text(stringResource(R.string.main_item_cost_hint)) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = viewModel.selectedCategoryName,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.main_category_hint)) },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categoriesList.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        viewModel.onCategoryChange(category)
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = { viewModel.openCategoryDialog() }) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.main_edit_categories_desc))
                }
            }

            val formatter = remember { DateTimeFormatter.ofPattern("dd MMMM yy", Locale.getDefault()) }
            OutlinedButton(
                onClick = { viewModel.openDatePickerDialog() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.main_select_date_desc), modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = viewModel.selectedDate.format(formatter))
            }

            Button(
                onClick = {
                    viewModel.saveSavingEntry(entryType)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (entryType == EntryType.SAVING) stringResource(R.string.main_save_saving_button) else stringResource(R.string.main_save_waste_button))
            }
        }
    }
}

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
                Text(stringResource(R.string.cat_dialog_title), style = MaterialTheme.typography.titleLarge)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.newCategoryName,
                        onValueChange = { viewModel.onNewCategoryNameChange(it) },
                        label = { Text(stringResource(R.string.cat_dialog_new_category_hint)) },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.addNewCategory() }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cat_dialog_add_category_desc))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(R.string.cat_dialog_existing_categories_title), style = MaterialTheme.typography.titleMedium)

                if (categories.isEmpty()) {
                    Text(stringResource(R.string.cat_dialog_no_custom_categories))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(categories) { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category.name)
                                IconButton(onClick = { viewModel.deleteCategory(category) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cat_dialog_delete_category_desc))
                                }
                            }
                            Divider()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cat_dialog_close_button))
                }
            }
        }
    }
}
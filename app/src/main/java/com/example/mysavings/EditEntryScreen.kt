package com.example.mysavings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    navController: NavController,
    viewModel: HistoryViewModel,
    entryId: Int
) {
    LaunchedEffect(key1 = entryId) {
        viewModel.loadEntryForEdit(entryId)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.navigateBack.collect {
            if (it) {
                navController.popBackStack()
            }
        }
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    val categoriesList by viewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_entry_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.edit_entry_back_button_desc))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.updateEntry() }) {
                        Icon(Icons.Filled.Done, contentDescription = stringResource(R.string.edit_entry_save_button_desc))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.entryName,
                onValueChange = viewModel::onEditorNameChange,
                label = { Text(stringResource(R.string.add_entry_title_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.entryCost,
                onValueChange = viewModel::onEditorCostChange,
                label = { Text(stringResource(R.string.edit_entry_cost_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = viewModel.entryCategory,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.add_entry_category_hint)) },
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
                                viewModel.onEditorCategoryChange(category.name)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (viewModel.isSavingEntry) stringResource(R.string.edit_entry_is_saving_label) else stringResource(R.string.edit_entry_is_waste_label),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = viewModel.isSavingEntry,
                    onCheckedChange = viewModel::onEditorTypeChange
                )
            }
        }
    }
}
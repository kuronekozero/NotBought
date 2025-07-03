package com.example.mysavings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, navController: NavController, settingsViewModel: SettingsViewModel) {
    val allEntries by viewModel.allEntries.collectAsState()
    val currencyCode by settingsViewModel.currencyCode.collectAsState()
    val currencyFormat = remember(currencyCode) {
        java.text.NumberFormat.getCurrencyInstance(java.util.Locale.getDefault()).apply {
            currency = java.util.Currency.getInstance(currencyCode)
        }
    }

    if (viewModel.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeletion() },
            title = { Text(stringResource(id = R.string.goals_delete_confirm_title)) },
            text = { Text(stringResource(R.string.history_delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeletion() }) { Text(stringResource(id = R.string.goals_delete_confirm_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeletion() }) { Text(stringResource(id = R.string.goals_delete_confirm_no)) }
            }
        )
    }

    if (allEntries.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.history_empty_state))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allEntries, key = { it.id }) { entry ->
                HistoryItemCard(
                    entry = entry,
                    currencyFormatter = currencyFormat,
                    onEditClick = {
                        navController.navigate("${Screen.EditEntryScreen.route}/${entry.id}")
                    },
                    onDeleteClick = {
                        viewModel.onDeleteRequest(entry)
                    }
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    entry: SavingEntry,
    currencyFormatter: java.text.NumberFormat,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isSaving = entry.cost >= 0
    val indicatorColor = if (isSaving) Color(0xFF4CAF50) else Color(0xFFF44336)
    val indicatorIcon = if (isSaving) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(indicatorColor)
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = entry.itemName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.history_category_label, entry.category),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.date.format(formatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(top = 4.dp, end = 0.dp)
            ) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, stringResource(id = R.string.goals_options_menu_desc))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.goals_edit_option)) },
                            onClick = {
                                onEditClick()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.goals_delete_option)) },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = indicatorIcon,
                        contentDescription = if (isSaving) stringResource(R.string.history_saving_desc) else stringResource(R.string.history_waste_desc),
                        tint = indicatorColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currencyFormatter.format(entry.cost),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = indicatorColor
                    )
                }
            }
        }
    }
}
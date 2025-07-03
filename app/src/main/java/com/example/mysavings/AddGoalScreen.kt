package com.example.mysavings

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(navController: NavController, viewModel: GoalsViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isEditing = viewModel.goalToEdit != null

    LaunchedEffect(key1 = Unit) {
        viewModel.navigateBack.collect {
            if (it) {
                navController.popBackStack()
            }
        }
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val initialDate = viewModel.customStartDate ?: LocalDate.now()
        calendar.set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)

        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                viewModel.onCustomStartDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) stringResource(R.string.goals_edit_title) else stringResource(R.string.goals_new_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.edit_entry_back_button_desc))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveGoal() }) {
                        Icon(Icons.Filled.Done, contentDescription = stringResource(R.string.goals_save_button))
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
                value = viewModel.goalName,
                onValueChange = viewModel::onGoalNameChange,
                label = { Text(stringResource(R.string.goals_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = viewModel.goalDescription,
                onValueChange = viewModel::onGoalDescriptionChange,
                label = { Text(stringResource(R.string.goals_description_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.targetAmount,
                onValueChange = viewModel::onTargetAmountChange,
                label = { Text(stringResource(R.string.goals_target_amount_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!isEditing) {
                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.goals_include_past_savings_label),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = viewModel.includePastSavings,
                        onCheckedChange = viewModel::onIncludePastSavingsChange
                    )
                }

                if (viewModel.includePastSavings) {
                    val formattedDate = viewModel.customStartDate?.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) ?: stringResource(R.string.goals_start_date_placeholder)
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.goals_start_date_button_label, formattedDate))
                    }
                }
            }
        }
    }
}
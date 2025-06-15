package com.example.mysavings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val currentTheme by viewModel.themeOption.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Тема оформления", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        ThemeOptionRow(
            text = "Светлая",
            selected = currentTheme == ThemeOption.LIGHT,
            onClick = { viewModel.setThemeOption(ThemeOption.LIGHT) }
        )
        ThemeOptionRow(
            text = "Темная",
            selected = currentTheme == ThemeOption.DARK,
            onClick = { viewModel.setThemeOption(ThemeOption.DARK) }
        )
        ThemeOptionRow(
            text = "Как в системе",
            selected = currentTheme == ThemeOption.SYSTEM,
            onClick = { viewModel.setThemeOption(ThemeOption.SYSTEM) }
        )
    }
}

@Composable
private fun ThemeOptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
package com.example.mysavings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

enum class EntryType {
    SAVING, WASTE
}

@Composable
fun AddEntryChooserScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Что вы хотите добавить?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("${Screen.MainScreen.route}/${EntryType.SAVING.name}") },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Я сэкономил (не купил)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { navController.navigate("${Screen.MainScreen.route}/${EntryType.WASTE.name}") },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Я потратил (купил зря)")
        }
    }
}
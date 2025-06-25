package com.example.mysavings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Добро пожаловать в My Savings!",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "My Savings — это простое приложение, созданное для того, чтобы помочь тебе отслеживать свои сбережения и траты. Часто бывает сложно понять, куда уходят деньги, или увидеть реальный прогресс в накоплениях, особенно когда речь идёт об экономии от отказа от ненужных покупок. Его основная идея — показать тебе, как ты можешь копить деньги, и визуализировать эффект от сэкономленных средств. Ты сможешь отмечать свои финансовые цели и следить за тем, как сбережения помогают тебе их достигать. Это твой личный инструмент для более осознанного отношения к финансам. Давай начнем!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Давай начнем!")
            }
        }
    }
}
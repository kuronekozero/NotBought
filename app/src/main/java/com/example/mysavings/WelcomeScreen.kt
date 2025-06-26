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
                .padding(horizontal = 24.dp, vertical = 32.dp), // Adjusted horizontal padding for better look
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Headline for the welcome message
            Text(
                text = "Добро пожаловать в My Savings!",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp) // Add padding below the headline
            )

            // First paragraph of the explanatory text
            Text(
                text = "My Savings — это простое приложение, созданное для того, чтобы помочь тебе отслеживать свои сбережения и траты. Часто бывает сложно понять, куда уходят деньги, или увидеть реальный прогресс в накоплениях, особенно когда речь идёт об экономии от отказа от ненужных покупок.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp) // Add horizontal padding for better text flow
            )
            Spacer(modifier = Modifier.height(16.dp)) // Spacer between paragraphs

            // Second paragraph of the explanatory text
            Text(
                text = "Его основная идея — показать тебе, как ты можешь копить деньги, и визуализировать эффект от сэкономленных средств. Ты сможешь отмечать свои финансовые цели и следить за тем, как сбережения помогают тебе их достигать.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp) // Add horizontal padding for better text flow
            )
            Spacer(modifier = Modifier.height(24.dp)) // Spacer before the final call to action

            // Final sentence acting as a call to action
            Text(
                text = "Это твой личный инструмент для более осознанного отношения к финансам.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp) // Add horizontal padding for better text flow
            )
            Spacer(modifier = Modifier.height(32.dp)) // Spacer before the button

            // Start button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // Make the button slightly taller
            ) {
                Text("Давай начнем!")
            }
        }
    }
}

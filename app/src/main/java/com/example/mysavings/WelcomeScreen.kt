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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun WelcomeScreen(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 1. Create a scroll state to remember the scroll position
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                // 2. Add the verticalScroll modifier to make the column scrollable
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            // 3. Remove verticalArrangement, as a scrollable column starts from the top
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Добро пожаловать в My Savings!",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "My Savings — это простое приложение, созданное для того, чтобы помочь тебе отслеживать свои сбережения и траты. Часто бывает сложно понять, куда уходят деньги, или увидеть реальный прогресс в накоплениях, особенно когда речь идёт об экономии от отказа от ненужных покупок.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Его основная идея — показать тебе, как ты можешь копить деньги, и визуализировать эффект от сэкономленных средств. Ты сможешь отмечать свои финансовые цели и следить за тем, как сбережения помогают тебе их достигать.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Это твой личный инструмент для более осознанного отношения к финансам.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Start button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Давай начнем!")
            }
        }
    }
}

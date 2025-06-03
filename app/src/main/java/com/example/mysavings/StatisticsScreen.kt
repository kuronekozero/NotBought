package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.mysavings.ui.theme.MySavingsTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    // Собираем значения из StateFlow
    val totalSaved by viewModel.totalSaved.collectAsState()
    val savedToday by viewModel.savedToday.collectAsState()
    val savedThisWeek by viewModel.savedThisWeek.collectAsState()
    val savedThisMonth by viewModel.savedThisMonth.collectAsState()
    val savedThisYear by viewModel.savedThisYear.collectAsState()

    // Форматтер для валюты (можно настроить под конкретную локаль/валюту)
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU")) // Пример для рублей

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Статистика", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        StatisticRow("Всего сэкономлено:", currencyFormat.format(totalSaved))
        StatisticRow("Сегодня:", currencyFormat.format(savedToday))
        StatisticRow("Эта неделя:", currencyFormat.format(savedThisWeek))
        StatisticRow("Этот месяц:", currencyFormat.format(savedThisMonth))
        StatisticRow("Этот год:", currencyFormat.format(savedThisYear))
    }
}

@Composable
fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    MySavingsTheme {
        // Для превью нужен фейковый ViewModel или данные
        // StatisticsScreen(viewModel = FakeStatisticsViewModel()) // Заглушка
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Статистика (Превью)", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            StatisticRow("Всего сэкономлено:", "10 000 ₽")
            StatisticRow("Сегодня:", "150 ₽")
        }
    }
}
// Для превью можно создать FakeStatisticsViewModel, если нужно:
// class FakeStatisticsViewModel : StatisticsViewModel(FakeSavingEntryDao()) { /* ... */ }
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // Используем itemsIndexed для доступа к index
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysavings.ui.theme.generateDistinctColors // <<<--- ИМПОРТ НАШЕЙ ФУНКЦИИ
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.compose.ui.draw.alpha // <<<--- ВОЗМОЖНО НОВЫЙ ИМПОРТ
import androidx.compose.foundation.layout.padding // Конкретно для функции padding
// или import androidx.compose.foundation.layout.* // Если используешь много всего из layout
import androidx.compose.ui.unit.dp // Для указания размеров в dp


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

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val totalSaved by viewModel.totalSaved.collectAsState()
    val savedToday by viewModel.savedToday.collectAsState()
    val savedThisWeek by viewModel.savedThisWeek.collectAsState()
    val savedThisMonth by viewModel.savedThisMonth.collectAsState()
    val savedThisYear by viewModel.savedThisYear.collectAsState()

    // Собираем данные по категориям
    val savingsByCategory by viewModel.savingsByCategory.collectAsState()

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }

    LazyColumn( // Используем LazyColumn, если контента может быть много
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Немного увеличим отступ
    ) {
        item {
            Text("Статистика", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp)) // Дополнительный отступ после заголовка
        }

        item { StatisticRow("Всего сэкономлено:", currencyFormat.format(totalSaved)) }
        item { StatisticRow("Сегодня:", currencyFormat.format(savedToday)) }
        item { StatisticRow("Эта неделя:", currencyFormat.format(savedThisWeek)) }
        item { StatisticRow("Этот месяц:", currencyFormat.format(savedThisMonth)) }
        item { StatisticRow("Этот год:", currencyFormat.format(savedThisYear)) }

        item {
            // Раздел для статистики по категориям
            if (savingsByCategory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("По категориям", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                CategoryPieChartWithLegend(
                    data = savingsByCategory,
                    currencyFormatter = currencyFormat
                )
            } else {
                // Можно показать сообщение, если данных по категориям нет
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Нет данных по категориям для отображения.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
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

@Composable
fun CategoryPieChartWithLegend(
    data: List<CategorySavings>,
    currencyFormatter: NumberFormat
) {
    val totalAmountAllCategories = remember(data) { data.sumOf { it.totalAmount } }
    val chartColors = remember(data.size) { generateDistinctColors(data.size) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val selectedCategoryInfo = selectedIndex?.let { data.getOrNull(it) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (totalAmountAllCategories > 0 && data.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp)
                    .pointerInput(data, totalAmountAllCategories) { // Перезапускаем обработчик, если данные или сумма изменились
                        detectTapGestures { tapOffset ->
                            if (totalAmountAllCategories <= 0) { // Не обрабатывать клики, если нет данных
                                selectedIndex = null
                                return@detectTapGestures
                            }

                            val canvasWidth = size.width.toFloat()
                            val canvasHeight = size.height.toFloat()
                            val centerX = canvasWidth / 2
                            val centerY = canvasHeight / 2
                            val diameter = minOf(canvasWidth, canvasHeight) * 0.9f
                            val radius = diameter / 2

                            val dx = tapOffset.x - centerX
                            val dy = tapOffset.y - centerY

                            if (dx * dx + dy * dy <= radius * radius) { // Клик внутри круга диаграммы
                                var tapAngleRad = atan2(dy.toDouble(), dx.toDouble()) // Угол в радианах
                                var tapAngleDeg = Math.toDegrees(tapAngleRad).toFloat() // В градусах, -180 to 180

                                // Приводим к диапазону 0-360, где 0 градусов "смотрит" вверх (как наш startAngle)
                                tapAngleDeg = (tapAngleDeg + 450f) % 360f // +450 = +90 (поворот)+360 (нормализация)

                                var currentAngleProgress = 0f
                                var foundIdx: Int? = null
                                for (i in data.indices) {
                                    val proportion = (data[i].totalAmount / totalAmountAllCategories).toFloat()
                                    val sweep = 360f * proportion
                                    val nextAngleProgress = currentAngleProgress + sweep

                                    if (tapAngleDeg >= currentAngleProgress && tapAngleDeg < nextAngleProgress) {
                                        foundIdx = i
                                        break
                                    }
                                    currentAngleProgress = nextAngleProgress
                                }
                                selectedIndex = if (foundIdx == selectedIndex) null else foundIdx
                            } else {
                                selectedIndex = null // Клик вне пирога
                            }
                        }
                    }
            ) { // Начало DrawScope для Canvas
                val canvasWidth = size.width
                val canvasHeight = size.height
                val diameter = minOf(canvasWidth, canvasHeight) * 0.9f
                val topLeftX = (canvasWidth - diameter) / 2
                val topLeftY = (canvasHeight - diameter) / 2

                var startAngleCanvas = -90f // Начинаем рисовать сверху

                data.forEachIndexed { index, categoryData ->
                    val proportion = (categoryData.totalAmount / totalAmountAllCategories).toFloat()
                    val sweepAngle = 360f * proportion
                    val currentColor = chartColors.getOrElse(index) { Color.Gray } // Запасной цвет
                    val finalColor = if (selectedIndex != null && selectedIndex != index) {
                        currentColor.copy(alpha = 0.3f)
                    } else {
                        currentColor
                    }

                    drawArc( // Это вызов метода из DrawScope
                        color = finalColor,
                        startAngle = startAngleCanvas,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(topLeftX, topLeftY),
                        size = Size(diameter, diameter)
                    )

                    if (selectedIndex == index) {
                        drawArc( // Это вызов метода из DrawScope
                            color = Color.Black,
                            startAngle = startAngleCanvas,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(topLeftX, topLeftY),
                            size = Size(diameter, diameter),
                            style = Stroke(width = 2.dp.toPx()) // Немного тоньше обводка
                        )
                    }
                    startAngleCanvas += sweepAngle
                }
            } // Конец DrawScope для Canvas

        } else {
            Text("Нет данных для диаграммы", modifier = Modifier.padding(16.dp))
        }

        // Отображение информации о выбранной категории (текстом, под диаграммой)
        if (selectedIndex != null && selectedCategoryInfo != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = selectedCategoryInfo.categoryName,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = currencyFormatter.format(selectedCategoryInfo.totalAmount),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Расходы по категориям:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            data.forEachIndexed { index, categoryData ->
                val itemColor = chartColors.getOrElse(index) { Color.LightGray }
                val isSelected = selectedIndex == index
                val itemAlpha = if (selectedIndex != null && !isSelected) 0.5f else 1.0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .alpha(itemAlpha),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(itemColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = categoryData.categoryName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = currencyFormatter.format(categoryData.totalAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Bold
                    )
                }
                if (index < data.size - 1) { // Не добавлять Divider после последнего элемента
                    Divider(modifier = Modifier.padding(start = 28.dp).padding(vertical = 4.dp))
                }
            }
        }
    }
}



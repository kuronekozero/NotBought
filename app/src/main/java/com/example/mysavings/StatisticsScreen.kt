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

    // Генерируем цвета на основе количества категорий
    val chartColors = remember(data.size) { // Пересчитываем, если количество категорий изменилось
        generateDistinctColors(data.size)
    }

    // Состояние для хранения индекса выбранного сегмента (-1 означает, что ничего не выбрано)
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Информация о выбранном сегменте
    val selectedCategoryInfo = selectedIndex?.let { data.getOrNull(it) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (totalAmountAllCategories > 0 && data.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // Немного увеличим высоту для информации в центре
                    .padding(16.dp)
                    .pointerInput(data) { // Добавляем обработчик нажатий, перезапускаем если данные изменились
                        detectTapGestures { tapOffset ->
                            val canvasWidth = size.width.toFloat()
                            val canvasHeight = size.height.toFloat()
                            val centerX = canvasWidth / 2
                            val centerY = canvasHeight / 2
                            val diameter = minOf(canvasWidth, canvasHeight) * 0.9f
                            val radius = diameter / 2

                            // Проверяем, находится ли клик внутри окружности пирога
                            val dx = tapOffset.x - centerX
                            val dy = tapOffset.y - centerY
                            if (dx * dx + dy * dy <= radius * radius) {
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f // Нормализуем угол 0-360

                                // Скорректируем угол, если наш pie chart начинается не с 0 градусов (например, с -90)
                                // Наш startAngle для рисования -90 (вверх). atan2(0,-1) -> -90, atan2(1,0) -> 90
                                // Угол от atan2: 0 вправо, 90 вниз, 180 влево, -90 (270) вверх
                                // Нам нужно привести его к системе отсчета, где -90 (или 270) это начало
                                val drawingStartAngleOffset = -90f
                                var currentAngleCheck = drawingStartAngleOffset
                                var foundIndex = -1

                                for (i in data.indices) {
                                    val proportion = (data[i].totalAmount / totalAmountAllCategories).toFloat()
                                    val sweep = 360f * proportion
                                    val endAngleCheck = currentAngleCheck + sweep

                                    // Нормализуем углы для проверки (angle может быть 0-360, currentAngleCheck может быть <0 или >360)
                                    // Эта логика определения попадания в сектор может быть сложной, особенно с учетом нормализации
                                    // Упрощенный вариант (может потребовать доработки для крайних случаев):
                                    // Сначала приведем angle к диапазону, где 0 это наш drawingStartAngleOffset
                                    var adjustedTapAngle = angle
                                    // Если drawingStartAngleOffset -90, то углы от atan2 нужно "повернуть"
                                    // atan2: 0 (вправо), 90 (вниз), 180 (влево), 270 (вверх)
                                    // pie:   0 (вверх),  90 (вправо), 180 (вниз), 270 (влево)
                                    // Нужно преобразование. Проще работать с углами от 0 до 360 и начальным углом 0.

                                    // Пересчитаем углы для проверки от 0 (справа) до 360
                                    var tapAngleNormalized = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (tapAngleNormalized < 0) tapAngleNormalized += 360f // 0-360, 0 вправо

                                    var checkStart = 0f
                                    var currentRunningAngle = 0f // Угол, как он рисуется, но от 0 (вправо)
                                    // Для корректного сравнения, углы должны быть в одной системе отсчета
                                    // Давайте считать, что 0 градусов это вправо. Pie рисуется с -90 (вверх).
                                    // Временно для проверки, пусть 0 градусов atan2 совпадает с 0 градусов для секторов.
                                    // Это означает, что мы должны рисовать первый сектор от 0 градусов.
                                    // Или правильно преобразовать tapAngleNormalized к системе отсчета pie.

                                    // ---- Упрощенная логика проверки угла ----
                                    // (Эта часть может быть неточной и требовать отладки, работа с углами коварна)
                                    // Вместо сложной проверки угла, можно применить другой подход,
                                    // но попробуем этот.
                                    // Будем считать углы от -90 (как рисуем)
                                    var angleCursor = drawingStartAngleOffset
                                    for (idx in data.indices) {
                                        val itemSweep = 360f * (data[idx].totalAmount / totalAmountAllCategories).toFloat()
                                        val itemEndAngle = angleCursor + itemSweep

                                        // Нормализация tapAngle к системе отсчета pie (где 0 это -90)
                                        var tapAngleInPieSystem = angle // atan2_angle
                                        // if tapAngleInPieSystem > angleCursor && tapAngleInPieSystem < itemEndAngle (не сработает из-за пересечения 360/0)

                                        // Проверка, если точка между двумя векторами (начало и конец сектора)
                                        // Это требует более сложной геометрии, чем просто сравнение углов
                                        // Пока оставим простой перебор, но он может быть неточным.
                                        // Для надежности лучше использовать векторное произведение или готовые решения.

                                        // Условная проверка (может быть неточной на границах и при пересечении 0/360):
                                        // Приводим все углы к диапазону [0, 360), где 0 - это "вверх" (-90 у atan2)
                                        val tapAngleCorrected = (tapAngleNormalized + 90f) % 360f
                                        val sectorStartCorrected = (angleCursor + 90f + 360f) % 360f
                                        val sectorEndCorrected = (itemEndAngle + 90f + 360f) % 360f

                                        if (sectorStartCorrected <= sectorEndCorrected) { // Обычный сектор
                                            if (tapAngleCorrected >= sectorStartCorrected && tapAngleCorrected < sectorEndCorrected) {
                                                foundIndex = idx
                                                break
                                            }
                                        } else { // Сектор пересекает 0 (например, от 350 до 10 градусов)
                                            if (tapAngleCorrected >= sectorStartCorrected || tapAngleCorrected < sectorEndCorrected) {
                                                foundIndex = idx
                                                break
                                            }
                                        }
                                        angleCursor = itemEndAngle
                                    }
                                    // ---- Конец упрощенной логики ----

                                    selectedIndex = if (foundIndex != -1) {
                                        if (selectedIndex == foundIndex) null else foundIndex // Toggle selection
                                    } else {
                                        null
                                    }
                                } else {
                                    selectedIndex = null // Клик вне пирога
                                }
                            }
                        }
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val diameter = minOf(canvasWidth, canvasHeight) * 0.9f
                            val radius = diameter / 2
                            val topLeftX = (canvasWidth - diameter) / 2
                            val topLeftY = (canvasHeight - diameter) / 2

                            var startAngle = -90f

                            data.forEachIndexed { index, categoryData ->
                                val proportion = (categoryData.totalAmount / totalAmountAllCategories).toFloat()
                                val sweepAngle = 360f * proportion
                                // Определяем цвет: если этот сегмент выбран - обычный, иначе - приглушенный
                                val
                                        currentColor = chartColors[index % chartColors.size]
                                val finalColor = if (selectedIndex != null && selectedIndex != index) {
                                    currentColor.copy(alpha = 0.3f) // Приглушаем невыбранные
                                } else {
                                    currentColor // Обычный цвет для выбранного или если ничего не выбрано
                                }

                                drawArc(
                                    color = finalColor,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    topLeft = Offset(topLeftX, topLeftY),
                                    size = Size(diameter, diameter)
                                )
                                // Если сегмент выбран, можно нарисовать обводку
                                if (selectedIndex == index) {
                                    drawArc(
                                        color = Color.Black, // Цвет обводки
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true,
                                        topLeft = Offset(topLeftX, topLeftY),
                                        size = Size(diameter, diameter),
                                        style = Stroke(width = 3.dp.toPx()) // Стиль обводки
                                    )
                                }
                                startAngle += sweepAngle
                            }

                            // Отображение информации о выбранной категории в центре
                            if (selectedIndex != null && selectedCategoryInfo != null) {
                                // Не самый лучший способ отображать текст на Canvas, лучше через Text Composable,
                                // но для простоты примера можно попробовать так, либо вынести за Canvas.
                                // Этот код для Text на Canvas очень упрощен и не рекомендуется для сложного форматирования.
                                // Вместо этого, информацию лучше отображать в отдельном Text Composable ниже.
                                // Пока оставим эту идею и сосредоточимся на затемнении и легенде.
                            }
                        }
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

            Spacer(modifier = Modifier.height(24.dp)) // Увеличим отступ перед легендой

            // Легенда к диаграмме (уже была, но убедимся, что она подробна)
            Text("Расходы по категориям:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                data.forEachIndexed { index, categoryData ->
                    val itemColor = chartColors[index % chartColors.size]
                    val isSelected = selectedIndex == index
                    val itemAlpha = if (selectedIndex != null && !isSelected) 0.5f else 1.0f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp) // Немного увеличим отступ
                            .alpha(itemAlpha), // Применяем альфу и к легенде
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp) // Чуть больше
                                .background(itemColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp)) // Чуть больше
                        Text(
                            text = categoryData.categoryName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge, // Чуть крупнее
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = currencyFormatter.format(categoryData.totalAmount),
                            style = MaterialTheme.typography.bodyLarge, // Чуть крупнее
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Bold // Сумма всегда жирная или только у выделенной
                        )
                    }
                    if (index < data.size - 1) {
                        Divider(modifier = Modifier.padding(start = 28.dp, vertical = 4.dp)) // Отступ у разделителя
                    }
                }
            }
        }
    }



package com.example.mysavings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mysavings.ui.theme.generateColorFromString
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow



@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val totalSaved by viewModel.totalSaved.collectAsState()
    val savingsProjections by viewModel.savingsProjections.collectAsState()
    val wastesProjections by viewModel.wastesProjections.collectAsState()
    val savingsData by viewModel.savingsData.collectAsState()
    val wastesData by viewModel.wastesData.collectAsState()
    val heatmapData by viewModel.heatmapData.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    // State for the selected date in the heatmap
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // When the month changes, clear the selected date
    LaunchedEffect(currentMonth) {
        selectedDate = null
    }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeatmapCalendar(
                data = heatmapData,
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    // Toggle selection: if same date is clicked, deselect. Otherwise, select new date.
                    selectedDate = if (selectedDate == date) null else date
                },
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )

            // Animated card to show details for the selected date
            DailyDetailCard(
                visible = selectedDate != null,
                date = selectedDate,
                netAmount = selectedDate?.let { heatmapData[it] },
                currencyFormatter = currencyFormat
            )
        }

        // --- EXISTING ITEMS ---
        item {
            ProjectionCard(
                title = "Прогноз экономии",
                projections = savingsProjections,
                currencyFormatter = currencyFormat,
                accentColor = Color(0xFF4CAF50)
            )
        }
        item {
            ProjectionCard(
                title = "Прогноз лишних трат",
                projections = wastesProjections,
                currencyFormatter = currencyFormat,
                accentColor = Color(0xFFF44336)
            )
        }

        item {
            Divider()
            Text(
                "Общая статистика",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatisticRow("Всего сэкономлено (чистыми):", currencyFormat.format(totalSaved))
        }

        if (savingsData.isNotEmpty()) {
            item {
                CategoryPieChartWithLegend(
                    title = "Сэкономлено по категориям",
                    data = savingsData,
                    currencyFormatter = currencyFormat
                )
            }
        }

        if (wastesData.isNotEmpty()) {
            item {
                CategoryPieChartWithLegend(
                    title = "Лишние траты по категориям",
                    data = wastesData,
                    currencyFormatter = currencyFormat
                )
            }
        }
    }
}

// --- UPDATED HeatmapCalendar ---
@Composable
fun HeatmapCalendar(
    data: Map<LocalDate, Double>,
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val maxAbsValue = remember(data) { data.values.maxOfOrNull { abs(it) } ?: 1.0 }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 for Monday, 7 for Sunday
    val russianLocale = Locale("ru")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CalendarHeader(
                month = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, russianLocale).replaceFirstChar { it.uppercase() },
                year = currentMonth.year.toString(),
                onPrevious = onPreviousMonth,
                onNext = onNextMonth
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val weekdays = DayOfWeek.values()
                for (i in 0..6) {
                    Text(
                        text = weekdays[i].getDisplayName(TextStyle.SHORT, russianLocale),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val totalCells = (firstDayOfWeek - 1) + daysInMonth
            val numRows = (totalCells + 6) / 7

            for (row in 0 until numRows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (col in 0..6) {
                        val dayIndex = row * 7 + col
                        if (dayIndex >= firstDayOfWeek - 1 && dayIndex < totalCells) {
                            val dayOfMonth = dayIndex - (firstDayOfWeek - 1) + 1
                            val date = currentMonth.atDay(dayOfMonth)
                            val netAmount = data[date]

                            DayCell(
                                day = dayOfMonth.toString(),
                                netAmount = netAmount,
                                maxAbsValue = maxAbsValue,
                                isSelected = date == selectedDate,
                                onClick = { onDateSelected(date) }
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
                if (row < numRows - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

// --- NEW DailyDetailCard Composable ---
@Composable
fun DailyDetailCard(
    visible: Boolean,
    date: LocalDate?,
    netAmount: Double?,
    currencyFormatter: NumberFormat
) {
    val russianLocale = Locale("ru")
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", russianLocale) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 2 },
        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(animationSpec = tween(300)) { it / 2 }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date?.format(formatter) ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = currencyFormatter.format(netAmount ?: 0.0),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        netAmount == null || netAmount == 0.0 -> MaterialTheme.colorScheme.onSurface
                        netAmount > 0 -> Color(0xFF388E3C) // Darker Green
                        else -> Color(0xFFD32F2F) // Darker Red
                    }
                )
            }
        }
    }
}


// --- CalendarHeader (No Changes) ---
@Composable
private fun CalendarHeader(
    month: String,
    year: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = month, style = MaterialTheme.typography.titleLarge)
            Text(text = year, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
        }
    }
}

@Composable
private fun RowScope.DayCell(
    day: String,
    netAmount: Double?,
    maxAbsValue: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Определяем нашу палитру для тепловой карты
    val darkGreen = Color(0xFF1B5E20)
    val lightGreen = Color(0xFFA5D6A7)
    val darkRed = Color(0xFFB71C1C)
    val lightRed = Color(0xFFEF9A9A)
    val neutralColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val borderColor = MaterialTheme.colorScheme.primary

    val color = when {
        netAmount == null || netAmount == 0.0 -> neutralColor
        else -> {
            // Коэффициент интенсивности. Меньше 1.0f делает градиент более заметным для малых значений.
            // Можешь поэкспериментировать, изменив его на 0.4f или 0.6f.
            val intensityFactor = 0.5f

            // Нормализуем значение от 0.0 до 1.0
            val normalizedValue = (abs(netAmount) / maxAbsValue).toFloat()

            // Применяем нелинейное преобразование для лучшего визуального градиента
            val fraction = normalizedValue.pow(intensityFactor)

            if (netAmount > 0) {
                lerp(lightGreen, darkGreen, fraction)
            } else {
                lerp(lightRed, darkRed, fraction)
            }
        }
    }

    val textColor = when {
        netAmount == null || netAmount == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> Color.White // Белый текст для контраста на цветном фоне
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, borderColor, CircleShape) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ProjectionCard(
    title: String,
    projections: Projections,
    currencyFormatter: NumberFormat,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProjectionValue(
                    label = "Неделя",
                    value = currencyFormatter.format(projections.perWeek)
                )
                ProjectionValue(
                    label = "Месяц",
                    value = currencyFormatter.format(projections.perMonth)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProjectionValue(
                    label = "Полгода",
                    value = currencyFormatter.format(projections.perHalfYear)
                )
                ProjectionValue(
                    label = "Год",
                    value = currencyFormatter.format(projections.perYear)
                )
            }
        }
    }
}

@Composable
fun ProjectionValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
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

@Composable
fun CategoryPieChartWithLegend(
    title: String,
    data: List<CategorySavings>,
    currencyFormatter: NumberFormat
) {
    val totalAmountAbs = remember(data) { data.sumOf { abs(it.totalAmount) } }
    val chartColors = remember(data) { data.map { generateColorFromString(it.categoryName) } }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val selectedCategoryInfo = selectedIndex?.let { data.getOrNull(it) }

    Column {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (totalAmountAbs > 0) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(16.dp)
                        .pointerInput(data) {
                            detectTapGestures { tapOffset ->
                                val canvasWidth = size.width.toFloat()
                                val canvasHeight = size.height.toFloat()
                                val centerX = canvasWidth / 2
                                val centerY = canvasHeight / 2
                                val diameter = minOf(canvasWidth, canvasHeight) * 0.9f
                                val radius = diameter / 2
                                val dx = tapOffset.x - centerX
                                val dy = tapOffset.y - centerY

                                if (dx * dx + dy * dy <= radius * radius) {
                                    var tapAngleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    tapAngleDeg = (tapAngleDeg + 450f) % 360f

                                    var currentAngleProgress = 0f
                                    var foundIdx: Int? = null
                                    for (i in data.indices) {
                                        val proportion = (abs(data[i].totalAmount) / totalAmountAbs).toFloat()
                                        val sweep = 360f * proportion
                                        if (tapAngleDeg >= currentAngleProgress && tapAngleDeg < currentAngleProgress + sweep) {
                                            foundIdx = i
                                            break
                                        }
                                        currentAngleProgress += sweep
                                    }
                                    selectedIndex = if (foundIdx == selectedIndex) null else foundIdx
                                } else {
                                    selectedIndex = null
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val diameter = minOf(canvasWidth, canvasHeight) * 0.9f
                    val topLeft = Offset((canvasWidth - diameter) / 2, (canvasHeight - diameter) / 2)

                    var startAngleCanvas = -90f
                    data.forEachIndexed { index, categoryData ->
                        val proportion = (abs(categoryData.totalAmount) / totalAmountAbs).toFloat()
                        val sweepAngle = 360f * proportion
                        val currentColor = chartColors.getOrElse(index) { Color.Gray }
                        val finalColor = if (selectedIndex != null && selectedIndex != index) {
                            currentColor.copy(alpha = 0.3f)
                        } else {
                            currentColor
                        }
                        drawArc(color = finalColor, startAngle = startAngleCanvas, sweepAngle = sweepAngle, useCenter = true, topLeft = topLeft, size = Size(diameter, diameter))
                        if (selectedIndex == index) {
                            drawArc(color = Color.Black, startAngle = startAngleCanvas, sweepAngle = sweepAngle, useCenter = true, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = 2.dp.toPx()))
                        }
                        startAngleCanvas += sweepAngle
                    }
                }
            }
            if (selectedIndex != null && selectedCategoryInfo != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = selectedCategoryInfo.categoryName, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(text = currencyFormatter.format(selectedCategoryInfo.totalAmount), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        Box(modifier = Modifier.size(18.dp).background(itemColor))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = categoryData.categoryName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        Text(text = currencyFormatter.format(categoryData.totalAmount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                    if (index < data.size - 1) {
                        Divider(modifier = Modifier.padding(start = 28.dp).padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
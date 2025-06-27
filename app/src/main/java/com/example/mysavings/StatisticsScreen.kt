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
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.min
import kotlin.math.pow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.sp
import kotlin.math.max
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import java.time.*
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.unit.dp
import kotlin.math.max
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp

// In StatisticsScreen.kt

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val totalSaved by viewModel.totalSaved.collectAsState()
    val savingsProjections by viewModel.savingsProjections.collectAsState()
    val wastesProjections by viewModel.wastesProjections.collectAsState()
    val savingsData by viewModel.savingsData.collectAsState()
    val wastesData by viewModel.wastesData.collectAsState()
    val heatmapData by viewModel.heatmapData.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

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
        // --- NEW ORDER STARTS HERE ---

        // 1. Total Net Savings Card
        item {
            TotalNetSavingsCard(
                totalSaved = totalSaved,
                currencyFormatter = currencyFormat
            )
        }

        // 2. Projections Cards
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
            HeatmapCalendar(
                data = heatmapData,
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = if (selectedDate == date) null else date
                },
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )

            DailyDetailCard(
                visible = selectedDate != null,
                date = selectedDate,
                netAmount = selectedDate?.let { heatmapData[it] },
                currencyFormatter = currencyFormat
            )
        }

        // 4. Net Savings Line Chart
        item {
            val netSavingsData by viewModel.netSavingsOverTimeData.collectAsState()
            val selectedPeriod by viewModel.selectedTimePeriod.collectAsState()

            NetSavingsLineChartCard(
                data = netSavingsData,
                selectedPeriod = selectedPeriod,
                onPeriodSelect = { viewModel.setTimePeriod(it) },
                currencyFormatter = currencyFormat
            )
        }

        // 5. Savings vs Spending Bar Chart
        item {
            val histogramData by viewModel.savingsAndSpendingHistogramData.collectAsState()
            val histogramPeriod by viewModel.histogramPeriod.collectAsState()

            SavingsAndSpendingBarChartCard(
                data = histogramData,
                selectedPeriod = histogramPeriod,
                onPeriodSelect = { viewModel.setHistogramPeriod(it) },
                currencyFormatter = currencyFormat
            )
        }

        // 6. Savings by Category Pie Chart
        if (savingsData.isNotEmpty()) {
            item {
                CategoryPieChartWithLegend(
                    title = "Сэкономлено по категориям",
                    data = savingsData,
                    currencyFormatter = currencyFormat
                )
            }
        }

        // 7. Wastes by Category Pie Chart
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
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
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
    val darkGreen = Color(0xFF1B5E20)
    val lightGreen = Color(0xFFA5D6A7)
    val darkRed = Color(0xFFB71C1C)
    val lightRed = Color(0xFFEF9A9A)
    val neutralColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val borderColor = MaterialTheme.colorScheme.primary

    val color = when {
        netAmount == null || netAmount == 0.0 -> neutralColor
        else -> {
            val intensityFactor = 0.5f
            val normalizedValue = (abs(netAmount) / maxAbsValue).toFloat()
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
        else -> Color.White
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
        // Use the same background color as the calendar and other charts
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
fun TotalNetSavingsCard(totalSaved: Double, currencyFormatter: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Use the same background color as the calendar and other charts
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Всего сэкономлено (чистыми)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currencyFormatter.format(totalSaved),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NetSavingsLineChartCard(
    data: List<NetSavingsDataPoint>,
    selectedPeriod: TimePeriod,
    onPeriodSelect: (TimePeriod) -> Unit,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Net Savings Over Time",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TabRow(
                selectedTabIndex = selectedPeriod.ordinal,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                TimePeriod.values().forEach { period ->
                    Tab(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelect(period) },
                        text = { Text(period.name.lowercase().replaceFirstChar { it.titlecase() }) }
                    )
                }
            }

            if (data.isNotEmpty()) {
                LineChart(
                    data = data,
                    currencyFormatter = currencyFormatter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(top = 16.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data available for the selected period.")
                }
            }
        }
    }
}

@Composable
private fun LineChart(
    data: List<NetSavingsDataPoint>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier,
) {
    val (yMin, yMax) = remember(data) {
        val minVal = data.minOfOrNull { it.amount } ?: 0.0
        val maxVal = data.maxOfOrNull { it.amount } ?: 0.0
        Pair(min(minVal, 0.0), max(maxVal, 0.0))
    }

    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val pointColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val positiveLineColor = Color(0xFF4CAF50)
    val negativeLineColor = Color(0xFFF44336)

    val textPaint = remember {
        Paint().apply {
            textAlign = Paint.Align.RIGHT
            textSize = 30f
            color = 0
        }
    }

    Canvas(modifier = modifier) {
        val yAxisPadding = 100f
        val xAxisPadding = 50f
        val chartWidth = size.width - yAxisPadding
        val chartHeight = size.height - xAxisPadding

        drawYAxisWithGrid(yMin, yMax, currencyFormatter, yAxisPadding, chartHeight, gridColor, textPaint, textColor)
        drawXAxisLabels(data, yAxisPadding, chartHeight, textPaint, textColor)

        val yRange = (yMax - yMin).toFloat().coerceAtLeast(1f)
        val xStep = chartWidth / (data.size - 1).coerceAtLeast(1)

        val positivePath = Path()
        val negativePath = Path()
        var lastY: Float? = null

        data.forEachIndexed { i, point ->
            val x = yAxisPadding + i * xStep
            val y = chartHeight - ((point.amount - yMin) / yRange * chartHeight).toFloat()
            val currentLastY = lastY

            if (point.amount >= 0) {
                if (currentLastY != null && currentLastY < 0) {
                    val intersectX = findZeroCrossing(i, data) * xStep + yAxisPadding
                    negativePath.lineTo(intersectX, chartHeight - ((-yMin) / yRange * chartHeight).toFloat())
                    positivePath.moveTo(intersectX, chartHeight - ((-yMin) / yRange * chartHeight).toFloat())
                }
                if (i == 0) positivePath.moveTo(x, y) else positivePath.lineTo(x, y)
            } else {
                if (currentLastY != null && currentLastY >= 0) {
                    val intersectX = findZeroCrossing(i, data) * xStep + yAxisPadding
                    positivePath.lineTo(intersectX, chartHeight - ((-yMin) / yRange * chartHeight).toFloat())
                    negativePath.moveTo(intersectX, chartHeight - ((-yMin) / yRange * chartHeight).toFloat())
                }
                if (i == 0) negativePath.moveTo(x, y) else negativePath.lineTo(x, y)
            }
            lastY = point.amount.toFloat()
        }

        drawPath(positivePath, color = positiveLineColor, style = Stroke(width = 4.dp.toPx()))
        drawPath(negativePath, color = negativeLineColor, style = Stroke(width = 4.dp.toPx()))

        data.forEachIndexed { i, point ->
            val x = yAxisPadding + i * xStep
            val y = chartHeight - ((point.amount - yMin) / yRange * chartHeight).toFloat()
            drawCircle(color = pointColor, radius = 8f, center = Offset(x, y))
            drawCircle(color = surfaceColor, radius = 5f, center = Offset(x, y))
        }
    }
}

private fun findZeroCrossing(index: Int, data: List<NetSavingsDataPoint>): Float {
    if (index == 0) return 0f
    val p1 = data[index - 1]
    val p2 = data[index]
    if ((p1.amount * p2.amount) >= 0) return index.toFloat()

    val x1 = (index - 1).toFloat()
    val y1 = p1.amount.toFloat()
    val x2 = index.toFloat()
    val y2 = p2.amount.toFloat()

    return x1 - y1 * (x2 - x1) / (y2 - y1)
}


private fun DrawScope.drawYAxisWithGrid(
    yMin: Double, yMax: Double, currencyFormatter: NumberFormat,
    yAxisPadding: Float, chartHeight: Float, gridColor: Color,
    textPaint: Paint, textColor: Color
) {
    val numGridLines = 5
    val yRange = (yMax - yMin).coerceAtLeast(1.0)

    (0..numGridLines).forEach { i ->
        val value = yMin + (yRange * i / numGridLines)
        val y = chartHeight - (chartHeight * i / numGridLines)

        drawLine(
            color = gridColor,
            start = Offset(yAxisPadding, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )

        drawContext.canvas.nativeCanvas.drawText(
            currencyFormatter.format(value).replace(Regex("[^0-9,.\\-]"), ""),
            yAxisPadding - 15f,
            y + 5f,
            textPaint.apply { color = textColor.value.hashCode() }
        )
    }
}

private fun DrawScope.drawXAxisLabels(
    data: List<NetSavingsDataPoint>,
    yAxisPadding: Float, chartHeight: Float, textPaint: Paint, textColor: Color
) {
    val xStep = (size.width - yAxisPadding) / (data.size - 1).coerceAtLeast(1)

    val labelsToShow = data.filterIndexed { index, _ ->
        when {
            data.size <= 7 -> true
            data.size <= 15 -> index % 2 == 0
            else -> index % 4 == 0
        }
    }

    labelsToShow.forEach { point ->
        val index = data.indexOf(point)
        val x = yAxisPadding + index * xStep
        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.rotate(-45f, x, chartHeight + 10f)
        drawContext.canvas.nativeCanvas.drawText(
            point.label,
            x,
            chartHeight + 40f,
            textPaint.apply {
                color = textColor.value.hashCode()
                textAlign = Paint.Align.RIGHT
            }
        )
        drawContext.canvas.nativeCanvas.restore()
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

    // This Card wrapper provides the consistent background and padding
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                                        var tapAngleDeg =
                                            Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                                                .toFloat()
                                        tapAngleDeg = (tapAngleDeg + 450f) % 360f

                                        var currentAngleProgress = 0f
                                        var foundIdx: Int? = null
                                        for (i in data.indices) {
                                            val proportion =
                                                (abs(data[i].totalAmount) / totalAmountAbs).toFloat()
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
}

@Composable
fun SavingsAndSpendingBarChartCard(
    data: List<SavingsSpendingDataPoint>,
    selectedPeriod: HistogramPeriod,
    onPeriodSelect: (HistogramPeriod) -> Unit,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Savings vs Spending",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TabRow(
                selectedTabIndex = selectedPeriod.ordinal,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                HistogramPeriod.values().forEach { period ->
                    Tab(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelect(period) },
                        text = { Text(period.name.lowercase().replaceFirstChar { it.titlecase() }) }
                    )
                }
            }

            if (data.isNotEmpty()) {
                SavingsAndSpendingBarChart(
                    data = data,
                    currencyFormatter = currencyFormatter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(top = 16.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No savings or spending data for this period.")
                }
            }
        }
    }
}

private fun DrawScope.drawBarChartYAxis(
    maxAmount: Double,
    currencyFormatter: NumberFormat,
    yAxisPadding: Float,
    chartHeight: Float,
    gridColor: Color,
    textColor: Color
) {
    val textPaint = Paint().apply {
        textAlign = Paint.Align.RIGHT
        textSize = 12.sp.toPx()
        color = textColor.toArgb()
    }

    val numGridLines = 4
    val yZero = chartHeight / 2
    val availableHeight = chartHeight / 2

    drawLine(
        color = gridColor,
        start = Offset(yAxisPadding, yZero),
        end = Offset(size.width, yZero),
        strokeWidth = 2.dp.toPx()
    )
    drawContext.canvas.nativeCanvas.drawText(
        "0",
        yAxisPadding - 10f,
        yZero + 5f,
        textPaint
    )

    (1..numGridLines).forEach { i ->
        val value = maxAmount * i / numGridLines
        val label = currencyFormatter.format(value).replace(Regex("[^\\d,.-kmM]"), "")

        // Positive side (Savings)
        val yTop = yZero - (availableHeight * i / numGridLines)
        drawLine(
            color = gridColor,
            start = Offset(yAxisPadding, yTop),
            end = Offset(size.width, yTop)
        )
        drawContext.canvas.nativeCanvas.drawText(
            label,
            yAxisPadding - 10f,
            yTop + 5f,
            textPaint
        )

        val yBottom = yZero + (availableHeight * i / numGridLines)
        drawLine(
            color = gridColor,
            start = Offset(yAxisPadding, yBottom),
            end = Offset(size.width, yBottom)
        )
        drawContext.canvas.nativeCanvas.drawText(
            label,
            yAxisPadding - 10f,
            yBottom + 5f,
            textPaint
        )
    }
}

private fun DrawScope.drawBarChartXAxis(
    data: List<SavingsSpendingDataPoint>,
    yAxisPadding: Float,
    chartHeight: Float,
    totalBarWidth: Float,
    textColor: Color
) {
    val textPaint = Paint().apply {
        textAlign = Paint.Align.RIGHT
        textSize = 12.sp.toPx()
        color = textColor.toArgb()
    }

    data.forEachIndexed { i, point ->
        val x = yAxisPadding + (i * totalBarWidth) + (totalBarWidth / 2)

        if (point.savings > 0 || point.spending > 0) {
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(-60f, x, chartHeight + 10f)
            drawContext.canvas.nativeCanvas.drawText(
                point.label,
                x,
                chartHeight + 45f,
                textPaint
            )
            drawContext.canvas.nativeCanvas.restore()
        }
    }
}


@Composable
private fun SavingsAndSpendingBarChart(
    data: List<SavingsSpendingDataPoint>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    val maxAmount = remember(data) {
        val maxSavings = data.maxOfOrNull { it.savings } ?: 0.0
        val maxSpending = data.maxOfOrNull { it.spending } ?: 0.0
        max(maxSavings, maxSpending).coerceAtLeast(1.0)
    }

    val savingsColor = Color(0xFF4CAF50)
    val spendingColor = Color(0xFFF44336)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val scrollState = rememberScrollState()

    val density = LocalDensity.current

    val barWidthDp = 32.dp
    val yAxisPadding = 120f
    val xAxisPadding = 80f

    val barWidth: Float
    val totalBarWidth: Float
    val totalCanvasWidthDp: Dp

    with(density) {
        barWidth = barWidthDp.toPx()
        totalBarWidth = barWidth * 1.5f // bar width + spacing
        val totalCanvasWidthInPx = (data.size * totalBarWidth) + yAxisPadding
        totalCanvasWidthDp = totalCanvasWidthInPx.toDp()
    }

    Box(modifier = modifier.horizontalScroll(scrollState)) {
        Canvas(
            modifier = Modifier
                .width(totalCanvasWidthDp)
                .fillMaxHeight()
        ) {
            val chartHeight = size.height - xAxisPadding
            val yZero = chartHeight / 2
            val availableHeight = chartHeight / 2

            drawBarChartYAxis(
                maxAmount = maxAmount,
                currencyFormatter = currencyFormatter,
                yAxisPadding = yAxisPadding,
                chartHeight = chartHeight,
                gridColor = gridColor,
                textColor = textColor
            )

            drawBarChartXAxis(
                data = data,
                yAxisPadding = yAxisPadding,
                chartHeight = chartHeight,
                totalBarWidth = totalBarWidth,
                textColor = textColor
            )

            data.forEachIndexed { i, point ->
                if (point.savings == 0.0 && point.spending == 0.0) {
                    return@forEachIndexed
                }

                val x = yAxisPadding + (i * totalBarWidth) + (totalBarWidth - barWidth) / 2
                val savingsHeight = (point.savings / maxAmount * availableHeight).toFloat()
                val spendingHeight = (point.spending / maxAmount * availableHeight).toFloat()

                if (point.savings > 0) {
                    drawRect(
                        color = savingsColor,
                        topLeft = Offset(x, yZero - savingsHeight),
                        size = Size(barWidth, savingsHeight)
                    )
                }

                if (point.spending > 0) {
                    drawRect(
                        color = spendingColor,
                        topLeft = Offset(x, yZero),
                        size = Size(barWidth, spendingHeight)
                    )
                }
            }
        }
    }
}

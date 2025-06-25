package com.example.mysavings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.mysavings.ui.theme.generateColorFromString
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min


@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val totalSaved by viewModel.totalSaved.collectAsState()
    val savingsProjections by viewModel.savingsProjections.collectAsState()
    val wastesProjections by viewModel.wastesProjections.collectAsState()
    val savingsData by viewModel.savingsData.collectAsState()
    val wastesData by viewModel.wastesData.collectAsState()

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).alpha(itemAlpha),
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
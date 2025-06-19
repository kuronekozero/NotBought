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
import com.example.mysavings.ui.theme.generateDistinctColors
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min


@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val totalSaved by viewModel.totalSaved.collectAsState()
    val savedToday by viewModel.savedToday.collectAsState()
    val savedThisWeek by viewModel.savedThisWeek.collectAsState()
    val savedThisMonth by viewModel.savedThisMonth.collectAsState()
    val savedThisYear by viewModel.savedThisYear.collectAsState()

    val savingsData by viewModel.savingsData.collectAsState()
    val wastesData by viewModel.wastesData.collectAsState()

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Общая статистика", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            StatisticRow("Всего сэкономлено (чистыми):", currencyFormat.format(totalSaved))
            StatisticRow("Сегодня (чистыми):", currencyFormat.format(savedToday))
            StatisticRow("Эта неделя (чистыми):", currencyFormat.format(savedThisWeek))
            StatisticRow("Этот месяц (чистыми):", currencyFormat.format(savedThisMonth))
            StatisticRow("Этот год (чистыми):", currencyFormat.format(savedThisYear))
        }

        if (savingsData.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                CategoryPieChartWithLegend(
                    title = "Сэкономлено по категориям",
                    data = savingsData,
                    currencyFormatter = currencyFormat
                )
            }
        }

        if (wastesData.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
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
    val chartColors = remember(data.size) { generateDistinctColors(data.size) }
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
                        Divider(modifier = Modifier.padding(start = 28.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}
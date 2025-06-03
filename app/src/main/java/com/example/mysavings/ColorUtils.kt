package com.example.mysavings.ui.theme // Или другое подходящее место для утилит UI

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

fun generateDistinctColors(count: Int): List<Color> {
    val colors = mutableListOf<Color>()
    if (count <= 0) return colors

    // Базовые параметры для HSL
    val saturation = 0.7f // Насыщенность (0.0f до 1.0f)
    val lightness = 0.6f  // Светлота (0.0f до 1.0f) - не слишком темный, не слишком светлый

    // Золотой угол для генерации хорошо распределенных оттенков
    // Примерно 137.5 градусов. Это помогает получать визуально различимые цвета.
    val goldenRatioConjugate = 0.618033988749895f
    var hue = Math.random().toFloat() // Случайная начальная точка для оттенка (0.0f до 1.0f)

    for (i in 0 until count) {
        hue = (hue + goldenRatioConjugate) % 1.0f
        colors.add(Color.hsl(hue * 360, saturation, lightness))
    }
    return colors
}
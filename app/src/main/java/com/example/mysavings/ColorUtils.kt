package com.example.mysavings.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

fun generateColorFromString(input: String): Color {
    // Используем хэш-код строки для получения уникального числа
    val hashCode = input.hashCode()

    // Преобразуем хэш-код в значение оттенка (Hue) от 0 до 360
    val hue = (abs(hashCode) % 360).toFloat()

    // Задаем постоянные значения для насыщенности и светлоты для приятного вида
    val saturation = 0.7f
    val lightness = 0.6f

    return Color.hsl(hue, saturation, lightness)
}
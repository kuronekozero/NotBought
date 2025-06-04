package com.example.mysavings.ui.theme // Или другое подходящее место для утилит UI

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

fun generateDistinctColors(count: Int): List<Color> {
    val colors = mutableListOf<Color>()
    if (count <= 0) return colors

    val saturation = 0.7f
    val lightness = 0.6f
    val goldenRatioConjugate = 0.618033988749895f
    var hue = Math.random().toFloat()

    for (i in 0 until count) {
        hue = (hue + goldenRatioConjugate) % 1.0f
        colors.add(Color.hsl(hue * 360, saturation, lightness))
    }
    return colors
}


package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object StatisticsScreen : Screen("statistics_screen")
}
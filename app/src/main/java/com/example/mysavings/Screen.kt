package com.example.mysavings

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object AddEntryChooserScreen : Screen("add_entry_chooser_screen")
    object StatisticsScreen : Screen("statistics_screen")
    object SettingsScreen : Screen("settings_screen")
    object GoalsScreen : Screen("goals_screen")
    object AddGoalScreen : Screen("add_goal_screen")
}
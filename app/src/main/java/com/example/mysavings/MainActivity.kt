package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mysavings.ui.theme.MySavingsTheme
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults


class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val savingEntryDao by lazy { database.savingEntryDao() }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(savingEntryDao, userCategoryDao) // <<<--- Передаем userCategoryDao
    }

    private val statisticsViewModel: StatisticsViewModel by viewModels {
        StatisticsViewModelFactory(savingEntryDao)
    }

    private val userCategoryDao by lazy { database.userCategoryDao() } // Получаем DAO для категорий



    @OptIn(ExperimentalMaterial3Api::class) // Для Scaffold
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySavingsTheme {
                val navController = rememberNavController() // Контроллер навигации
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController = navController) }
                ) { innerPadding ->
                    AppNavigationHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding), // Важно для правильных отступов
                        mainViewModel = mainViewModel,
                        statisticsViewModel = statisticsViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    statisticsViewModel: StatisticsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route, // Начальный экран
        modifier = modifier
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(viewModel = mainViewModel)
        }
        composable(Screen.StatisticsScreen.route) {
            StatisticsScreen(viewModel = statisticsViewModel)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.MainScreen to Icons.Default.AddCircle, // Пример иконки
        Screen.StatisticsScreen to Icons.Default.List // Пример иконки
    )
    // Используем NavigationBar вместо BottomNavigation в Material 3
    NavigationBar {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        items.forEach { (screen, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = screen.route) },
                label = { Text(getLabelForScreen(screen.route)) }, // Функция для получения названия
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        // Избегаем создания нового экземпляра экрана, если он уже в стеке
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}


// Вспомогательная функция для получения названий для BottomBar
fun getLabelForScreen(route: String): String {
    return when (route) {
        Screen.MainScreen.route -> "Добавить"
        Screen.StatisticsScreen.route -> "Статистика"
        else -> ""
    }
}
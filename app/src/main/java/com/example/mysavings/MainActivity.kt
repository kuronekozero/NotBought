package com.example.mysavings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mysavings.ui.theme.MySavingsTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ShowChart
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.material.icons.outlined.History
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface


class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val savingEntryDao by lazy { database.savingEntryDao() }
    private val userCategoryDao by lazy { database.userCategoryDao() }
    private val settingsRepository by lazy { SettingsRepository(this) }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(settingsRepository, savingEntryDao, applicationContext)
    }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(savingEntryDao, userCategoryDao)
    }
    private val statisticsViewModel: StatisticsViewModel by viewModels {
        StatisticsViewModelFactory(savingEntryDao)
    }

    private val goalDao by lazy { database.goalDao() }

    private val goalsViewModel: GoalsViewModel by viewModels {
        GoalsViewModelFactory(goalDao, savingEntryDao)
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(savingEntryDao, userCategoryDao)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeOption by settingsRepository.themeOptionFlow.collectAsState(initial = ThemeOption.DARK)
            val useDarkTheme = when (themeOption) {
                ThemeOption.LIGHT -> false
                ThemeOption.DARK -> true
                ThemeOption.SYSTEM -> isSystemInDarkTheme()
            }

            MySavingsTheme(darkTheme = useDarkTheme) {
                // Используем null как начальное состояние, чтобы обозначить "загрузку"
                val hasSeenWelcome by settingsRepository.welcomeScreenSeenFlow.collectAsState(initial = null)

                // Ждем, пока hasSeenWelcome не получит реальное значение (true или false)
                when (hasSeenWelcome) {
                    true -> {
                        // Если пользователь уже видел экран, показываем основной интерфейс
                        AppShell(
                            mainViewModel = mainViewModel,
                            statisticsViewModel = statisticsViewModel,
                            settingsViewModel = settingsViewModel,
                            goalsViewModel = goalsViewModel,
                            historyViewModel = historyViewModel
                        )
                    }
                    false -> {
                        // Если пользователь новый, показываем приветствие
                        WelcomeScreen(onDismiss = { settingsViewModel.onWelcomeDismissed() })
                    }
                    null -> {
                        // Пока значение загружается, показываем пустой экран
                        // Это происходит так быстро, что пользователь ничего не заметит
                        Surface(modifier = Modifier.fillMaxSize()) { }
                    }
                }
        }
    }
}

@Composable
fun AppShell(
    mainViewModel: MainViewModel,
    statisticsViewModel: StatisticsViewModel,
    settingsViewModel: SettingsViewModel,
    goalsViewModel: GoalsViewModel,
    historyViewModel: HistoryViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                navController = navController,
                currentRoute = currentRoute,
                closeDrawer = { scope.launch { drawerState.close() } }

            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    currentRoute = currentRoute,
                    onNavigationIconClick = { scope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            AppNavigationHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                mainViewModel = mainViewModel,
                statisticsViewModel = statisticsViewModel,
                settingsViewModel = settingsViewModel,
                goalsViewModel = goalsViewModel,
                historyViewModel = historyViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(currentRoute: String?, onNavigationIconClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = getTitleForScreen(currentRoute))
        },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Меню навигации"
                )
            }
        }
    )
}

    @Composable
    fun AppDrawerContent(
        navController: NavController,
        currentRoute: String?,
        closeDrawer: () -> Unit
    ) {
        ModalDrawerSheet {
            Spacer(Modifier.height(12.dp))
            val menuItems = listOf(
                Screen.MainScreen,
                Screen.HistoryScreen,
                Screen.StatisticsScreen,
                Screen.GoalsScreen,
                Screen.SettingsScreen
            )
            menuItems.forEach { screen ->
                // --- Логика для правильного выбора и навигации ---
                val isSelected: Boolean
                val routeToNavigate: String

                if (screen is Screen.MainScreen) {
                    // Пункт "Добавить" считается активным и на экране выбора, и на экране добавления
                    isSelected = currentRoute == Screen.AddEntryChooserScreen.route || currentRoute?.startsWith(Screen.MainScreen.route) == true
                    routeToNavigate = Screen.AddEntryChooserScreen.route
                } else {
                    isSelected = currentRoute == screen.route
                    routeToNavigate = screen.route
                }

                NavigationDrawerItem(
                    icon = { Icon(getIconForScreen(screen.route), contentDescription = null) },
                    label = { Text(getLabelForScreen(screen.route)) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(routeToNavigate) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        closeDrawer()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }


    @Composable
    fun AppNavigationHost(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        mainViewModel: MainViewModel,
        statisticsViewModel: StatisticsViewModel,
        settingsViewModel: SettingsViewModel,
        goalsViewModel: GoalsViewModel,
        historyViewModel: HistoryViewModel
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.AddEntryChooserScreen.route,
            modifier = modifier
        ) {
            composable(Screen.AddEntryChooserScreen.route) {
                AddEntryChooserScreen(navController = navController)
            }
            composable(
                route = "${Screen.MainScreen.route}/{entryType}",
                arguments = listOf(navArgument("entryType") { type = NavType.StringType })
            ) { backStackEntry ->
                val entryType = backStackEntry.arguments?.getString("entryType")
                    ?.let { EntryType.valueOf(it) } ?: EntryType.SAVING
                MainScreen(
                    viewModel = mainViewModel,
                    entryType = entryType,
                    navController = navController
                )
            }
            composable(Screen.StatisticsScreen.route) {
                StatisticsScreen(viewModel = statisticsViewModel)
            }
            composable(Screen.SettingsScreen.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
            composable(Screen.GoalsScreen.route) {
                GoalsListScreen(
                    navController = navController,
                    viewModel = goalsViewModel
                )
            }
            composable(Screen.AddGoalScreen.route) {
                AddGoalScreen(
                    navController = navController,
                    viewModel = goalsViewModel
                )
            }
            composable(Screen.HistoryScreen.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    navController = navController
                )
            }
            composable(
                route = "${Screen.EditEntryScreen.route}/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.IntType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getInt("entryId")
                entryId?.let {
                    EditEntryScreen(
                        navController = navController,
                        viewModel = historyViewModel,
                        entryId = it
                    )
                }
            }
        }
    }

private fun getTitleForScreen(route: String?): String {
    return when (route) {
        Screen.MainScreen.route -> "Добавить"
        Screen.StatisticsScreen.route -> "Статистика"
        Screen.SettingsScreen.route -> "Настройки"
        Screen.GoalsScreen.route -> "Мои Цели"
        Screen.AddGoalScreen.route -> "Новая Цель"
        Screen.HistoryScreen.route -> "История"
        else -> "My Savings"
    }
}

private fun getLabelForScreen(route: String): String {
    return when (route) {
        Screen.MainScreen.route -> "Добавить"
        Screen.StatisticsScreen.route -> "Статистика"
        Screen.SettingsScreen.route -> "Настройки"
        Screen.GoalsScreen.route -> "Цели"
        Screen.HistoryScreen.route -> "История"
        else -> ""
    }
}

@Composable
private fun getIconForScreen(route: String): ImageVector {
    return when (route) {
        Screen.MainScreen.route -> Icons.Outlined.AddCircle
        Screen.StatisticsScreen.route -> Icons.Outlined.ShowChart
        Screen.SettingsScreen.route -> Icons.Outlined.Settings
        Screen.GoalsScreen.route -> Icons.Outlined.Flag
        Screen.HistoryScreen.route -> Icons.Outlined.History
        else -> Icons.Outlined.AddCircle
    }
}}
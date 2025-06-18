This file is a merged representation of a subset of the codebase, containing specifically included files, combined into a single document by Repomix.
The content has been processed where comments have been removed, empty lines have been removed.

# File Summary

## Purpose
This file contains a packed representation of the entire repository's contents.
It is designed to be easily consumable by AI systems for analysis, code review,
or other automated processes.

## File Format
The content is organized as follows:
1. This summary section
2. Repository information
3. Directory structure
4. Repository files (if enabled)
5. Multiple file entries, each consisting of:
  a. A header with the file path (## File: path/to/file)
  b. The full contents of the file in a code block

## Usage Guidelines
- This file should be treated as read-only. Any changes should be made to the
  original repository files, not this packed version.
- When processing this file, use the file path to distinguish
  between different files in the repository.
- Be aware that this file may contain sensitive information. Handle it with
  the same level of security as you would the original repository.

## Notes
- Some files may have been excluded based on .gitignore rules and Repomix's configuration
- Binary files are not included in this packed representation. Please refer to the Repository Structure section for a complete list of file paths, including binary files
- Only files matching these patterns are included: app/src/main/java/com/example/mysavings/**/*.kt
- Files matching patterns in .gitignore are excluded
- Files matching default ignore patterns are excluded
- Code comments have been removed from supported file types
- Empty lines have been removed from all files
- Files are sorted by Git change count (files with more changes are at the bottom)

# Directory Structure
```
app/src/main/java/com/example/mysavings/AddGoalScreen.kt
app/src/main/java/com/example/mysavings/AppDatabase.kt
app/src/main/java/com/example/mysavings/CategorySavings.kt
app/src/main/java/com/example/mysavings/ColorUtils.kt
app/src/main/java/com/example/mysavings/CsvHandler.kt
app/src/main/java/com/example/mysavings/Goal.kt
app/src/main/java/com/example/mysavings/GoalDao.kt
app/src/main/java/com/example/mysavings/GoalsListScreen.kt
app/src/main/java/com/example/mysavings/GoalsViewModel.kt
app/src/main/java/com/example/mysavings/GoalWithProgress.kt
app/src/main/java/com/example/mysavings/MainActivity.kt
app/src/main/java/com/example/mysavings/MainScreen.kt
app/src/main/java/com/example/mysavings/MainViewModel.kt
app/src/main/java/com/example/mysavings/SavingEntry.kt
app/src/main/java/com/example/mysavings/SavingEntryDao.kt
app/src/main/java/com/example/mysavings/Screen.kt
app/src/main/java/com/example/mysavings/SettingsRepository.kt
app/src/main/java/com/example/mysavings/SettingsScreen.kt
app/src/main/java/com/example/mysavings/SettingsViewModel.kt
app/src/main/java/com/example/mysavings/StatisticsScreen.kt
app/src/main/java/com/example/mysavings/StatisticsViewModel.kt
app/src/main/java/com/example/mysavings/ui/theme/Color.kt
app/src/main/java/com/example/mysavings/ui/theme/Theme.kt
app/src/main/java/com/example/mysavings/ui/theme/Type.kt
app/src/main/java/com/example/mysavings/UserCategory.kt
app/src/main/java/com/example/mysavings/UserCategoryDao.kt
```

# Files

## File: app/src/main/java/com/example/mysavings/CategorySavings.kt
```kotlin
package com.example.mysavings
data class CategorySavings(
    val categoryName: String,
    val totalAmount: Double
)
```

## File: app/src/main/java/com/example/mysavings/GoalWithProgress.kt
```kotlin
package com.example.mysavings
data class GoalWithProgress(
    val goal: Goal,
    val currentAmount: Double
)
```

## File: app/src/main/java/com/example/mysavings/SettingsRepository.kt
```kotlin
package com.example.mysavings
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
enum class ThemeOption {
    LIGHT, DARK, SYSTEM
}
class SettingsRepository(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private object PreferencesKeys {
        val THEME_OPTION = stringPreferencesKey("theme_option")
    }
    val themeOptionFlow: Flow<ThemeOption> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_OPTION] ?: ThemeOption.DARK.name
            ThemeOption.valueOf(themeName)
        }
    suspend fun saveThemeOption(themeOption: ThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_OPTION] = themeOption.name
        }
    }
}
```

## File: app/src/main/java/com/example/mysavings/ui/theme/Type.kt
```kotlin
package com.example.mysavings.ui.theme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
```

## File: app/src/main/java/com/example/mysavings/UserCategory.kt
```kotlin
package com.example.mysavings
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "user_categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class UserCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
```

## File: app/src/main/java/com/example/mysavings/UserCategoryDao.kt
```kotlin
package com.example.mysavings
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface UserCategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: UserCategory)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<UserCategory>)
    @Delete
    suspend fun delete(category: UserCategory)
    @Query("SELECT * FROM user_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<UserCategory>>
    @Query("SELECT COUNT(*) FROM user_categories")
    suspend fun getCategoryCount(): Int
}
```

## File: app/src/main/java/com/example/mysavings/AddGoalScreen.kt
```kotlin
package com.example.mysavings
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(navController: NavController, viewModel: GoalsViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isEditing = viewModel.goalToEdit != null
    LaunchedEffect(key1 = Unit) {
        viewModel.navigateBack.collect {
            if (it) {
                navController.popBackStack()
            }
        }
    }
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val initialDate = viewModel.customStartDate ?: LocalDate.now()
        calendar.set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                viewModel.onCustomStartDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Редактировать Цель" else "Новая Цель") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveGoal() }) {
                        Icon(Icons.Filled.Done, contentDescription = "Сохранить цель")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.goalName,
                onValueChange = viewModel::onGoalNameChange,
                label = { Text("Название цели") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = viewModel.goalDescription,
                onValueChange = viewModel::onGoalDescriptionChange,
                label = { Text("Описание (необязательно)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.targetAmount,
                onValueChange = viewModel::onTargetAmountChange,
                label = { Text("Сумма для накопления") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (!isEditing) {
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Учитывать прошлые сбережения?",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = viewModel.includePastSavings,
                        onCheckedChange = viewModel::onIncludePastSavingsChange
                    )
                }
                if (viewModel.includePastSavings) {
                    val formattedDate = viewModel.customStartDate?.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) ?: "Выберите дату"
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Начать учет с: $formattedDate")
                    }
                }
            }
        }
    }
}
```

## File: app/src/main/java/com/example/mysavings/ColorUtils.kt
```kotlin
package com.example.mysavings.ui.theme
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
```

## File: app/src/main/java/com/example/mysavings/CsvHandler.kt
```kotlin
package com.example.mysavings
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.LocalDateTime
object CsvHandler {
    private const val CSV_HEADER = "id,itemName,cost,category,date"
    fun writeCsv(outputStream: OutputStream, entries: List<SavingEntry>) {
        outputStream.bufferedWriter().use { writer ->
            writer.write(CSV_HEADER)
            writer.newLine()
            entries.forEach { entry ->
                val line = "${entry.id},\"${entry.itemName.replace("\"", "\"\"")}\",${entry.cost},\"${entry.category.replace("\"", "\"\"")}\",${entry.date}"
                writer.write(line)
                writer.newLine()
            }
        }
    }
    fun readCsv(inputStream: InputStream): List<SavingEntry> {
        val entries = mutableListOf<SavingEntry>()
        inputStream.bufferedReader().useLines { lines ->
            lines.drop(1)
                .forEach { line ->
                    try {
                        val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                        if (tokens.size >= 5) {
                            val entry = SavingEntry(
                                id = 0,
                                itemName = tokens[1].trim('"'),
                                cost = tokens[2].toDouble(),
                                category = tokens[3].trim('"'),
                                date = LocalDateTime.parse(tokens[4])
                            )
                            entries.add(entry)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
        return entries
    }
}
```

## File: app/src/main/java/com/example/mysavings/Goal.kt
```kotlin
package com.example.mysavings
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String?,
    val targetAmount: Double,
    val creationDate: LocalDateTime,
    val savingsStartDate: LocalDateTime
)
```

## File: app/src/main/java/com/example/mysavings/GoalDao.kt
```kotlin
package com.example.mysavings
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete
import androidx.room.Update
@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal)
    @Query("SELECT * FROM goals ORDER BY creationDate DESC")
    fun getAllGoals(): Flow<List<Goal>>
    @Update
    suspend fun update(goal: Goal)
    @Delete
    suspend fun delete(goal: Goal)
}
```

## File: app/src/main/java/com/example/mysavings/GoalsListScreen.kt
```kotlin
package com.example.mysavings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
@Composable
fun GoalsListScreen(navController: NavController, viewModel: GoalsViewModel) {
    val goalsState by viewModel.goalsState.collectAsState()
    val activeGoals = goalsState.activeGoals
    val completedGoals = goalsState.completedGoals
    LaunchedEffect(key1 = Unit) {
        viewModel.navigateToGoal.collect { goalId ->
            navController.navigate(Screen.AddGoalScreen.route)
        }
    }
    if (viewModel.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeletion() },
            title = { Text("Подтверждение") },
            text = { Text("Вы уверены, что хотите удалить эту цель?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeletion() }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeletion() }) { Text("Отмена") }
            }
        )
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddGoalClicked() }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить новую цель")
            }
        }
    ) { paddingValues ->
        if (activeGoals.isEmpty() && completedGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("У вас пока нет целей. Нажмите +, чтобы добавить первую!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeGoals.isNotEmpty()) {
                    item {
                        Text(
                            "Активные цели",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(activeGoals, key = { it.goal.id }) { goalItem ->
                        GoalCard(
                            goalWithProgress = goalItem,
                            onEditClicked = { viewModel.onEditGoalClicked(goalItem.goal) },
                            onDeleteClicked = { viewModel.onDeleteGoalClicked(goalItem.goal) }
                        )
                    }
                }
                if (completedGoals.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Завершенные цели",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(completedGoals, key = { it.goal.id }) { goalItem ->
                        GoalCard(
                            goalWithProgress = goalItem,
                            onEditClicked = { viewModel.onEditGoalClicked(goalItem.goal) },
                            onDeleteClicked = { viewModel.onDeleteGoalClicked(goalItem.goal) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun GoalCard(
    goalWithProgress: GoalWithProgress,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    val progress = (goalWithProgress.currentAmount / goalWithProgress.goal.targetAmount).toFloat().coerceIn(0f, 1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = goalWithProgress.goal.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Опции")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {
                                onEditClicked()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            onClick = {
                                onDeleteClicked()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null) }
                )
            }
        }
    }
}}}
```

## File: app/src/main/java/com/example/mysavings/GoalsViewModel.kt
```kotlin
package com.example.mysavings
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime
data class GoalsScreenState(
    val activeGoals: List<GoalWithProgress> = emptyList(),
    val completedGoals: List<GoalWithProgress> = emptyList()
)
class GoalsViewModel(
    private val goalDao: GoalDao,
    private val savingEntryDao: SavingEntryDao
) : ViewModel() {
    val goalsState: StateFlow<GoalsScreenState> =
        goalDao.getAllGoals()
            .combine(savingEntryDao.getAllEntries()) { goals, savings ->
                val goalsWithProgress = goals.map { goal ->
                    val relevantSavings = savings.filter { saving ->
                        !saving.date.isBefore(goal.savingsStartDate)
                    }
                    val currentAmount = relevantSavings.sumOf { it.cost }
                    GoalWithProgress(goal, currentAmount)
                }
                val (completed, active) = goalsWithProgress.partition {
                    it.currentAmount >= it.goal.targetAmount
                }
                GoalsScreenState(active, completed)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GoalsScreenState()
            )
    var goalName by mutableStateOf("")
    var goalDescription by mutableStateOf("")
    var targetAmount by mutableStateOf("")
    var includePastSavings by mutableStateOf(false)
    var customStartDate by mutableStateOf<LocalDate?>(null)
    private val _navigateBack = Channel<Boolean>()
    val navigateBack = _navigateBack.receiveAsFlow()
    var goalToEdit by mutableStateOf<Goal?>(null)
    var showDeleteConfirmation by mutableStateOf(false)
    private var goalToDelete: Goal? = null
    private val _navigateToGoal = Channel<Int?>()
    val navigateToGoal = _navigateToGoal.receiveAsFlow()
    fun onGoalNameChange(name: String) { goalName = name }
    fun onGoalDescriptionChange(description: String) { goalDescription = description }
    fun onTargetAmountChange(amount: String) { targetAmount = amount }
    fun onIncludePastSavingsChange(include: Boolean) {
        includePastSavings = include
        if (include && customStartDate == null) {
            customStartDate = LocalDate.now()
        }
    }
    fun onCustomStartDateChange(date: LocalDate) { customStartDate = date }
    fun saveGoal() {
        val amount = targetAmount.toDoubleOrNull()
        if (goalName.isBlank() || amount == null || amount <= 0) {
            return
        }
        val goal = goalToEdit
        if (goal != null) {
            val updatedGoal = goal.copy(
                name = goalName,
                description = goalDescription.ifBlank { null },
                targetAmount = amount
            )
            viewModelScope.launch {
                goalDao.update(updatedGoal)
                _navigateBack.send(true)
                resetForm()
            }
        } else {
            val creationDateTime = LocalDateTime.now()
            val savingsStartDateTime = if (includePastSavings) {
                customStartDate?.atStartOfDay() ?: creationDateTime
            } else {
                creationDateTime
            }
            val newGoal = Goal(
                name = goalName,
                description = goalDescription.ifBlank { null },
                targetAmount = amount,
                creationDate = creationDateTime,
                savingsStartDate = savingsStartDateTime
            )
            viewModelScope.launch {
                goalDao.insert(newGoal)
                _navigateBack.send(true)
                resetForm()
            }
        }
    }
    fun onEditGoalClicked(goal: Goal) {
        goalToEdit = goal
        goalName = goal.name
        goalDescription = goal.description ?: ""
        targetAmount = goal.targetAmount.toString()
        // При редактировании опции выбора даты пока скрыты
        includePastSavings = false
        customStartDate = null
        viewModelScope.launch {
            _navigateToGoal.send(goal.id)
        }
    }
    fun onAddGoalClicked() {
        resetForm()
        goalToEdit = null
        viewModelScope.launch {
            _navigateToGoal.send(null) // null означает создание новой цели
        }
    }
    fun onDeleteGoalClicked(goal: Goal) {
        goalToDelete = goal
        showDeleteConfirmation = true
    }
    fun confirmDeletion() {
        goalToDelete?.let {
            viewModelScope.launch {
                goalDao.delete(it)
                showDeleteConfirmation = false
                goalToDelete = null
            }
        }
    }
    fun cancelDeletion() {
        showDeleteConfirmation = false
        goalToDelete = null
    }
    private fun resetForm() {
        goalName = ""
        goalDescription = ""
        targetAmount = ""
        includePastSavings = false
        customStartDate = null
        goalToEdit = null // <<<--- Добавь сброс
    }
}
class GoalsViewModelFactory(
    private val goalDao: GoalDao,
    private val savingEntryDao: SavingEntryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalsViewModel(goalDao, savingEntryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

## File: app/src/main/java/com/example/mysavings/MainScreen.kt
```kotlin
package com.example.mysavings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var categoryExpanded by remember { mutableStateOf(false) }
    val categoriesList by viewModel.categories.collectAsState()
    val showDialog = viewModel.showCategoryDialog
    if (showDialog) {
        CategoryManagementDialog(
            viewModel = viewModel,
            categories = categoriesList,
            onDismiss = { viewModel.closeCategoryDialog() }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Добавить экономию", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = viewModel.itemName,
            onValueChange = { viewModel.onItemNameChange(it) },
            label = { Text("Название товара/услуги") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.itemCost,
            onValueChange = { viewModel.onItemCostChange(it) },
            label = { Text("Стоимость") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedCategoryName,
                        onValueChange = {},
                        label = { Text("Категория") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        if (categoriesList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Нет категорий. Добавьте категорию.") },
                                onClick = {
                                    categoryExpanded = false
                                    viewModel.openCategoryDialog()
                                }
                            )
                        } else {
                            categoriesList.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        viewModel.onCategoryChange(category)
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { viewModel.openCategoryDialog() }) {
                Icon(Icons.Filled.Edit, contentDescription = "Управление категориями")
            }
        }
        Button(
            onClick = { viewModel.saveSavingEntry() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementDialog(
    viewModel: MainViewModel,
    categories: List<UserCategory>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Управление категориями", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.newCategoryNameInput,
                        onValueChange = { viewModel.onNewCategoryNameInputChange(it) },
                        label = { Text("Новая категория") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.addUserCategory() }) {
                        Icon(Icons.Filled.Add, contentDescription = "Добавить категорию")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Существующие категории:", style = MaterialTheme.typography.titleMedium)
                if (categories.isEmpty()) {
                    Text("Пользовательских категорий нет.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(categories) { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category.name)
                                IconButton(onClick = { viewModel.deleteUserCategory(category) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Удалить категорию")
                                }
                            }
                            Divider()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onDismiss) {
                    Text("Закрыть")
                }
            }
        }
    }
}
```

## File: app/src/main/java/com/example/mysavings/SavingEntry.kt
```kotlin
package com.example.mysavings
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
@Entity(tableName = "saving_entries")
data class SavingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemName: String,
    val cost: Double,
    val category: String,
    val date: LocalDateTime = LocalDateTime.now()
)
```

## File: app/src/main/java/com/example/mysavings/SettingsScreen.kt
```kotlin
package com.example.mysavings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val currentTheme by viewModel.themeOption.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let { viewModel.exportDataToUri(it) }
        }
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.importDataFromUri(it) }
        }
    )
    LaunchedEffect(key1 = Unit) {
        viewModel.uiAction.collectLatest { action ->
            when (action) {
                is SettingsViewModel.UiAction.LaunchExport -> {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    exportLauncher.launch("mysavings_export_$timeStamp.csv")
                }
                is SettingsViewModel.UiAction.LaunchImport -> {
                    importLauncher.launch(arrayOf("text/csv"))
                }
            }
        }
    }
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.snackbarMessageShown()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Тема оформления", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            ThemeOptionRow(
                text = "Светлая",
                selected = currentTheme == ThemeOption.LIGHT,
                onClick = { viewModel.setThemeOption(ThemeOption.LIGHT) }
            )
            ThemeOptionRow(
                text = "Темная",
                selected = currentTheme == ThemeOption.DARK,
                onClick = { viewModel.setThemeOption(ThemeOption.DARK) }
            )
            ThemeOptionRow(
                text = "Как в системе",
                selected = currentTheme == ThemeOption.SYSTEM,
                onClick = { viewModel.setThemeOption(ThemeOption.SYSTEM) }
            )
            Divider(modifier = Modifier.padding(vertical = 24.dp))
            Text("Управление данными", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.onExportClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Экспортировать статистику")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.onImportClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Импортировать статистику")
            }
        }
    }
}
@Composable
private fun ThemeOptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
```

## File: app/src/main/java/com/example/mysavings/SettingsViewModel.kt
```kotlin
package com.example.mysavings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class SettingsViewModel(private val settingsRepository: SettingsRepository,
                        private val savingEntryDao: SavingEntryDao,
                        private val context: Context) : ViewModel() {
    val themeOption: StateFlow<ThemeOption> = settingsRepository.themeOptionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeOption.DARK
        )
    fun setThemeOption(option: ThemeOption) {
        viewModelScope.launch {
            settingsRepository.saveThemeOption(option)
        }
    }
    sealed class UiAction {
        object LaunchExport : UiAction()
        object LaunchImport : UiAction()
    }
    data class UiState(
        val snackbarMessage: String? = null
    )
    private val _uiAction = Channel<UiAction>()
    val uiAction = _uiAction.receiveAsFlow()
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    fun onExportClicked() {
        viewModelScope.launch {
            _uiAction.send(UiAction.LaunchExport)
        }
    }
    fun onImportClicked() {
        viewModelScope.launch {
            _uiAction.send(UiAction.LaunchImport)
        }
    }
    fun exportDataToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val entries = savingEntryDao.getAllEntriesList()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    CsvHandler.writeCsv(outputStream, entries)
                }
                _uiState.update { it.copy(snackbarMessage = "Экспорт завершен успешно") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Ошибка при экспорте") }
            }
        }
    }
    fun importDataFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val entries = CsvHandler.readCsv(inputStream)
                    savingEntryDao.insertAll(entries)
                }
                _uiState.update { it.copy(snackbarMessage = "Импорт завершен успешно") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Ошибка при импорте") }
            }
        }
    }
    fun snackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
class SettingsViewModelFactory(
    private val repository: SettingsRepository,
    private val savingEntryDao: SavingEntryDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, savingEntryDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

## File: app/src/main/java/com/example/mysavings/ui/theme/Color.kt
```kotlin
package com.example.mysavings.ui.theme
import androidx.compose.ui.graphics.Color
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkPrimary = Color(0xFF53A6A6)
val DarkOnPrimary = Color(0xFF003737)
val DarkOnSurface = Color(0xFFE6E1E5)
```

## File: app/src/main/java/com/example/mysavings/ui/theme/Theme.kt
```kotlin
package com.example.mysavings.ui.theme
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
)
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)
@Composable
fun MySavingsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

## File: app/src/main/java/com/example/mysavings/MainViewModel.kt
```kotlin
package com.example.mysavings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
class MainViewModel(
    private val savingEntryDao: SavingEntryDao,
    private val userCategoryDao: UserCategoryDao
) : ViewModel() {
    var itemName by mutableStateOf("")
    var itemCost by mutableStateOf("")
    // Список категорий из базы данных
    val categories: StateFlow<List<UserCategory>> = userCategoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // Выбранная категория (храним объект UserCategory или ее имя)
    // Для простоты будем хранить имя, т.к. оно уникально
    var selectedCategoryName by mutableStateOf("")
        private set // Чтобы изменять только через onCategoryChange
    // Состояние для диалога управления категориями
    var showCategoryDialog by mutableStateOf(false)
    var newCategoryNameInput by mutableStateOf("")
    init {
        // При инициализации ViewModel, если список категорий не пуст,
        // устанавливаем selectedCategoryName в первую из списка.
        // Это также поможет, если пользователь удалит текущую выбранную категорию.
        viewModelScope.launch {
            categories.collectLatest { categoryList ->
                if (categoryList.isNotEmpty()) {
                    // Если текущая selectedCategoryName невалидна или пуста, или не содержится в новом списке
                    if (selectedCategoryName.isBlank() || categoryList.none { it.name == selectedCategoryName }) {
                        selectedCategoryName = categoryList.first().name
                    }
                } else {
                    selectedCategoryName = "" // Если категорий нет, сбрасываем
                }
            }
        }
    }
    fun onItemNameChange(newName: String) { itemName = newName }
    fun onItemCostChange(newCost: String) { itemCost = newCost }
    fun onCategoryChange(newCategory: UserCategory) { // Теперь принимаем UserCategory
        selectedCategoryName = newCategory.name
    }
    // Если нужно менять по имени (например, из текстового поля, но у нас dropdown)
    // fun onCategoryNameChange(newCategoryName: String) { selectedCategoryName = newCategoryName }
    fun saveSavingEntry() {
        val cost = itemCost.toDoubleOrNull()
        if (itemName.isNotBlank() && cost != null && cost > 0 && selectedCategoryName.isNotBlank()) {
            val newEntry = SavingEntry(
                itemName = itemName,
                cost = cost,
                category = selectedCategoryName, // Сохраняем имя категории
                date = LocalDateTime.now()
            )
            viewModelScope.launch {
                savingEntryDao.insert(newEntry)
                itemName = ""
                itemCost = ""
                // selectedCategoryName остается (для удобства)
            }
        } else {
            println("Ошибка: Имя, стоимость и категория должны быть заполнены корректно.")
        }
    }
    // --- Функции для управления категориями ---
    fun openCategoryDialog() {
        newCategoryNameInput = "" // Сбрасываем поле ввода при открытии
        showCategoryDialog = true
    }
    fun closeCategoryDialog() {
        showCategoryDialog = false
    }
    fun onNewCategoryNameInputChange(name: String) {
        newCategoryNameInput = name
    }
    fun addUserCategory() {
        val name = newCategoryNameInput.trim()
        if (name.isNotBlank()) {
            viewModelScope.launch {
                userCategoryDao.insert(UserCategory(name = name))
                newCategoryNameInput = "" // Очищаем поле после добавления
                // Диалог можно не закрывать, чтобы пользователь мог добавить еще
            }
        }
    }
    fun deleteUserCategory(category: UserCategory) {
        viewModelScope.launch {
            userCategoryDao.delete(category)
            // Если удаленная категория была выбрана, нужно сбросить selectedCategoryName
            // Логика в init { categories.collectLatest ... } должна это обработать
        }
    }
}
// Обновленная Фабрика для MainViewModel
class MainViewModelFactory(
    private val savingEntryDao: SavingEntryDao,
    private val userCategoryDao: UserCategoryDao // <<<--- Добавляем DAO категорий
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(savingEntryDao, userCategoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

## File: app/src/main/java/com/example/mysavings/SavingEntryDao.kt
```kotlin
package com.example.mysavings
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
@Dao
interface SavingEntryDao {
    @Insert
    suspend fun insert(entry: SavingEntry)
    @Query("SELECT * FROM saving_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<SavingEntry>>
    @Query("SELECT SUM(cost) FROM saving_entries")
    fun getTotalSaved(): Flow<Double?>
    @Query("SELECT SUM(cost) FROM saving_entries WHERE date = :specificDate")
    fun getTotalSavedForDate(specificDate: LocalDate): Flow<Double?>
    @Query("SELECT SUM(cost) FROM saving_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalSavedBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Double?>
    @Query("SELECT category AS categoryName, SUM(cost) AS totalAmount FROM saving_entries GROUP BY category ORDER BY totalAmount DESC")
    fun getSavingsPerCategory(): Flow<List<CategorySavings>>
    @Query("SELECT * FROM saving_entries")
    suspend fun getAllEntriesList(): List<SavingEntry>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SavingEntry>)
}
```

## File: app/src/main/java/com/example/mysavings/Screen.kt
```kotlin
package com.example.mysavings
sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object StatisticsScreen : Screen("statistics_screen")
    object SettingsScreen : Screen("settings_screen")
    object GoalsScreen : Screen("goals_screen")
    object AddGoalScreen : Screen("add_goal_screen")
}
```

## File: app/src/main/java/com/example/mysavings/StatisticsScreen.kt
```kotlin
package com.example.mysavings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.mysavings.ui.theme.MySavingsTheme
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysavings.ui.theme.generateDistinctColors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
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
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val totalSaved by viewModel.totalSaved.collectAsState()
    val savedToday by viewModel.savedToday.collectAsState()
    val savedThisWeek by viewModel.savedThisWeek.collectAsState()
    val savedThisMonth by viewModel.savedThisMonth.collectAsState()
    val savedThisYear by viewModel.savedThisYear.collectAsState()
    val savingsByCategory by viewModel.savingsByCategory.collectAsState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Статистика", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }
        item { StatisticRow("Всего сэкономлено:", currencyFormat.format(totalSaved)) }
        item { StatisticRow("Сегодня:", currencyFormat.format(savedToday)) }
        item { StatisticRow("Эта неделя:", currencyFormat.format(savedThisWeek)) }
        item { StatisticRow("Этот месяц:", currencyFormat.format(savedThisMonth)) }
        item { StatisticRow("Этот год:", currencyFormat.format(savedThisYear)) }
        item {
            if (savingsByCategory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("По категориям", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                CategoryPieChartWithLegend(
                    data = savingsByCategory,
                    currencyFormatter = currencyFormat
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Нет данных по категориям для отображения.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    MySavingsTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Статистика (Превью)", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            StatisticRow("Всего сэкономлено:", "10 000 ₽")
            StatisticRow("Сегодня:", "150 ₽")
        }
    }
}
@Composable
fun CategoryPieChartWithLegend(
    data: List<CategorySavings>,
    currencyFormatter: NumberFormat
) {
    val totalAmountAllCategories = remember(data) { data.sumOf { it.totalAmount } }
    val chartColors = remember(data.size) { generateDistinctColors(data.size) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val selectedCategoryInfo = selectedIndex?.let { data.getOrNull(it) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (totalAmountAllCategories > 0 && data.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp)
                    .pointerInput(data, totalAmountAllCategories) {
                        detectTapGestures { tapOffset ->
                            if (totalAmountAllCategories <= 0) {
                                selectedIndex = null
                                return@detectTapGestures
                            }
                            val canvasWidth = size.width.toFloat()
                            val canvasHeight = size.height.toFloat()
                            val centerX = canvasWidth / 2
                            val centerY = canvasHeight / 2
                            val diameter = minOf(canvasWidth, canvasHeight) * 0.9f
                            val radius = diameter / 2
                            val dx = tapOffset.x - centerX
                            val dy = tapOffset.y - centerY
                            if (dx * dx + dy * dy <= radius * radius) {
                                var tapAngleRad = atan2(dy.toDouble(), dx.toDouble())
                                var tapAngleDeg = Math.toDegrees(tapAngleRad).toFloat()
                                tapAngleDeg = (tapAngleDeg + 450f) % 360f
                                var currentAngleProgress = 0f
                                var foundIdx: Int? = null
                                for (i in data.indices) {
                                    val proportion = (data[i].totalAmount / totalAmountAllCategories).toFloat()
                                    val sweep = 360f * proportion
                                    val nextAngleProgress = currentAngleProgress + sweep
                                    if (tapAngleDeg >= currentAngleProgress && tapAngleDeg < nextAngleProgress) {
                                        foundIdx = i
                                        break
                                    }
                                    currentAngleProgress = nextAngleProgress
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
                val topLeftX = (canvasWidth - diameter) / 2
                val topLeftY = (canvasHeight - diameter) / 2
                var startAngleCanvas = -90f
                data.forEachIndexed { index, categoryData ->
                    val proportion = (categoryData.totalAmount / totalAmountAllCategories).toFloat()
                    val sweepAngle = 360f * proportion
                    val currentColor = chartColors.getOrElse(index) { Color.Gray }
                    val finalColor = if (selectedIndex != null && selectedIndex != index) {
                        currentColor.copy(alpha = 0.3f)
                    } else {
                        currentColor
                    }
                    drawArc(
                        color = finalColor,
                        startAngle = startAngleCanvas,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(topLeftX, topLeftY),
                        size = Size(diameter, diameter)
                    )
                    if (selectedIndex == index) {
                        drawArc(
                            color = Color.Black,
                            startAngle = startAngleCanvas,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(topLeftX, topLeftY),
                            size = Size(diameter, diameter),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    startAngleCanvas += sweepAngle
                }
            }
        } else {
            Text("Нет данных для диаграммы", modifier = Modifier.padding(16.dp))
        }
        if (selectedIndex != null && selectedCategoryInfo != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = selectedCategoryInfo.categoryName,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = currencyFormatter.format(selectedCategoryInfo.totalAmount),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Расходы по категориям:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
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
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(itemColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = categoryData.categoryName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = currencyFormatter.format(categoryData.totalAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Bold
                    )
                }
                if (index < data.size - 1) {
                    Divider(modifier = Modifier.padding(start = 28.dp).padding(vertical = 4.dp))
                }
            }
        }
    }
}
```

## File: app/src/main/java/com/example/mysavings/StatisticsViewModel.kt
```kotlin
package com.example.mysavings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
class StatisticsViewModel(private val dao: SavingEntryDao) : ViewModel() {
    val totalSaved: StateFlow<Double> = dao.getTotalSaved()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    val savedToday: StateFlow<Double> = dao.getTotalSavedForDate(LocalDate.now())
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    val savedThisWeek: StateFlow<Double> = run {
        val today = LocalDate.now()
        val firstDayOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val lastDayOfWeek = firstDayOfWeek.plusDays(6)
        dao.getTotalSavedBetweenDates(firstDayOfWeek, lastDayOfWeek)
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    }
    val savedThisMonth: StateFlow<Double> = run {
        val today = LocalDate.now()
        val firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
        val lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
        dao.getTotalSavedBetweenDates(firstDayOfMonth, lastDayOfMonth)
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    }
    val savedThisYear: StateFlow<Double> = run {
        val today = LocalDate.now()
        val firstDayOfYear = today.with(TemporalAdjusters.firstDayOfYear())
        val lastDayOfYear = today.with(TemporalAdjusters.lastDayOfYear())
        dao.getTotalSavedBetweenDates(firstDayOfYear, lastDayOfYear)
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    }
    val savingsByCategory: StateFlow<List<CategorySavings>> = dao.getSavingsPerCategory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )
}
class StatisticsViewModelFactory(private val dao: SavingEntryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

## File: app/src/main/java/com/example/mysavings/AppDatabase.kt
```kotlin
package com.example.mysavings
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.TypeConverter
import java.time.LocalDateTime
class LocalDateTimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, java.time.ZoneOffset.UTC) }
    }
    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(java.time.ZoneOffset.UTC)
    }
}
class LocalDateConverter {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }
    @androidx.room.TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}
@Database(
    entities = [SavingEntry::class, UserCategory::class, Goal::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class, LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savingEntryDao(): SavingEntryDao
    abstract fun userCategoryDao(): UserCategoryDao
    abstract fun goalDao(): GoalDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_savings_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val categoryDao = database.userCategoryDao()
                                    if (categoryDao.getCategoryCount() == 0) {
                                        categoryDao.insertAll(DefaultCategories.getList())
                                    }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
object DefaultCategories {
    fun getList(): List<UserCategory> {
        return listOf(
            UserCategory(name = "Еда и напитки"),
            UserCategory(name = "Развлечения"),
            UserCategory(name = "Покупки (вещи)"),
            UserCategory(name = "Транспорт"),
            UserCategory(name = "Хобби"),
            UserCategory(name = "Здоровье"),
            UserCategory(name = "Дом"),
            UserCategory(name = "Образование"),
            UserCategory(name = "Другое")
        )
    }
}
```

## File: app/src/main/java/com/example/mysavings/MainActivity.kt
```kotlin
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
                AppShell(
                    mainViewModel = mainViewModel,
                    statisticsViewModel = statisticsViewModel,
                    settingsViewModel = settingsViewModel,
                    goalsViewModel = goalsViewModel
                )
            }
    }
}
@Composable
fun AppShell(
    mainViewModel: MainViewModel,
    statisticsViewModel: StatisticsViewModel,
    settingsViewModel: SettingsViewModel,
    goalsViewModel: GoalsViewModel
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
                goalsViewModel = goalsViewModel
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
            Screen.StatisticsScreen,
            Screen.GoalsScreen,
            Screen.SettingsScreen
        )
        menuItems.forEach { screen ->
            NavigationDrawerItem(
                icon = { Icon(getIconForScreen(screen.route), contentDescription = null) },
                label = { Text(getLabelForScreen(screen.route)) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
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
    goalsViewModel: GoalsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route,
        modifier = modifier
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(viewModel = mainViewModel)
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
    }
}
private fun getTitleForScreen(route: String?): String {
    return when (route) {
        Screen.MainScreen.route -> "Добавить"
        Screen.StatisticsScreen.route -> "Статистика"
        Screen.SettingsScreen.route -> "Настройки"
        Screen.GoalsScreen.route -> "Мои Цели"
        Screen.AddGoalScreen.route -> "Новая Цель"
        else -> "My Savings"
    }
}
private fun getLabelForScreen(route: String): String {
    return when (route) {
        Screen.MainScreen.route -> "Добавить"
        Screen.StatisticsScreen.route -> "Статистика"
        Screen.SettingsScreen.route -> "Настройки"
        Screen.GoalsScreen.route -> "Цели"
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
        else -> Icons.Outlined.AddCircle
    }
}}
```

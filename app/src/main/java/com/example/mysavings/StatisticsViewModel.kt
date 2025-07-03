package com.example.mysavings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlin.math.abs

// Enum for the Line Chart period
enum class TimePeriod {
    WEEK, MONTH, YEAR
}

// Data class for the Line Chart
data class NetSavingsDataPoint(
    val date: LocalDate,
    val amount: Double,
    val label: String
)

data class DualBarChartDataPoint(
    val date: LocalDate,
    val savings: Double,
    val spending: Double
)

data class Projections(
    val perWeek: Double = 0.0,
    val perMonth: Double = 0.0,
    val perHalfYear: Double = 0.0,
    val perYear: Double = 0.0
)

// Enum for the dual bar chart period selector
enum class DualBarChartPeriod {
    WEEK, MONTH, YEAR
}

class StatisticsViewModel(private val savingEntryDao: SavingEntryDao) : ViewModel() {

    private val allEntries: Flow<List<SavingEntry>> = savingEntryDao.getAllEntries()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    val totalSaved: StateFlow<Double> = allEntries
        .map { entries -> entries.sumOf { it.cost } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val savingsEntries = allEntries.map { it.filter { entry -> entry.cost > 0 } }
    private val wastesEntries = allEntries.map { it.filter { entry -> entry.cost < 0 } }

    val savingsData: StateFlow<List<CategorySavings>> = savingsEntries
        .map { entries ->
            entries.groupBy { it.category }
                .map { (category, entryList) ->
                    CategorySavings(category, entryList.sumOf { it.cost })
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wastesData: StateFlow<List<CategorySavings>> = wastesEntries
        .map { entries ->
            entries.groupBy { it.category }
                .map { (category, entryList) ->
                    CategorySavings(category, entryList.sumOf { it.cost })
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun calculateProjections(entries: List<SavingEntry>): Projections {
        if (entries.isEmpty()) return Projections(0.0, 0.0, 0.0, 0.0)
        val firstDay = entries.minOfOrNull { it.date.toLocalDate() } ?: LocalDate.now()
        val days = ChronoUnit.DAYS.between(firstDay, LocalDate.now()).coerceAtLeast(1)
        val total = entries.sumOf { it.cost }
        val perDay = total / days
        return Projections(
            perWeek = perDay * 7,
            perMonth = perDay * 30.4,
            perHalfYear = perDay * 182.5,
            perYear = perDay * 365
        )
    }

    val savingsProjections: StateFlow<Projections> = savingsEntries
        .map { calculateProjections(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Projections())

    val wastesProjections: StateFlow<Projections> = wastesEntries
        .map { calculateProjections(it).let { p -> p.copy(perWeek = abs(p.perWeek), perMonth = abs(p.perMonth), perHalfYear = abs(p.perHalfYear), perYear = abs(p.perYear)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Projections())

    // --- HEATMAP STATE ---
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }
    fun previousMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }

    val heatmapData: StateFlow<Map<LocalDate, Double>> = combine(allEntries, _currentMonth) { entries, month ->
        entries
            .filter { YearMonth.from(it.date) == month }
            .groupBy { it.date.toLocalDate() }
            .mapValues { it.value.sumOf { entry -> entry.cost } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- LINE CHART STATE ---
    private val _selectedTimePeriod = MutableStateFlow(TimePeriod.MONTH)
    val selectedTimePeriod: StateFlow<TimePeriod> = _selectedTimePeriod.asStateFlow()

    fun setTimePeriod(period: TimePeriod) { _selectedTimePeriod.value = period }

    val netSavingsOverTimeData: StateFlow<List<NetSavingsDataPoint>> = combine(allEntries, _selectedTimePeriod) { entries, period ->
        val now = LocalDate.now()
        val locale = Locale.getDefault()
        when (period) {
            TimePeriod.WEEK -> {
                val weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                (0..6).map { i ->
                    val day = weekStart.plusDays(i.toLong())
                    val amount = entries.filter { it.date.toLocalDate() == day }.sumOf { it.cost }
                    NetSavingsDataPoint(day, amount, day.format(DateTimeFormatter.ofPattern("E", locale)))
                }
            }
            TimePeriod.MONTH -> {
                val monthStart = now.withDayOfMonth(1)
                (0 until now.lengthOfMonth()).map { i ->
                    val day = monthStart.plusDays(i.toLong())
                    val amount = entries.filter { it.date.toLocalDate() == day }.sumOf { it.cost }
                    NetSavingsDataPoint(day, amount, day.dayOfMonth.toString())
                }
            }
            TimePeriod.YEAR -> {
                val yearStart = now.withDayOfYear(1)
                (0..11).map { i ->
                    val month = yearStart.plusMonths(i.toLong())
                    val amount = entries.filter { YearMonth.from(it.date) == YearMonth.from(month) }.sumOf { it.cost }
                    NetSavingsDataPoint(month, amount, month.format(DateTimeFormatter.ofPattern("MMM", locale)))
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DUAL BAR CHART STATE ---
    private val _dualBarChartPeriod = MutableStateFlow(DualBarChartPeriod.WEEK)
    val dualBarChartPeriod: StateFlow<DualBarChartPeriod> = _dualBarChartPeriod.asStateFlow()

    private val _currentDualBarChartDate = MutableStateFlow(LocalDate.now())
    val currentDualBarChartDate: StateFlow<LocalDate> = _currentDualBarChartDate.asStateFlow()

    fun setDualBarChartPeriod(period: DualBarChartPeriod) {
        _dualBarChartPeriod.value = period
        // Reset date to today when changing period to avoid confusion
        _currentDualBarChartDate.value = LocalDate.now()
    }

    fun navigateDualBarChart(forward: Boolean) {
        val currentPeriod = _dualBarChartPeriod.value
        val currentDate = _currentDualBarChartDate.value
        val newDate = when (currentPeriod) {
            DualBarChartPeriod.WEEK -> if (forward) currentDate.plusWeeks(1) else currentDate.minusWeeks(1)
            DualBarChartPeriod.MONTH -> if (forward) currentDate.plusMonths(1) else currentDate.minusMonths(1)
            DualBarChartPeriod.YEAR -> if (forward) currentDate.plusYears(1) else currentDate.minusYears(1)
        }
        _currentDualBarChartDate.value = newDate
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dualBarChartData: StateFlow<List<DualBarChartDataPoint>> = _dualBarChartPeriod
        .flatMapLatest { period ->
            combine(allEntries, _currentDualBarChartDate) { entries, refDate ->
                when (period) {
                    DualBarChartPeriod.WEEK -> {
                        // Always 7 days, Mon-Sun of the week containing refDate
                        val weekStart = refDate.with(DayOfWeek.MONDAY)
                        (0..6).map { i ->
                            val date = weekStart.plusDays(i.toLong())
                            val dayEntries = entries.filter { it.date.toLocalDate() == date }
                            val savings = dayEntries.filter { it.cost > 0 }.sumOf { it.cost }
                            val spending = abs(dayEntries.filter { it.cost < 0 }.sumOf { it.cost })
                            DualBarChartDataPoint(date, savings, spending)
                        }
                    }
                    DualBarChartPeriod.MONTH -> {
                        val yearMonth = YearMonth.from(refDate)
                        val numWeeks = yearMonth.atEndOfMonth().get(java.time.temporal.WeekFields.ISO.weekOfMonth())
                        (1..numWeeks).map { week ->
                            val start = yearMonth.atDay(1).plusWeeks((week - 1).toLong()).with(DayOfWeek.MONDAY)
                            val end = start.plusDays(6)
                            val weekEntries = entries.filter { it.date.toLocalDate() in start..end }
                            val savings = weekEntries.filter { it.cost > 0 }.sumOf { it.cost }
                            val spending = abs(weekEntries.filter { it.cost < 0 }.sumOf { it.cost })
                            DualBarChartDataPoint(start, savings, spending)
                        }
                    }
                    DualBarChartPeriod.YEAR -> {
                        val year = refDate.year
                        (1..12).map { month ->
                            val ym = YearMonth.of(year, month)
                            val monthEntries = entries.filter { YearMonth.from(it.date) == ym }
                            val savings = monthEntries.filter { it.cost > 0 }.sumOf { it.cost }
                            val spending = abs(monthEntries.filter { it.cost < 0 }.sumOf { it.cost })
                            DualBarChartDataPoint(ym.atDay(1), savings, spending)
                        }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Average daily savings and waste ---
    private val _averageDailySavings = MutableStateFlow(0.0)
    val averageDailySavings: StateFlow<Double> = _averageDailySavings.asStateFlow()

    private val _averageDailyWaste = MutableStateFlow(0.0)
    val averageDailyWaste: StateFlow<Double> = _averageDailyWaste.asStateFlow()

    private val _firstEntryDate = MutableStateFlow<LocalDate?>(null)
    val firstEntryDate: StateFlow<LocalDate?> = _firstEntryDate.asStateFlow()

    init {
        viewModelScope.launch {
            allEntries.collect { entries ->
                if (entries.isNotEmpty()) {
                    val firstDay = entries.minOf { it.date }.toLocalDate()
                    _firstEntryDate.value = firstDay
                    val days = ChronoUnit.DAYS.between(firstDay, LocalDate.now()).coerceAtLeast(1)

                    val totalSavings = entries.filter { it.cost > 0 }.sumOf { it.cost }
                    _averageDailySavings.value = totalSavings / days

                    val totalWaste = abs(entries.filter { it.cost < 0 }.sumOf { it.cost })
                    _averageDailyWaste.value = totalWaste / days
                }
            }
        }
    }
}

class StatisticsViewModelFactory(private val savingEntryDao: SavingEntryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(savingEntryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
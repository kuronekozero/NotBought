package com.example.mysavings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.abs
import java.time.DayOfWeek


data class SavingsSpendingDataPoint(
    val label: String,
    val savings: Double,
    val spending: Double // Note: This will be a positive value
)

// New enum to control the bar chart's period
enum class HistogramPeriod {
    WEEKLY, MONTHLY
}

// Enum for the existing Line Chart period
enum class TimePeriod {
    WEEK, MONTH, YEAR
}

// Data class for the existing Line Chart
data class NetSavingsDataPoint(
    val date: LocalDate,
    val amount: Double,
    val label: String
)

data class Projections(
    val perWeek: Double = 0.0,
    val perMonth: Double = 0.0,
    val perHalfYear: Double = 0.0,
    val perYear: Double = 0.0
)

// Add enum for new period selector
enum class DualBarChartPeriod {
    WEEK, MONTH, YEAR
}

class StatisticsViewModel(private val savingEntryDao: SavingEntryDao) : ViewModel() {

    private val allEntries: Flow<List<SavingEntry>> = savingEntryDao.getAllEntries()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    // --- EXISTING LOGIC ---

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

    private val _selectedTimePeriod = MutableStateFlow(TimePeriod.MONTH)
    val selectedTimePeriod: StateFlow<TimePeriod> = _selectedTimePeriod.asStateFlow()

    fun setTimePeriod(period: TimePeriod) { _selectedTimePeriod.value = period }

    val netSavingsOverTimeData: StateFlow<List<NetSavingsDataPoint>> = combine(allEntries, _selectedTimePeriod) { entries, period ->
        val now = LocalDate.now()
        when (period) {
            TimePeriod.WEEK -> {
                val weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                (0..6).map { i ->
                    val day = weekStart.plusDays(i.toLong())
                    val amount = entries.filter { it.date.toLocalDate() == day }.sumOf { it.cost }
                    NetSavingsDataPoint(day, amount, day.format(DateTimeFormatter.ofPattern("E", Locale("ru"))))
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
                    NetSavingsDataPoint(month, amount, month.format(DateTimeFormatter.ofPattern("MMM", Locale("ru"))))
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- NEW LOGIC FOR SAVINGS/SPENDING HISTOGRAM ---
    private val _histogramPeriod = MutableStateFlow(HistogramPeriod.MONTHLY)
    val histogramPeriod: StateFlow<HistogramPeriod> = _histogramPeriod.asStateFlow()

    val savingsAndSpendingHistogramData: StateFlow<List<SavingsSpendingDataPoint>> =
        combine(allEntries, _histogramPeriod) { entries, period ->
            processDataForHistogram(entries, period)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHistogramPeriod(period: HistogramPeriod) {
        _histogramPeriod.value = period
    }

    private fun processDataForHistogram(
        entries: List<SavingEntry>,
        period: HistogramPeriod
    ): List<SavingsSpendingDataPoint> {
        if (entries.isEmpty()) {
            return emptyList()
        }

        val russianLocale = Locale("ru")
        val now = LocalDate.now()
        val firstEntryDate = entries.minOfOrNull { it.date.toLocalDate() } ?: now

        return when (period) {
            HistogramPeriod.MONTHLY -> {
                val formatter = DateTimeFormatter.ofPattern("MMM yy", russianLocale)
                val startMonth = YearMonth.from(firstEntryDate)
                val endMonth = YearMonth.from(now)

                val dataMap = entries.groupBy { YearMonth.from(it.date) }
                    .mapValues { (_, monthEntries) ->
                        val savings = monthEntries.filter { it.cost > 0 }.sumOf { it.cost }
                        val spending = abs(monthEntries.filter { it.cost < 0 }.sumOf { it.cost })
                        Pair(savings, spending)
                    }

                val monthSequence = generateSequence(startMonth) { it.plusMonths(1) }
                    .takeWhile { it <= endMonth }

                monthSequence.map { yearMonth ->
                    val (savings, spending) = dataMap[yearMonth] ?: Pair(0.0, 0.0)
                    SavingsSpendingDataPoint(
                        label = yearMonth.format(formatter),
                        savings = savings,
                        spending = spending
                    )
                }.toList()
            }
            HistogramPeriod.WEEKLY -> {
                val formatter = DateTimeFormatter.ofPattern("dd MMM", russianLocale)
                val dayOfWeekAdjuster = TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)

                val startWeek = firstEntryDate.with(dayOfWeekAdjuster)
                val endWeek = now.with(dayOfWeekAdjuster)

                val dataMap = entries.groupBy { it.date.toLocalDate().with(dayOfWeekAdjuster) }
                    .mapValues { (_, weekEntries) ->
                        val savings = weekEntries.filter { it.cost > 0 }.sumOf { it.cost }
                        val spending = abs(weekEntries.filter { it.cost < 0 }.sumOf { it.cost })
                        Pair(savings, spending)
                    }

                val weekSequence = generateSequence(startWeek) { it.plusWeeks(1) }
                    .takeWhile { it <= endWeek }

                weekSequence.map { weekStart ->
                    val (savings, spending) = dataMap[weekStart] ?: Pair(0.0, 0.0)
                    SavingsSpendingDataPoint(
                        label = weekStart.format(formatter),
                        savings = savings,
                        spending = spending
                    )
                }.toList()
            }
        }
    }

    // --- DUAL BAR CHART PERIOD STATE ---
    private val _dualBarChartPeriod = MutableStateFlow(DualBarChartPeriod.WEEK)
    val dualBarChartPeriod: StateFlow<DualBarChartPeriod> = _dualBarChartPeriod.asStateFlow()

    fun setDualBarChartPeriod(period: DualBarChartPeriod) { _dualBarChartPeriod.value = period }

    // State for current week/month/year navigation
    private val _currentDualBarChartDate = MutableStateFlow(LocalDate.now())
    val currentDualBarChartDate: StateFlow<LocalDate> = _currentDualBarChartDate.asStateFlow()

    fun nextDualBarChartPeriod() {
        when (_dualBarChartPeriod.value) {
            DualBarChartPeriod.WEEK -> _currentDualBarChartDate.value = _currentDualBarChartDate.value.plusWeeks(1)
            DualBarChartPeriod.MONTH -> _currentDualBarChartDate.value = _currentDualBarChartDate.value.plusMonths(1)
            DualBarChartPeriod.YEAR -> _currentDualBarChartDate.value = _currentDualBarChartDate.value.plusYears(1)
        }
    }
    fun previousDualBarChartPeriod() {
        when (_dualBarChartPeriod.value) {
            DualBarChartPeriod.WEEK -> _currentDualBarChartDate.value = _currentDualBarChartDate.value.minusWeeks(1)
            DualBarChartPeriod.MONTH -> _currentDualBarChartDate.value = _currentDualBarChartDate.value.minusMonths(1)
            DualBarChartPeriod.YEAR -> _currentDualBarChartDate.value = _currentDualBarChartDate.value.minusYears(1)
        }
    }

    // Expose data for the dual bar chart
    val dualBarChartData: StateFlow<List<SavingsSpendingDataPoint>> = combine(
        allEntries, dualBarChartPeriod, currentDualBarChartDate
    ) { entries, period, refDate ->
        when (period) {
            DualBarChartPeriod.WEEK -> {
                // Always 7 days, Mon-Sun of the week containing refDate
                val weekStart = refDate.with(java.time.DayOfWeek.MONDAY)
                (0..6).map { i ->
                    val day = weekStart.plusDays(i.toLong())
                    val dayEntries = entries.filter { it.date.toLocalDate() == day }
                    val savings = dayEntries.filter { it.cost > 0 }.sumOf { it.cost }
                    val spending = dayEntries.filter { it.cost < 0 }.sumOf { it.cost }.let { kotlin.math.abs(it) }
                    SavingsSpendingDataPoint(
                        label = day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("ru")),
                        savings = savings,
                        spending = spending
                    )
                }
            }
            DualBarChartPeriod.MONTH -> {
                // Always 4-6 weeks in the month containing refDate
                val month = java.time.YearMonth.from(refDate)
                val firstDay = month.atDay(1)
                val lastDay = month.atEndOfMonth()
                val weeks = mutableListOf<Pair<LocalDate, LocalDate>>()
                var weekStart = firstDay.with(java.time.DayOfWeek.MONDAY)
                while (weekStart <= lastDay) {
                    val weekEnd = weekStart.plusDays(6).coerceAtMost(lastDay)
                    weeks.add(weekStart to weekEnd)
                    weekStart = weekStart.plusWeeks(1)
                }
                weeks.map { (start, end) ->
                    val weekEntries = entries.filter { it.date.toLocalDate() in start..end }
                    val savings = weekEntries.filter { it.cost > 0 }.sumOf { it.cost }
                    val spending = weekEntries.filter { it.cost < 0 }.sumOf { it.cost }.let { kotlin.math.abs(it) }
                    SavingsSpendingDataPoint(
                        label = "${start.dayOfMonth}-${end.dayOfMonth}",
                        savings = savings,
                        spending = spending
                    )
                }
            }
            DualBarChartPeriod.YEAR -> {
                // Always 12 months in the year containing refDate
                val year = refDate.year
                (1..12).map { monthNum ->
                    val ym = java.time.YearMonth.of(year, monthNum)
                    val monthEntries = entries.filter { java.time.YearMonth.from(it.date) == ym }
                    val savings = monthEntries.filter { it.cost > 0 }.sumOf { it.cost }
                    val spending = monthEntries.filter { it.cost < 0 }.sumOf { it.cost }.let { kotlin.math.abs(it) }
                    SavingsSpendingDataPoint(
                        label = ym.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("ru")),
                        savings = savings,
                        spending = spending
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class StatisticsViewModelFactory(
    private val savingEntryDao: SavingEntryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(savingEntryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
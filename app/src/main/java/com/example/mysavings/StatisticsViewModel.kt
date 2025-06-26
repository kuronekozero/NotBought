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

data class Projections(
    val perWeek: Double = 0.0,
    val perMonth: Double = 0.0,
    val perHalfYear: Double = 0.0,
    val perYear: Double = 0.0
)

class StatisticsViewModel(private val dao: SavingEntryDao) : ViewModel() {

    private val allEntriesFlow = dao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSaved: StateFlow<Double> = allEntriesFlow
        .map { entries -> entries.sumOf { it.cost } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    private val projectionsState: StateFlow<Pair<Projections, Projections>> = allEntriesFlow
        .map { entries ->
            val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
            val recentEntries = entries.filter { it.date >= thirtyDaysAgo }

            if (recentEntries.isEmpty()) {
                return@map Pair(Projections(), Projections())
            }

            val firstEntryDate = recentEntries.minOf { it.date }
            val daysSpan = ChronoUnit.DAYS.between(firstEntryDate, LocalDateTime.now()).coerceAtLeast(1).toDouble()

            val totalSavings = recentEntries.filter { it.cost > 0 }.sumOf { it.cost }
            val totalWastes = abs(recentEntries.filter { it.cost < 0 }.sumOf { it.cost })

            val avgDailySaving = totalSavings / daysSpan
            val avgDailyWaste = totalWastes / daysSpan

            val savingsProjection = Projections(
                perWeek = avgDailySaving * 7,
                perMonth = avgDailySaving * 30,
                perHalfYear = avgDailySaving * 182.5,
                perYear = avgDailySaving * 365
            )

            val wastesProjection = Projections(
                perWeek = avgDailyWaste * 7,
                perMonth = avgDailyWaste * 30,
                perHalfYear = avgDailyWaste * 182.5,
                perYear = avgDailyWaste * 365
            )

            Pair(savingsProjection, wastesProjection)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(Projections(), Projections()))

    val savingsProjections: StateFlow<Projections> = projectionsState
        .map { it.first }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Projections())

    val wastesProjections: StateFlow<Projections> = projectionsState
        .map { it.second }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Projections())

    private val allCategoryData: StateFlow<List<CategorySavings>> = dao.getSavingsPerCategory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savingsData: StateFlow<List<CategorySavings>> = allCategoryData
        .map { list -> list.filter { it.totalAmount > 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wastesData: StateFlow<List<CategorySavings>> = allCategoryData
        .map { list -> list.filter { it.totalAmount < 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val dateTimeRange = _currentMonth.map { month ->
        // The very beginning of the first day of the month
        val startDateTime = month.atDay(1).atStartOfDay()
        // The very end of the last day of the month
        val endDateTime = month.atEndOfMonth().atTime(LocalTime.MAX)
        startDateTime to endDateTime
    }

    val heatmapData: StateFlow<Map<LocalDate, Double>> = dateTimeRange
        .flatMapLatest { (start, end) ->
            // The call to the DAO is now correct.
            dao.getEntriesForDateRange(start, end)
        }
        .map { entries ->
            entries.groupBy { it.date.toLocalDate() }
                .mapValues { (_, dailyEntries) -> dailyEntries.sumOf { it.cost } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

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
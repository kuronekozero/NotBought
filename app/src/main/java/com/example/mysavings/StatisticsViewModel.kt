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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

class StatisticsViewModel(private val dao: SavingEntryDao) : ViewModel() {

    private val allEntriesFlow = dao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalSaved: StateFlow<Double> = allEntriesFlow
        .map { entries -> entries.sumOf { it.cost } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val savedToday: StateFlow<Double> = allEntriesFlow
        .map { entries ->
            val startOfDay = LocalDate.now().atStartOfDay()
            val endOfDay = LocalDateTime.now() // до текущего момента
            entries.filter { it.date >= startOfDay && it.date <= endOfDay }.sumOf { it.cost }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val savedThisWeek: StateFlow<Double> = allEntriesFlow
        .map { entries ->
            val today = LocalDate.now()
            val firstDayOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay()
            val lastDayOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX)
            entries.filter { it.date >= firstDayOfWeek && it.date <= lastDayOfWeek }.sumOf { it.cost }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val savedThisMonth: StateFlow<Double> = allEntriesFlow
        .map { entries ->
            val today = LocalDate.now()
            val firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
            val lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX)
            entries.filter { it.date >= firstDayOfMonth && it.date <= lastDayOfMonth }.sumOf { it.cost }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val savedThisYear: StateFlow<Double> = allEntriesFlow
        .map { entries ->
            val today = LocalDate.now()
            val firstDayOfYear = today.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay()
            val lastDayOfYear = today.with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX)
            entries.filter { it.date >= firstDayOfYear && it.date <= lastDayOfYear }.sumOf { it.cost }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)


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
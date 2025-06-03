package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

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
        .map { it ?: 0.0 } // Если null (нет записей), то 0.0
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val savedToday: StateFlow<Double> = dao.getTotalSavedForDate(LocalDate.now())
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val savedThisWeek: StateFlow<Double> = run {
        val today = LocalDate.now()
        // В Европе неделя часто начинается с понедельника
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
}

// Фабрика для StatisticsViewModel
class StatisticsViewModelFactory(private val dao: SavingEntryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
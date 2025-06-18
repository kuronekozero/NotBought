package com.example.mysavings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class GoalsViewModel(
    private val goalDao: GoalDao,
    private val savingEntryDao: SavingEntryDao
) : ViewModel() {

    val goalsWithProgress: StateFlow<List<GoalWithProgress>> =
        goalDao.getAllGoals()
            .combine(savingEntryDao.getAllEntries()) { goals, savings ->
                goals.map { goal ->
                    val relevantSavings = savings.filter { saving ->
                        !saving.date.isBefore(goal.savingsStartDate)
                    }
                    val currentAmount = relevantSavings.sumOf { it.cost }
                    GoalWithProgress(goal, currentAmount)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
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
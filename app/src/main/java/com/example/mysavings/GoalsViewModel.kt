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
                    val netAmount = relevantSavings.sumOf { it.cost }
                    val currentAmount = maxOf(0.0, netAmount)
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

    // --- НАЧАЛО НОВОГО КОДА для создания цели ---
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
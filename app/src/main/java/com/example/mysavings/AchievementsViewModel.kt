package com.example.mysavings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import java.time.LocalDate

/**
 * Categories of achievements we track.
 */
enum class AchievementCategory { SAVED, WASTED, STREAK }

/**
 * Immutable definition of an achievement target.
 */
data class AchievementDefinition(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    /**
     * Target value: for SAVED/WASTED it is an amount of money (absolute positive).
     * For STREAK it is number of consecutive days.
     */
    val target: Double,
    val category: AchievementCategory
)

/**
 * Runtime progress for a given achievement.
 */
data class AchievementProgress(
    val definition: AchievementDefinition,
    val current: Double,
    val achieved: Boolean
) {
    val progressFraction: Float get() =
        if (definition.target == 0.0) 1f else (current / definition.target).coerceIn(0.0, 1.0).toFloat()
}

/**
 * View-model that exposes achievements and their progress as a reactive flow.
 */
class AchievementsViewModel(
    private val savingEntryDao: SavingEntryDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val currencyRates: Map<String, Double> = mapOf(
        "USD" to 1.0,
        "RUB" to 90.0,
        "EUR" to 1.1, // approx 1 EUR = 1.1 USD
        "GBP" to 1.25,
        "JPY" to 0.0068,
        "CNY" to 0.14
    )

    // Static definitions of every achievement supported by the application.
    private val definitions: List<AchievementDefinition> = listOf(
        // Saved money achievements
        AchievementDefinition("saved_1", R.string.achievement_saved_1_name, R.string.achievement_saved_1_desc, 1.0, AchievementCategory.SAVED),
        AchievementDefinition("saved_10", R.string.achievement_saved_10_name, R.string.achievement_saved_10_desc, 10.0, AchievementCategory.SAVED),
        AchievementDefinition("saved_100", R.string.achievement_saved_100_name, R.string.achievement_saved_100_desc, 100.0, AchievementCategory.SAVED),
        AchievementDefinition("saved_1000", R.string.achievement_saved_1000_name, R.string.achievement_saved_1000_desc, 1_000.0, AchievementCategory.SAVED),
        AchievementDefinition("saved_10000", R.string.achievement_saved_10000_name, R.string.achievement_saved_10000_desc, 10_000.0, AchievementCategory.SAVED),
        AchievementDefinition("saved_100000", R.string.achievement_saved_100000_name, R.string.achievement_saved_100000_desc, 100_000.0, AchievementCategory.SAVED),
        AchievementDefinition("saved_1000000", R.string.achievement_saved_1000000_name, R.string.achievement_saved_1000000_desc, 1_000_000.0, AchievementCategory.SAVED),

        // Wasted money achievements
        AchievementDefinition("wasted_1", R.string.achievement_wasted_1_name, R.string.achievement_wasted_1_desc, 1.0, AchievementCategory.WASTED),
        AchievementDefinition("wasted_10", R.string.achievement_wasted_10_name, R.string.achievement_wasted_10_desc, 10.0, AchievementCategory.WASTED),
        AchievementDefinition("wasted_100", R.string.achievement_wasted_100_name, R.string.achievement_wasted_100_desc, 100.0, AchievementCategory.WASTED),
        AchievementDefinition("wasted_1000", R.string.achievement_wasted_1000_name, R.string.achievement_wasted_1000_desc, 1_000.0, AchievementCategory.WASTED),
        AchievementDefinition("wasted_10000", R.string.achievement_wasted_10000_name, R.string.achievement_wasted_10000_desc, 10_000.0, AchievementCategory.WASTED),
        AchievementDefinition("wasted_100000", R.string.achievement_wasted_100000_name, R.string.achievement_wasted_100000_desc, 100_000.0, AchievementCategory.WASTED),
        AchievementDefinition("wasted_1000000", R.string.achievement_wasted_1000000_name, R.string.achievement_wasted_1000000_desc, 1_000_000.0, AchievementCategory.WASTED),

        // Streak achievements
        AchievementDefinition("streak_7", R.string.achievement_streak_7_name, R.string.achievement_streak_7_desc, 7.0, AchievementCategory.STREAK),
        AchievementDefinition("streak_15", R.string.achievement_streak_15_name, R.string.achievement_streak_15_desc, 15.0, AchievementCategory.STREAK),
        AchievementDefinition("streak_30", R.string.achievement_streak_30_name, R.string.achievement_streak_30_desc, 30.0, AchievementCategory.STREAK),
        AchievementDefinition("streak_100", R.string.achievement_streak_100_name, R.string.achievement_streak_100_desc, 100.0, AchievementCategory.STREAK),
        AchievementDefinition("streak_365", R.string.achievement_streak_365_name, R.string.achievement_streak_365_desc, 365.0, AchievementCategory.STREAK),
        AchievementDefinition("streak_500", R.string.achievement_streak_500_name, R.string.achievement_streak_500_desc, 500.0, AchievementCategory.STREAK),
        AchievementDefinition("streak_1000", R.string.achievement_streak_1000_name, R.string.achievement_streak_1000_desc, 1000.0, AchievementCategory.STREAK)
    )

    /**
     * Public stream of achievements with live progress.
     */
    val achievements: StateFlow<List<AchievementProgress>> = combine(
        savingEntryDao.getAllEntries(),
        settingsRepository.currencyCodeFlow
    ) { entries, currencyCode ->
        val rate = currencyRates[currencyCode] ?: 1.0

        val entriesInUsd = entries.map { it.copy(cost = it.cost / rate) }

        val totalSaved = entriesInUsd.filter { it.cost > 0 }.sumOf { it.cost }
        val totalWasted = entriesInUsd.filter { it.cost < 0 }.sumOf { -it.cost }
        val currentStreak = calculateCurrentStreak(entries)

        definitions.map { def ->
            val current = when (def.category) {
                AchievementCategory.SAVED -> totalSaved
                AchievementCategory.WASTED -> totalWasted
                AchievementCategory.STREAK -> currentStreak.toDouble()
            }
            AchievementProgress(definition = def, current = current, achieved = current >= def.target)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun calculateCurrentStreak(entries: List<SavingEntry>): Int {
        if (entries.isEmpty()) return 0
        val datesSet = entries.map { it.date.toLocalDate() }.toSet()
        var streak = 0
        var cursor = LocalDate.now()
        while (datesSet.contains(cursor)) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}

class AchievementsViewModelFactory(
    private val savingEntryDao: SavingEntryDao,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AchievementsViewModel(savingEntryDao, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
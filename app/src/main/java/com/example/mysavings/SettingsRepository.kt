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
            // По умолчанию - темная тема, как ты и просил
            val themeName = preferences[PreferencesKeys.THEME_OPTION] ?: ThemeOption.DARK.name
            ThemeOption.valueOf(themeName)
        }

    suspend fun saveThemeOption(themeOption: ThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_OPTION] = themeOption.name
        }
    }
}
package com.example.mysavings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private object PreferencesKeys {
        val WELCOME_SEEN = booleanPreferencesKey("welcome_seen_flag")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    val welcomeScreenSeenFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.WELCOME_SEEN] ?: false
        }

    val languageCodeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // Default to Russian if no language is set
            preferences[PreferencesKeys.LANGUAGE_CODE] ?: "ru"
        }

    suspend fun setWelcomeScreenSeen() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WELCOME_SEEN] = true
        }
    }

    suspend fun setLanguageCode(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = language
        }
    }
}
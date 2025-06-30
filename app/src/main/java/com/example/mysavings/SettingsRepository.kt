package com.example.mysavings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private object PreferencesKeys {
        val WELCOME_SEEN = booleanPreferencesKey("welcome_seen_flag")
    }

    val welcomeScreenSeenFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.WELCOME_SEEN] ?: false
        }

    suspend fun setWelcomeScreenSeen() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WELCOME_SEEN] = true
        }
    }
}
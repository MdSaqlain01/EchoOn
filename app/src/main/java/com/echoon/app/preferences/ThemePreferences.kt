package com.echoon.app.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

class ThemePreferences(private val context: Context) {

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }
}

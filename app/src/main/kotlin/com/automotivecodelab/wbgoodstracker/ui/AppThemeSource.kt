package com.automotivecodelab.wbgoodstracker.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class AppTheme {
    LIGHT, DARK, AUTO;
}

class AppThemeSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val THEME_KEY = stringPreferencesKey("theme")

    suspend fun getAppTheme(): AppTheme = dataStore.data
        .map { prefs ->
            val themeName = prefs[THEME_KEY] ?: AppTheme.AUTO.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: Exception) {
                AppTheme.AUTO
            }
        }
        .first()

    suspend fun saveAndSetupAppTheme(theme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme.name
        }
        val mode = when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

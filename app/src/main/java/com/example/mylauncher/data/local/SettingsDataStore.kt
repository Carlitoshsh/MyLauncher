package com.example.mylauncher.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {

    private val dataStore = context.settingsDataStore

    private object PreferencesKeys {
        val USE_24_HOUR_FORMAT = booleanPreferencesKey("use_24_hour_format")
    }

    val use24HourFormat: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_24_HOUR_FORMAT] ?: false
        }

    suspend fun setUse24HourFormat(use24Hour: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_24_HOUR_FORMAT] = use24Hour
        }
    }
}

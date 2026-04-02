package com.example.mylauncher.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shortcuts")

class ShortcutsDataStore(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val SHORTCUTS = stringSetPreferencesKey("shortcuts")
    }

    val shortcuts: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHORTCUTS] ?: emptySet()
        }

    suspend fun saveShortcuts(shortcuts: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHORTCUTS] = shortcuts
        }
    }
}

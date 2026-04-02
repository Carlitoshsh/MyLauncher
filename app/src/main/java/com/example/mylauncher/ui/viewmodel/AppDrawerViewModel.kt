package com.example.mylauncher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylauncher.data.AppDataSource
import com.example.mylauncher.data.local.ShortcutsDataStore
import com.example.mylauncher.domain.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppDrawerViewModel(
    private val appDataSource: AppDataSource,
    private val shortcutsDataStore: ShortcutsDataStore
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            shortcutsDataStore.shortcuts.collect { shortcuts ->
                val allApps = appDataSource.getInstalledApps(shortcuts)
                _apps.value = allApps
            }
        }
    }

    fun toggleShortcut(packageName: String) {
        viewModelScope.launch {
            val shortcuts = shortcutsDataStore.shortcuts.first()
            val newShortcuts = if (shortcuts.contains(packageName)) {
                shortcuts - packageName
            } else {
                if (shortcuts.size < 5) {
                    shortcuts + packageName
                } else {
                    // Optional: Show a message to the user that they can only have 5 shortcuts
                    shortcuts
                }
            }
            shortcutsDataStore.saveShortcuts(newShortcuts)
        }
    }
}

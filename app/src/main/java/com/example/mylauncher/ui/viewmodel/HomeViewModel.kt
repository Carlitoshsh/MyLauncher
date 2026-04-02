package com.example.mylauncher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylauncher.data.AppDataSource
import com.example.mylauncher.data.local.ShortcutsDataStore
import com.example.mylauncher.domain.AppInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val appDataSource: AppDataSource,
    private val shortcutsDataStore: ShortcutsDataStore
) : ViewModel() {

    val shortcuts: StateFlow<List<AppInfo>> = shortcutsDataStore.shortcuts
        .map { shortcuts ->
            appDataSource.getInstalledApps(shortcuts)
                .filter { it.isShortcut }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

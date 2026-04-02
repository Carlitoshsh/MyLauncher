package com.example.mylauncher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mylauncher.data.AppDataSource
import com.example.mylauncher.data.local.ShortcutsDataStore

class HomeViewModelFactory(
    private val appDataSource: AppDataSource,
    private val shortcutsDataStore: ShortcutsDataStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(appDataSource, shortcutsDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

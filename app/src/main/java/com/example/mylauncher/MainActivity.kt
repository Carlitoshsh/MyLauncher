package com.example.mylauncher

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.example.mylauncher.data.AppDataSource
import com.example.mylauncher.data.local.ShortcutsDataStore
import com.example.mylauncher.data.local.SettingsDataStore
import com.example.mylauncher.ui.screens.AppDrawerScreen
import com.example.mylauncher.ui.screens.HomeScreen
import com.example.mylauncher.ui.screens.SettingsScreen
import com.example.mylauncher.ui.theme.MyLauncherTheme
import com.example.mylauncher.ui.viewmodel.AppDrawerViewModel
import com.example.mylauncher.ui.viewmodel.AppDrawerViewModelFactory
import com.example.mylauncher.ui.viewmodel.HomeViewModel
import com.example.mylauncher.ui.viewmodel.HomeViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appDataSource = AppDataSource(applicationContext)
        val shortcutsDataStore = ShortcutsDataStore(applicationContext)
        val settingsDataStore = SettingsDataStore(applicationContext)
        val appDrawerViewModelFactory = AppDrawerViewModelFactory(appDataSource, shortcutsDataStore)
        val appDrawerViewModel = ViewModelProvider(this, appDrawerViewModelFactory).get(AppDrawerViewModel::class.java)
        val homeViewModelFactory = HomeViewModelFactory(appDataSource, shortcutsDataStore)
        val homeViewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)

        setContent {
            MyLauncherTheme {
                val pagerState = rememberPagerState(initialPage = 1) { 3 }
                val coroutineScope = rememberCoroutineScope()
                var isEditMode by remember { mutableStateOf(false) }

                val permissionState = rememberPermissionState(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                )

                LaunchedEffect(Unit) {
                    permissionState.launchPermissionRequest()
                }

                val launchApp: (String) -> Unit = { packageName ->
                    val intent: Intent? = packageManager.getLaunchIntentForPackage(packageName)
                    if (intent != null) {
                        startActivity(intent)
                    }
                }

                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> SettingsScreen(
                            onBack = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                            onEditShortcuts = {
                                isEditMode = true
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(2)
                                }
                            },
                            settingsDataStore = settingsDataStore
                        )
                        1 -> HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToAppDrawer = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(2)
                                }
                            },
                            onNavigateToSettings = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                            onAppClick = launchApp,
                            permissionState = permissionState,
                            settingsDataStore = settingsDataStore
                        )
                        2 -> AppDrawerScreen(
                            viewModel = appDrawerViewModel,
                            onBack = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                                isEditMode = false
                            },
                            onAppClick = launchApp,
                            isEditMode = isEditMode,
                            permissionState = permissionState
                        )
                    }
                }
            }
        }
    }
}

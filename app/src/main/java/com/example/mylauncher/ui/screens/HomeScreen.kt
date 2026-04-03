package com.example.mylauncher.ui.screens

import android.app.WallpaperManager
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import android.content.pm.LauncherApps
import androidx.compose.ui.platform.LocalContext
import android.os.Process
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
// pointerInput removed; not used after switching to buttons
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.mylauncher.domain.AppInfo
import com.example.mylauncher.ui.components.Clock
import com.example.mylauncher.ui.viewmodel.HomeViewModel
import com.example.mylauncher.data.local.SettingsDataStore
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted

import android.graphics.drawable.Drawable


@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAppDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAppClick: (String) -> Unit,
    permissionState: PermissionState,
    settingsDataStore: SettingsDataStore
) {
    val shortcuts by viewModel.shortcuts.collectAsState()
    val context = LocalContext.current
    val use24HourFormat by settingsDataStore.use24HourFormat.collectAsState(initial = false)

    val wallpaper: Drawable? = if (permissionState.status.isGranted) {
        remember {
            WallpaperManager.getInstance(context).peekDrawable()
        }
    } else {
        null
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (wallpaper != null) {
            Image(
                bitmap = wallpaper.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Clock(use24HourFormat = use24HourFormat)
            Spacer(modifier = Modifier.height(32.dp))

            if (shortcuts.isEmpty()) {
                Text("No shortcuts selected. Tap below to add some!", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToAppDrawer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.8f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Add Shortcuts")
                }
            } else {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Show up to 5 items in a single centered row; empty slots are placeholders
                        for (i in 0 until 5) {
                            val app = shortcuts.getOrNull(i)
                            var showOptionsForItem by remember { mutableStateOf(false) }
                            var optionsForItem by remember { mutableStateOf(listOf<ShortcutOption>()) }

                                    Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .combinedClickable(
                                            enabled = app != null,
                                            onClick = { app?.let { onAppClick(it.packageName) } },
                                                onLongClick = {
                                                    if (app != null) {
                                                        val opts = mutableListOf<ShortcutOption>()
                                                        try {
                                                            val launcherApps = context.getSystemService(LauncherApps::class.java)
                                                            val query = LauncherApps.ShortcutQuery()
                                                                .setPackage(app.packageName)
                                                                .setQueryFlags(
                                                                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                                                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                                                                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                                                                )
                                                            val shortcutsList = launcherApps?.getShortcuts(query, Process.myUserHandle())
                                                            if (!shortcutsList.isNullOrEmpty()) {
                                                                shortcutsList.forEach { s ->
                                                                    val label = s.shortLabel?.toString() ?: s.longLabel?.toString() ?: s.id
                                                                    var shortcutIcon: Drawable? = null
                                                                    try {
                                                                        val getIconMethod = s::class.java.getMethod("getIcon")
                                                                        val iconObj = getIconMethod.invoke(s)
                                                                        if (iconObj is android.graphics.drawable.Icon) {
                                                                            shortcutIcon = iconObj.loadDrawable(context)
                                                                        }
                                                                    } catch (_: Exception) { }
                                                                    opts.add(ShortcutOption(label, shortcutIcon) {
                                                                        try {
                                                                            launcherApps.startShortcut(app.packageName, s.id, null, null, Process.myUserHandle())
                                                                        } catch (_: Exception) { }
                                                                    })
                                                                }
                                                            }
                                                        } catch (_: Exception) { }
                                                        opts.add(ShortcutOption("Open", null) { onAppClick(app.packageName) })
                                                        opts.add(ShortcutOption("App info", null) {
                                                            try {
                                                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${app.packageName}".toUri())
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                context.startActivity(intent)
                                                            } catch (_: Exception) { }
                                                        })
                                                        optionsForItem = opts
                                                        showOptionsForItem = true
                                                    }
                                                }
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                if (app != null) {
                                    Image(
                                        bitmap = app.icon.toBitmap().asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                } else {
                                    // empty placeholder to keep layout consistent
                                    Box(modifier = Modifier.size(56.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                }
                            }
                        if (showOptionsForItem) {
                            AlertDialog(
                                onDismissRequest = { showOptionsForItem = false },
                                confirmButton = {},
                                text = {
                                    Column {
                                        optionsForItem.forEach { opt ->
                                            TextButton(onClick = {
                                                opt.action()
                                                showOptionsForItem = false
                                            }) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (opt.icon != null) {
                                                        Image(
                                                            bitmap = opt.icon.toBitmap().asImageBitmap(),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                    }
                                                    Text(opt.label)
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(shortcuts.take(5)) { app ->
                                    ShortcutItem(
                                        app = app,
                                        onAppClick = { onAppClick(app.packageName) }
                                    )
                        }
                    }
                }
            }
            // Bottom action buttons to replace swipe gestures
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onNavigateToSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.8f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Settings")
                }
                Button(
                    onClick = onNavigateToAppDrawer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.8f),
                        contentColor = Color.White
                    )
                ) {
                    Text("All apps")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortcutItem(app: AppInfo, onAppClick: () -> Unit) {
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }
    var options by remember { mutableStateOf(listOf<ShortcutOption>()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onAppClick,
                onLongClick = {
                    val opts = mutableListOf<ShortcutOption>()
                    try {
                        val launcherApps = context.getSystemService(LauncherApps::class.java)
                        val query = LauncherApps.ShortcutQuery()
                            .setPackage(app.packageName)
                            .setQueryFlags(
                                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                            )
                        val shortcutsList = launcherApps?.getShortcuts(query, Process.myUserHandle())
                        if (!shortcutsList.isNullOrEmpty()) {
                            shortcutsList.forEach { s ->
                                val label = s.shortLabel?.toString() ?: s.longLabel?.toString() ?: s.id
                                var shortcutIcon: Drawable? = null
                                try {
                                    val getIconMethod = s::class.java.getMethod("getIcon")
                                    val iconObj = getIconMethod.invoke(s)
                                    if (iconObj is android.graphics.drawable.Icon) {
                                        shortcutIcon = iconObj.loadDrawable(context)
                                    }
                                } catch (_: Exception) { }
                                opts.add(ShortcutOption(label, shortcutIcon) {
                                    try {
                                        launcherApps.startShortcut(app.packageName, s.id, null, null, Process.myUserHandle())
                                    } catch (_: Exception) { }
                                })
                            }
                        }
                    } catch (_: Exception) { }
                    opts.add(ShortcutOption("Open", null) { onAppClick() })
                    opts.add(ShortcutOption("App info", null) {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${app.packageName}".toUri())
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) { }
                    })
                    options = opts
                    showOptions = true
                }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = app.label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            confirmButton = {},
            text = {
                Column {
                    options.forEach { opt ->
                        TextButton(onClick = {
                            opt.action()
                            showOptions = false
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (opt.icon != null) {
                                    Image(
                                        bitmap = opt.icon.toBitmap().asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(opt.label)
                            }
                        }
                    }
                }
            }
        )
    }
}

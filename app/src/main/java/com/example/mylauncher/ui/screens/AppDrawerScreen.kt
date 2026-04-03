package com.example.mylauncher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import android.graphics.drawable.Drawable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.content.Intent
import android.net.Uri
import android.content.pm.LauncherApps
import android.os.Process
import android.provider.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.unit.dp
import com.example.mylauncher.domain.AppInfo
import com.example.mylauncher.ui.viewmodel.AppDrawerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AppDrawerScreen(
    viewModel: AppDrawerViewModel,
    onBack: () -> Unit,
    onAppClick: (String) -> Unit,
    isEditMode: Boolean,
    permissionState: PermissionState
) {
    val apps by viewModel.apps.collectAsState()
    val shortcutCount = apps.count { it.isShortcut }
    val maxShortcuts = 5
    val expandedPackage = remember { mutableStateOf<String?>(null) }
    val expandedOptions = remember { mutableStateOf<List<ShortcutOption>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Apps") },
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (isEditMode) {
                val remaining = maxShortcuts - shortcutCount
                val message = if (remaining > 0) {
                    "You have $remaining shortcut slots available."
                } else {
                    "You have no shortcut slots available."
                }
                Text(
                    text = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(apps) { app ->
                    AppItem(
                        app = app,
                        onToggleShortcut = {
                            if (shortcutCount < maxShortcuts || app.isShortcut) {
                                viewModel.toggleShortcut(app.packageName)
                            }
                        },
                        onAppClick = { onAppClick(app.packageName); expandedPackage.value = null },
                        isEditMode = isEditMode
                        ,isExpanded = expandedPackage.value == app.packageName
                        ,options = if (expandedPackage.value == app.packageName) expandedOptions.value else emptyList()
                        ,onRequestExpand = { opts -> expandedOptions.value = opts; expandedPackage.value = app.packageName }
                        ,onRequestCollapse = { if (expandedPackage.value == app.packageName) expandedPackage.value = null }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItem(
    app: AppInfo,
    onToggleShortcut: () -> Unit,
    onAppClick: () -> Unit,
    isEditMode: Boolean,
    isExpanded: Boolean,
    options: List<ShortcutOption>,
    onRequestExpand: (List<ShortcutOption>) -> Unit,
    onRequestCollapse: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (!isEditMode) onAppClick() },
                onLongClick = {
                    if (!isEditMode) {
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
                            val shortcuts = launcherApps?.getShortcuts(query, Process.myUserHandle())
                            if (!shortcuts.isNullOrEmpty()) {
                                shortcuts.forEach { s ->
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

                        // Always offer Open and App info
                        opts.add(ShortcutOption("Open", null) { onAppClick(); onRequestCollapse() })
                        opts.add(ShortcutOption("App info", null) {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    "package:${app.packageName}".toUri())
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) { }
                            onRequestCollapse()
                        })

                        onRequestExpand(opts)
                    }
                }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = app.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (isEditMode) {
            Checkbox(
                checked = app.isShortcut,
                onCheckedChange = { onToggleShortcut() }
            )
        }
    }

    if (isExpanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 44.dp, end = 16.dp, bottom = 8.dp)
        ) {
            options.forEach { opt ->
                TextButton(
                    onClick = {
                        opt.action()
                        onRequestCollapse()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (opt.icon != null) {
                            Image(
                                bitmap = opt.icon.toBitmap().asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(opt.label, modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
    }
}

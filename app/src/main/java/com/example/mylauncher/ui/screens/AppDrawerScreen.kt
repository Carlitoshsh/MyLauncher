package com.example.mylauncher.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.unit.dp
import com.example.mylauncher.domain.AppInfo
import com.example.mylauncher.ui.viewmodel.AppDrawerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                        onAppClick = { onAppClick(app.packageName) },
                        isEditMode = isEditMode
                    )
                }
            }
        }
    }
}

@Composable
fun AppItem(
    app: AppInfo,
    onToggleShortcut: () -> Unit,
    onAppClick: () -> Unit,
    isEditMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isEditMode, onClick = onAppClick)
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
}

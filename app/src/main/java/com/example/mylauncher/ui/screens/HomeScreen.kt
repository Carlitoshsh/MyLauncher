package com.example.mylauncher.ui.screens

import android.app.WallpaperManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.mylauncher.domain.AppInfo
import com.example.mylauncher.ui.components.Clock
import com.example.mylauncher.ui.viewmodel.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAppDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAppClick: (String) -> Unit,
    permissionState: PermissionState
) {
    val shortcuts by viewModel.shortcuts.collectAsState()
    val context = LocalContext.current
    var use24HourFormat by remember { mutableStateOf(false) }

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
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    if (dragAmount < -10) { // Swipe left
                        onNavigateToSettings()
                    }
                }
            }
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Clock(use24HourFormat = use24HourFormat)
            Spacer(modifier = Modifier.height(32.dp))

            if (shortcuts.isEmpty()) {
                Text("No shortcuts selected. Tap below to add some!", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateToAppDrawer) {
                    Text("Add Shortcuts")
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
    }
}

@Composable
fun ShortcutItem(app: AppInfo, onAppClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAppClick)
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
}

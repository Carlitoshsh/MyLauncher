package com.example.mylauncher.domain

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    val isShortcut: Boolean = false
)

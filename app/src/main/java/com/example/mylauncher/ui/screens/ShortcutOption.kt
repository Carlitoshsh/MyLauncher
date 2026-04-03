package com.example.mylauncher.ui.screens

import android.graphics.drawable.Drawable

data class ShortcutOption(val label: String, val icon: Drawable?, val action: () -> Unit)

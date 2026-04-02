package com.example.mylauncher.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Clock(use24HourFormat: Boolean = false) {
    val timeFormatPattern = if (use24HourFormat) "HH:mm" else "h:mm a"
    val timeFormat = remember(timeFormatPattern) { SimpleDateFormat(timeFormatPattern, Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
    val time = remember { mutableStateOf(timeFormat.format(Date())) }
    val date = remember { mutableStateOf(dateFormat.format(Date())) }
    val colorDefault = Color.White // Text color set to white

    LaunchedEffect(Unit, use24HourFormat) {
        while (true) {
            time.value = timeFormat.format(Date())
            date.value = dateFormat.format(Date())
            delay(1000)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = time.value, style = MaterialTheme.typography.displayLarge, color=colorDefault)
        Text(text = date.value, style = MaterialTheme.typography.headlineSmall, color=colorDefault)
    }
}

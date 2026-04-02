package com.example.mylauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.mylauncher.domain.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppDataSource(private val context: Context) {

    suspend fun getInstalledApps(shortcuts: Set<String>): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        resolveInfoList.mapNotNull { resolveInfo ->
            try {
                val appInfo = packageManager.getApplicationInfo(resolveInfo.activityInfo.packageName, 0)
                AppInfo(
                    label = packageManager.getApplicationLabel(appInfo).toString(),
                    packageName = appInfo.packageName,
                    icon = packageManager.getApplicationIcon(appInfo.packageName),
                    isShortcut = shortcuts.contains(appInfo.packageName)
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }.sortedBy { it.label }
    }
}

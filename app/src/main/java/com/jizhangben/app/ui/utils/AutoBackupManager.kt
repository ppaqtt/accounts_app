package com.jizhangben.app.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoBackupManager(context: Context) {
    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
    private val backupDir = File(context.filesDir, "auto_backups")

    val isEnabledState = mutableStateOf(prefs.getBoolean("auto_backup_enabled", false))
    var isEnabled: Boolean
        get() = isEnabledState.value
        set(value) {
            isEnabledState.value = value
            prefs.edit().putBoolean("auto_backup_enabled", value).apply()
        }

    val lastBackupTimeState = mutableStateOf(prefs.getString("last_backup_time", null))
    var lastBackupTime: String?
        get() = lastBackupTimeState.value
        set(value) {
            lastBackupTimeState.value = value
            prefs.edit().putString("last_backup_time", value).apply()
        }

    init {
        if (!backupDir.exists()) backupDir.mkdirs()
    }

    fun performBackup(dbFile: File): String? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "jizhangben_backup_$timestamp.db")
            dbFile.copyTo(backupFile, overwrite = true)

            // 只保留最近5个备份
            val backups = backupDir.listFiles()?.sortedByDescending { it.lastModified() }
            backups?.drop(5)?.forEach { it.delete() }

            lastBackupTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            backupFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun getBackupList(): List<File> {
        return backupDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun restoreFromBackup(backupFile: File, targetDb: File): Boolean {
        return try {
            backupFile.copyTo(targetDb, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }
}

val LocalAutoBackupManager = compositionLocalOf<AutoBackupManager> {
    error("No AutoBackupManager provided")
}

@Composable
fun ProvideAutoBackupManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val manager = remember { AutoBackupManager(context) }
    CompositionLocalProvider(LocalAutoBackupManager provides manager) {
        content()
    }
}

package com.jizhang.xiaomeng.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AppSettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    val defaultExpenseState = mutableStateOf(prefs.getBoolean("default_expense", true))
    var defaultExpense: Boolean
        get() = defaultExpenseState.value
        set(value) {
            defaultExpenseState.value = value
            prefs.edit().putBoolean("default_expense", value).apply()
        }

    val showAmountState = mutableStateOf(prefs.getBoolean("show_amount", true))
    var showAmount: Boolean
        get() = showAmountState.value
        set(value) {
            showAmountState.value = value
            prefs.edit().putBoolean("show_amount", value).apply()
        }
}

val LocalAppSettings = compositionLocalOf<AppSettingsManager> {
    error("No AppSettingsManager provided")
}

@Composable
fun ProvideAppSettingsManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val manager = remember { AppSettingsManager(context) }
    CompositionLocalProvider(LocalAppSettings provides manager) {
        content()
    }
}

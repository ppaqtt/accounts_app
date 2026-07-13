package com.jizhangben.app.ui.utils

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

class LanguageManager(context: Context) {
    private val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    val currentLanguageState = mutableStateOf(prefs.getString("app_language", "zh") ?: "zh")
    var currentLanguage: String
        get() = currentLanguageState.value
        set(value) {
            currentLanguageState.value = value
            prefs.edit().putString("app_language", value).apply()
        }

    fun getLocale(): Locale {
        return when (currentLanguage) {
            "en" -> Locale.ENGLISH
            else -> Locale.SIMPLIFIED_CHINESE
        }
    }
}

val LocalLanguageManager = compositionLocalOf<LanguageManager> {
    error("No LanguageManager provided")
}

@Composable
fun ProvideLanguageManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val manager = remember { LanguageManager(context) }
    CompositionLocalProvider(LocalLanguageManager provides manager) {
        content()
    }
}

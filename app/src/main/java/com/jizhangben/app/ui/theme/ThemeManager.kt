package com.jizhangben.app.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode(val value: String, val displayName: String) {
    FOLLOW_SYSTEM("follow_system", "跟随系统"),
    LIGHT("light", "浅色模式"),
    DARK("dark", "深色模式")
}

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    val themeModeState = mutableStateOf(
        when (prefs.getString("theme_mode", ThemeMode.FOLLOW_SYSTEM.value)) {
            ThemeMode.LIGHT.value -> ThemeMode.LIGHT
            ThemeMode.DARK.value -> ThemeMode.DARK
            else -> ThemeMode.FOLLOW_SYSTEM
        }
    )

    val dynamicColorState = mutableStateOf(prefs.getBoolean("dynamic_color", true))

    var themeMode: ThemeMode
        get() = themeModeState.value
        set(value) {
            themeModeState.value = value
            prefs.edit().putString("theme_mode", value.value).apply()
        }

    var dynamicColor: Boolean
        get() = dynamicColorState.value
        set(value) {
            dynamicColorState.value = value
            prefs.edit().putBoolean("dynamic_color", value).apply()
        }
}

val LocalThemeManager = compositionLocalOf<ThemeManager> {
    error("No ThemeManager provided")
}

@Composable
fun ProvideThemeManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        content()
    }
}

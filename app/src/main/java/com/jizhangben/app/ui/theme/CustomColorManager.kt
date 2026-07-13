package com.jizhangben.app.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

class CustomColorManager(context: Context) {
    private val prefs = context.getSharedPreferences("color_prefs", Context.MODE_PRIVATE)

    val customColorState = mutableStateOf(prefs.getInt("custom_color", 0xFFFF6B6B.toInt()))
    var customColor: Int
        get() = customColorState.value
        set(value) {
            customColorState.value = value
            prefs.edit().putInt("custom_color", value).apply()
        }

    val useCustomColorState = mutableStateOf(prefs.getBoolean("use_custom_color", false))
    var useCustomColor: Boolean
        get() = useCustomColorState.value
        set(value) {
            useCustomColorState.value = value
            prefs.edit().putBoolean("use_custom_color", value).apply()
        }

    companion object {
        val PRESET_COLORS = listOf(
            0xFFFF6B6B.toInt(), // 珊瑚粉
            0xFF4CAF50.toInt(), // 翠绿
            0xFF2196F3.toInt(), // 天蓝
            0xFFFF9800.toInt(), // 橙色
            0xFF9C27B0.toInt(), // 紫色
            0xFFE91E63.toInt(), // 粉红
            0xFF00BCD4.toInt(), // 青色
            0xFF795548.toInt(), // 棕色
            0xFF607D8B.toInt(), // 蓝灰
            0xFFFFEB3B.toInt()  // 黄色
        )
    }
}

val LocalCustomColorManager = compositionLocalOf<CustomColorManager> {
    error("No CustomColorManager provided")
}

@Composable
fun ProvideCustomColorManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val manager = remember { CustomColorManager(context) }
    CompositionLocalProvider(LocalCustomColorManager provides manager) {
        content()
    }
}

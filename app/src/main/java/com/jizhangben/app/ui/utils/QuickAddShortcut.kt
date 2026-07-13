package com.jizhangben.app.ui.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.jizhangben.app.MainActivity
import com.jizhangben.app.R

object QuickAddShortcut {
    fun createShortcut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return

            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("open_add", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val shortcut = ShortcutInfo.Builder(context, "quick_add_transaction")
                .setShortLabel("快速记账")
                .setLongLabel("快速打开记账页面")
                .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(intent)
                .build()

            shortcutManager.dynamicShortcuts = listOf(shortcut)
        }
    }
}
